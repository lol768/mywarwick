package services

import actors.WebsocketActor.Notification
import com.google.inject.{ImplementedBy, Inject}
import models._
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import services.ActivityError._
import services.dao.{ActivityCreationDao, ActivityDao, ActivityTagDao}
import services.messaging.MessagingService
import warwick.sso.{User, Usercode}

@ImplementedBy(classOf[ActivityServiceImpl])
trait ActivityService {
  def setPublished(activityId: String): Unit

  def getActivitiesToPublishNow(): Seq[ActivityIdAndAudienceId]

  def getActivityById(id: String): Option[Activity]

  def getActivitiesForUser(user: User, limit: Int = 50, before: Option[DateTime] = None): Seq[ActivityResponse]

  def save(activity: ActivitySave, recipients: Set[Usercode]): Either[Seq[ActivityError], String]

  def save(activity: ActivitySave, recipients: Seq[Usercode]): Either[Seq[ActivityError], String]

  def getLastReadDate(user: User): Option[DateTime]

  def setLastReadDate(user: User, dateTime: DateTime): Boolean

  def getActivitiesByPublisherId(publisherId: String, limit: Int = 50): Seq[Activity]

  def getActivitiesByProviderId(providerId: String, limit: Int = 50): Seq[Activity]

  def getActivityIcon(providerId: String): Option[ActivityIcon]

}

class ActivityServiceImpl @Inject()(
  dao: ActivityDao,
  creationDao: ActivityCreationDao,
  tagDao: ActivityTagDao,
  messaging: MessagingService,
  pubSub: PubSub,
  @NamedDatabase("default") db: Database,
  activityTypeService: ActivityTypeService
) extends ActivityService {

  override def setPublished(activityId: String): Unit =
    db.withConnection(implicit c => dao.setPublished(activityId))

  override def getActivitiesToPublishNow(): Seq[ActivityIdAndAudienceId] =
    db.withConnection(implicit c => dao.getActivitiesToPublishNow())

  override def getActivityById(id: String): Option[Activity] =
    db.withConnection(implicit c => dao.getActivityById(id))

  override def save(activity: ActivitySave, recipients: Seq[Usercode]): Either[Seq[ActivityError], String] =
    save(activity, recipients.toSet)

  override def save(activity: ActivitySave, recipients: Set[Usercode]): Either[Seq[ActivityError], String] = {
    if (recipients.isEmpty) {
      Left(Seq(NoRecipients))
    } else {
      val errors = validateActivity(activity)
      if (errors.nonEmpty) {
        Left(errors)
      } else {
        db.withTransaction { implicit c =>
          val replaceIds = tagDao.getActivitiesWithTags(activity.replace, activity.providerId)

          val result = creationDao.createActivity(activity, recipients, replaceIds)

          if (result.activity.shouldNotify) {
            messaging.send(recipients, result.activity)
          }

          recipients.foreach(usercode => pubSub.publish(usercode.string, Notification(result)))

          Right(result.activity.id)
        }
      }
    }
  }

  private def validateActivity(activity: ActivitySave): Seq[ActivityError] = {
    val maybeActivityTypeError: Seq[ActivityError] =
      if (!activityTypeService.isValidActivityType(activity.`type`)) {
        Seq(InvalidActivityType(activity.`type`))
      } else {
        Seq.empty
      }

    activity.tags
      .foldLeft(maybeActivityTypeError) { (errors, tag) =>
        if (!activityTypeService.isValidActivityTagName(tag.name)) {
          errors :+ InvalidTagName(tag.name)
        } else if (!activityTypeService.isValidActivityTag(tag.name, tag.value.internalValue)) {
          errors :+ InvalidTagValue(tag.name, tag.value.internalValue)
        } else {
          errors
        }
      }
  }

  override def getActivitiesForUser(user: User, limit: Int, before: Option[DateTime]): Seq[ActivityResponse] =
    db.withConnection(implicit c => dao.getActivitiesForUser(user.usercode.string, limit.min(50), before))

  override def getLastReadDate(user: User): Option[DateTime] =
    db.withConnection(implicit c => dao.getLastReadDate(user.usercode.string))

  override def setLastReadDate(user: User, dateTime: DateTime): Boolean =
    db.withConnection(implicit c => dao.saveLastReadDate(user.usercode.string, dateTime))

  override def getActivitiesByPublisherId(publisherId: String, limit: Int) =
    db.withConnection(implicit c => dao.getActivitiesByPublisherId(publisherId, limit))

  override def getActivitiesByProviderId(providerId: String, limit: Int) =
    db.withConnection(implicit c => dao.getActivitiesByProviderId(providerId, limit))

  override def getActivityIcon(providerId: String): Option[ActivityIcon] =
    db.withConnection(implicit c => dao.getActivityIcon(providerId))

}

sealed trait ActivityError {
  def message: String
}

object ActivityError {

  object NoRecipients extends ActivityError {
    val message = "No valid recipients"
  }

  case class InvalidActivityType(name: String) extends ActivityError {
    def message = s"The activity type '$name' is not valid"
  }

  case class InvalidProviderId(name: String) extends ActivityError {
    def message = s"No provider found with id '$name'"
  }

  case class InvalidTagName(name: String) extends ActivityError {
    def message = s"The tag name '$name' is not valid"
  }

  case class InvalidTagValue(name: String, value: String) extends ActivityError {
    def message = s"The value '$value' for tag '$name' is not valid"
  }

}
