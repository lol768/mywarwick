package services

import actors.WebsocketActor.Notification
import com.google.inject.{ImplementedBy, Inject}
import models.{Activity, ActivityPrototype, ActivityResponse}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import services.dao.{ActivityCreationDao, ActivityDao, ActivityTagDao}
import warwick.sso.{User, Usercode}

import scala.util.{Failure, Success, Try}

@ImplementedBy(classOf[ActivityServiceImpl])
trait ActivityService {
  def getActivityById(id: String): Option[Activity]

  def getActivitiesForUser(user: User, limit: Int = 50, before: Option[DateTime] = None): Seq[ActivityResponse]

  def save(activity: ActivityPrototype): Try[String]
}

class ActivityServiceImpl @Inject()(
  activityRecipientService: ActivityRecipientService,
  activityCreationDao: ActivityCreationDao,
  activityDao: ActivityDao,
  activityTagDao: ActivityTagDao,
  pubsub: PubSub,
  @NamedDatabase("default") db: Database
) extends ActivityService {

  override def getActivityById(id: String): Option[Activity] = db.withConnection(implicit c => activityDao.getActivityById(id))

  implicit def stringSeqToUsercodeSeq(strings: Seq[String]): Seq[Usercode] = strings.map(Usercode)

  implicit def stringSeqToGroupNameSeq(strings: Seq[String]): Seq[GroupName] = strings.map(GroupName)

  def save(activity: ActivityPrototype): Try[String] = {
    val recipients = activityRecipientService.getRecipientUsercodes(
      activity.recipients.users.getOrElse(Seq.empty),
      activity.recipients.groups.getOrElse(Seq.empty)
    )

    if (recipients.isEmpty) {
      Failure(NoRecipientsException)
    } else {
      db.withTransaction { implicit c =>
        val replaceIds = activityTagDao.getActivitiesWithTags(activity.replace, activity.providerId)

        val result = activityCreationDao.createActivity(activity, recipients, replaceIds)

        recipients.foreach(usercode => pubsub.publish(usercode.string, Notification(result)))

        Success(result.activity.id)
      }
    }

  }

  override def getActivitiesForUser(user: User, limit: Int, before: Option[DateTime]): Seq[ActivityResponse] =
    db.withConnection(implicit c => activityDao.getActivitiesForUser(user.usercode.string, limit.min(50), before))
}

object NoRecipientsException extends Throwable
