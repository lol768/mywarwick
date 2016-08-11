package services

import com.google.inject.{ImplementedBy, Inject}
import models._
import models.news.Audience
import org.joda.time.DateTime
import org.quartz._
import play.api.db.{Database, NamedDatabase}
import services.ActivityError._
import services.dao._
import services.job.PublishActivityJob
import warwick.sso.{User, Usercode}

@ImplementedBy(classOf[ActivityServiceImpl])
trait ActivityService {
  def getActivityById(id: String): Option[Activity]

  def getActivitiesForUser(user: User, limit: Int = 50, before: Option[DateTime] = None): Seq[ActivityResponse]

  def save(activity: ActivitySave, audience: Audience): Either[Seq[ActivityError], String]

  def setRecipients(activity: Activity, recipients: Set[Usercode]): Either[Seq[ActivityError], Unit]

  def update(id: String, activity: ActivitySave, audience: Audience): Either[Seq[ActivityError], String]

  def delete(id: String): Either[Seq[ActivityError], Unit]

  def getLastReadDate(user: User): Option[DateTime]

  def setLastReadDate(user: User, dateTime: DateTime): Boolean

  def getActivitiesByPublisherId(publisherId: String, limit: Int = 50): Seq[Activity]

  def getActivitiesByProviderId(providerId: String, limit: Int = 50): Seq[Activity]

  def getActivityIcon(providerId: String): Option[ActivityIcon]

}

class ActivityServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  dao: ActivityDao,
  activityTypeService: ActivityTypeService,
  tagDao: ActivityTagDao,
  audienceDao: AudienceDao,
  recipientDao: ActivityRecipientDao,
  scheduler: SchedulerService
) extends ActivityService {

  override def getActivityById(id: String): Option[Activity] =
    db.withConnection(implicit c => dao.getActivityById(id))

  override def update(activityId: String, activity: ActivitySave, audience: Audience) = {
    getActivityById(activityId).map { existingActivity =>
      if (existingActivity.publishedAt.isBeforeNow) {
        Left(Seq(AlreadyPublished))
      } else {
        db.withTransaction { implicit c =>
          val audienceId = existingActivity.audienceId match {
            case Some(id) if audienceDao.getAudience(activityId) == audience =>
              id
            case Some(id) =>
              audienceDao.deleteAudience(id)
              audienceDao.saveAudience(audience)
            case _ =>
              // Don't expect this, but for completeness
              audienceDao.saveAudience(audience)
          }

          dao.update(activityId, activity, audienceId)

          schedulePublishJob(activityId, audienceId, activity.publishedAt.getOrElse(DateTime.now))
        }
        Right(activityId)
      }
    }.getOrElse {
      Left(Seq(DoesNotExist))
    }
  }

  override def delete(id: String) = {
    getActivityById(id).map { existingActivity =>
      if (existingActivity.publishedAt.isBeforeNow) {
        Left(Seq(AlreadyPublished))
      } else {
        db.withTransaction(implicit c => dao.delete(id))
        unschedulePublishJob(id)

        Right(())
      }
    }.getOrElse {
      Left(Seq(DoesNotExist))
    }
  }

  override def save(activity: ActivitySave, audience: Audience): Either[Seq[ActivityError], String] = {
    val errors = validateAudience(audience) ++ validateActivity(activity)
    if (errors.nonEmpty) {
      Left(errors)
    } else {
      db.withTransaction { implicit c =>
        val replaceIds = tagDao.getActivitiesWithTags(activity.replace, activity.providerId)

        val audienceId = audienceDao.saveAudience(audience)
        val activityId = dao.save(activity, audienceId, replaceIds)

        activity.tags.foreach(tag => tagDao.save(activityId, tag))

        schedulePublishJob(activityId, audienceId, activity.publishedAt.getOrElse(DateTime.now))

        Right(activityId)
      }
    }
  }

  override def setRecipients(activity: Activity, recipients: Set[Usercode]): Either[Seq[ActivityError], Unit] = {
    db.withTransaction { implicit c =>
      recipientDao.setRecipients(activity, recipients)
    }
    Right(())
  }

  private def validateAudience(audience: Audience): Seq[ActivityError] = {
    val maybeEmptyAudience =
      if (audience.components.isEmpty) Seq(EmptyAudience)
      else Nil

    val maybePublicAudience =
      if (audience.public) Seq(PublicAudience)
      else Nil

    maybeEmptyAudience ++ maybePublicAudience
  }

  private def validateActivity(activity: ActivitySave): Seq[ActivityError] = {
    val maybeActivityTypeError: Seq[ActivityError] =
      if (!activityTypeService.isValidActivityType(activity.`type`))
        Seq(InvalidActivityType(activity.`type`))
      else Nil

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

  private def schedulePublishJob(activityId: String, audienceId: String, publishDate: DateTime): Unit = {
    val key = new JobKey(activityId, PublishActivityJob.name)

    scheduler.deleteJob(key)

    val job = JobBuilder.newJob(classOf[PublishActivityJob])
      .withIdentity(key)
      .usingJobData("activityId", activityId)
      .usingJobData("audienceId", audienceId)
      .build()

    if (publishDate.isAfterNow) {
      val trigger = TriggerBuilder.newTrigger()
        .startAt(publishDate.toDate)
        .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
        .build()

      scheduler.scheduleJob(job, trigger)
    } else {
      scheduler.triggerJobNow(job)
    }
  }

  private def unschedulePublishJob(activityId: String): Unit = {
    scheduler.deleteJob(new JobKey(activityId, PublishActivityJob.name))
  }

}

sealed trait ActivityError {
  def message: String
}

object ActivityError {

  object EmptyAudience extends ActivityError {
    val message = "Empty audience"
  }

  object PublicAudience extends ActivityError {
    val message = "Audience cannot be public"
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

  object AlreadyPublished extends ActivityError {
    val message = "This activity cannot be modified as it has already been published"
  }

  object DoesNotExist extends ActivityError {
    val message = "This activity does not exist"
  }

}
