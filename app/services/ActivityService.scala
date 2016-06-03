package services

import actors.WebsocketActor.Notification
import com.google.inject.{ImplementedBy, Inject}
import models.{Activity, ActivityResponse, ActivitySave}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import services.dao.{ActivityCreationDao, ActivityDao, ActivityTagDao}
import services.messaging.MessagingService
import warwick.sso.{User, Usercode}

import scala.util.{Failure, Success, Try}

@ImplementedBy(classOf[ActivityServiceImpl])
trait ActivityService {
  def getActivityById(id: String): Option[Activity]

  def getActivitiesForUser(user: User, limit: Int = 50, before: Option[DateTime] = None): Seq[ActivityResponse]

  def save(activity: ActivitySave, recipients: Set[Usercode]): Try[String]

  def save(activity: ActivitySave, recipients: Seq[Usercode]): Try[String]

  def getLastReadDate(user: User): Option[DateTime]

  def setLastReadDate(user: User, dateTime: DateTime): Boolean

  def getActivitiesByProviderId(providerId: String, limit: Int = 50): Seq[Activity]
}

class ActivityServiceImpl @Inject()(
  dao: ActivityDao,
  creationDao: ActivityCreationDao,
  tagDao: ActivityTagDao,
  messaging: MessagingService,
  pubSub: PubSub,
  @NamedDatabase("default") db: Database
) extends ActivityService {

  override def getActivityById(id: String): Option[Activity] =
    db.withConnection(implicit c => dao.getActivityById(id))

  override def save(activity: ActivitySave, recipients: Seq[Usercode]): Try[String] =
    save(activity, recipients.toSet)

  override def save(activity: ActivitySave, recipients: Set[Usercode]): Try[String] = {
    if (recipients.isEmpty) {
      Failure(NoRecipientsException)
    } else {
      db.withTransaction { implicit c =>
        val replaceIds = tagDao.getActivitiesWithTags(activity.replace, activity.providerId)

        val result = creationDao.createActivity(activity, recipients, replaceIds)

        if (result.activity.shouldNotify) {
          messaging.send(recipients, result.activity)
        }

        recipients.foreach(usercode => pubSub.publish(usercode.string, Notification(result)))

        Success(result.activity.id)
      }
    }
  }

  override def getActivitiesForUser(user: User, limit: Int, before: Option[DateTime]): Seq[ActivityResponse] =
    db.withConnection(implicit c => dao.getActivitiesForUser(user.usercode.string, limit.min(50), before))

  override def getLastReadDate(user: User): Option[DateTime] =
    db.withConnection(implicit c => dao.getLastReadDate(user.usercode.string))

  override def setLastReadDate(user: User, dateTime: DateTime): Boolean =
    db.withConnection(implicit c => dao.saveLastReadDate(user.usercode.string, dateTime))

  override def getActivitiesByProviderId(providerId: String, limit: Int) =
    db.withConnection(implicit c => dao.getActivitiesByProviderId(providerId, limit))
}

object NoRecipientsException extends Throwable
