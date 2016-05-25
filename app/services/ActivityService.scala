package services

import actors.WebsocketActor.Notification
import com.google.inject.{ImplementedBy, Inject}
import models.{Activity, ActivityPrototype, ActivityResponse}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import services.dao.{ActivityCreationDao, ActivityDao, ActivityTagDao}
import services.messaging.MessagingService
import warwick.sso.{GroupName, User, Usercode}

import scala.util.{Failure, Success, Try}

@ImplementedBy(classOf[ActivityServiceImpl])
trait ActivityService {
  def getActivityById(id: String): Option[Activity]

  def getActivitiesForUser(user: User, limit: Int = 50, before: Option[DateTime] = None): Seq[ActivityResponse]

  def save(activity: ActivityPrototype): Try[String]

  def getLastReadDate(user: User): Option[DateTime]

  def setLastReadDate(user: User, dateTime: DateTime): Boolean

  def getActivitiesByProviderId(providerId: String, limit: Int = 50): Seq[Activity]
}

class ActivityServiceImpl @Inject()(
  activityRecipientService: ActivityRecipientService,
  activityCreationDao: ActivityCreationDao,
  activityDao: ActivityDao,
  activityTagDao: ActivityTagDao,
  messaging: MessagingService,
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

        if (result.activity.shouldNotify) {
          messaging.send(recipients, result.activity)
        }

        recipients.foreach(usercode => pubsub.publish(usercode.string, Notification(result)))

        Success(result.activity.id)
      }
    }

  }

  override def getActivitiesForUser(user: User, limit: Int, before: Option[DateTime]): Seq[ActivityResponse] =
    db.withConnection(implicit c => activityDao.getActivitiesForUser(user.usercode.string, limit.min(50), before))

  override def getLastReadDate(user: User): Option[DateTime] =
    db.withConnection(implicit c => activityDao.getLastReadDate(user.usercode.string))

  override def setLastReadDate(user: User, dateTime: DateTime): Boolean =
    db.withConnection(implicit c => activityDao.saveLastReadDate(user.usercode.string, dateTime))

  override def getActivitiesByProviderId(providerId: String, limit: Int) =
    db.withConnection(implicit c => activityDao.getActivitiesByProviderId(providerId, limit))
}

object NoRecipientsException extends Throwable
