package services

import com.google.inject.{ImplementedBy, Inject}
import models.news.NotificationData
import models.publishing.PublisherActivityCount
import models.{Audience, _}
import org.joda.time.{DateTime, Interval}
import org.quartz._
import play.api.db.{Database, NamedDatabase}
import services.ActivityError._
import services.dao._
import services.elasticsearch.ActivityESService
import services.job.PublishActivityJob
import warwick.sso.{User, UserLookupService, Usercode}

@ImplementedBy(classOf[ActivityServiceImpl])
trait ActivityService {
  def getActivityById(id: String): Option[Activity]

  def getActivityRenderById(id: String): Option[ActivityRender]

  def getActivitiesForUser(user: User, before: Option[String], since: Option[String], limit: Int): Seq[ActivityRender]

  def getNotificationsForUser(user: User, before: Option[String], since: Option[String], limit: Int): Seq[ActivityRender]

  def countUnreadNotificationsForUsercode(usercode: Usercode): Int

  def save(activity: ActivitySave, audience: Audience): Either[Seq[ActivityError], String]

  def setRecipients(activity: Activity, recipients: Set[Usercode]): Either[Seq[ActivityError], Unit]

  def update(id: String, activity: ActivitySave, audience: Audience): Either[Seq[ActivityError], String]

  def delete(id: String): Either[Seq[ActivityError], Unit]

  def getLastReadDate(user: User): Option[DateTime]

  def setLastReadDate(user: User, dateTime: DateTime): Boolean

  def getFutureActivitiesWithAudienceByPublisherId(publisherId: String, limit: Int = 50, includeApiUser:Boolean = true): Seq[ActivityRenderWithAudience]

  def getSendingActivitiesWithAudienceByPublisherId(publisherId: String, limit: Int = 50, includeApiUser:Boolean = true): Seq[ActivityRenderWithAudience]

  def getPastActivitiesWithAudienceByPublisherId(publisherId: String, limit: Int = 50, includeApiUser:Boolean = true): Seq[ActivityRenderWithAudience]

  def getActivityIcon(providerId: String): Option[ActivityIcon]

  def getActivityMutes(
    activity: Activity,
    tags: Seq[ActivityTag],
    recipients: Set[Usercode],
    now: DateTime = DateTime.now
  ): Seq[ActivityMute]

  def getActivityMutesForRecipient(recipient: Usercode, now: DateTime = DateTime.now): Seq[ActivityMuteRender]

  def save(activityMute: ActivityMuteSave): Either[Seq[ActivityError], String]

  def update(id: String, activityMute: ActivityMuteSave): Either[Seq[ActivityError], Unit]

  def expireActivityMute(recipient: Usercode, id: String): Either[Seq[ActivityError], ActivityMuteRender]

  def allProviders: Seq[(ActivityProvider, Option[ActivityIcon])]

  def getProvider(id: String): Option[ActivityProvider]

  def countNotificationsByPublishersInLast48Hours: Seq[PublisherActivityCount]

  def updateAudienceCount(activityId: String, audienceId: String, recipients: Set[Usercode]): Unit

  def markProcessed(id: String, usercode: Usercode): Unit

  def getActivityWithAudience(id: String): Option[ActivityRenderWithAudience]

  def getActivitiesForDateTimeRange(from: DateTime, to: DateTime): Seq[Activity]

  def getActivitiesForDateTimeRange(interval: Interval): Seq[Activity]

  def getActivityReadCountSincePublishedDate(activityId: String): Int
}

class ActivityServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  dao: ActivityDao,
  activityTypeService: ActivityTypeService,
  tagDao: ActivityTagDao,
  audienceService: AudienceService,
  audienceDao: AudienceDao,
  recipientDao: ActivityRecipientDao,
  muteDao: ActivityMuteDao,
  scheduler: SchedulerService,
  userLookupService: UserLookupService
) extends ActivityService {

  override def getActivityById(id: String): Option[Activity] =
    db.withConnection(implicit c => dao.getActivityById(id))

  override def getActivityRenderById(id: String): Option[ActivityRender] =
    db.withConnection(implicit c => dao.getActivityRenderById(id))


  override def update(activityId: String, activity: ActivitySave, audience: Audience): Either[Seq[ActivityError], String] = {
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
          val audienceSize = audience match {
            case Audience.Public => AudienceSize.Public
            case _ => AudienceSize.Finite(audienceService.resolve(audience).toOption.map(_.size).getOrElse(0))
          }

          dao.update(activityId, activity, audienceId, audienceSize)

          // Might be more efficient to store job with replace=true
          // instead of deleting separately.
          unschedulePublishJob(activityId)
          schedulePublishJob(activityId, audienceId, activity.publishedAt.getOrElse(DateTime.now))
        }
        Right(activityId)
      }
    }.getOrElse {
      Left(Seq(DoesNotExist))
    }
  }

  override def delete(id: String): Either[Seq[ActivityError], Unit] = {
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
        val audienceSize = audience match {
          case Audience.Public => AudienceSize.Public
          case _ => AudienceSize.Finite(audienceService.resolve(audience).toOption.map(_.size).getOrElse(0))
        }
        val activityId = dao.save(activity, audienceId, audienceSize, replaceIds)

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
    activity.tags.flatMap(tag => {
      if (!activityTypeService.isValidActivityTag(tag.name, tag.value.internalValue)) {
        Some(InvalidTagValue(tag.name, tag.value.internalValue))
      } else {
        None
      }
    })
  }

  override def getNotificationsForUser(user: User, before: Option[String], since: Option[String], limit: Int): Seq[ActivityRender] =
    db.withConnection(implicit c => dao.getActivitiesForUser(user.usercode.string, notifications = true, before, since, limit))

  override def countUnreadNotificationsForUsercode(usercode: Usercode): Int =
    db.withConnection(implicit c => {
      val concreteLastReadDate = dao.getLastReadDate(usercode.string).getOrElse(new DateTime(0))
      dao.countNotificationsSinceDate(usercode.string, concreteLastReadDate)
    })

  override def getActivitiesForUser(user: User, before: Option[String], since: Option[String], limit: Int): Seq[ActivityRender] =
    db.withConnection(implicit c => dao.getActivitiesForUser(user.usercode.string, notifications = false, before, since, limit))

  override def getLastReadDate(user: User): Option[DateTime] =
    db.withConnection(implicit c => dao.getLastReadDate(user.usercode.string))

  override def setLastReadDate(user: User, dateTime: DateTime): Boolean =
    db.withConnection(implicit c => dao.saveLastReadDate(user.usercode.string, dateTime))

  override def getFutureActivitiesWithAudienceByPublisherId(publisherId: String, limit: Int, includeApiUser: Boolean): Seq[ActivityRenderWithAudience] =
    mixinAudience(db.withConnection(implicit c => dao.getFutureActivitiesByPublisherId(publisherId, limit, includeApiUser)))

  override def getSendingActivitiesWithAudienceByPublisherId(publisherId: String, limit: Int, includeApiUser:Boolean): Seq[ActivityRenderWithAudience] =
    mixinAudience(db.withConnection(implicit c => dao.getSendingActivitiesByPublisherId(publisherId, limit, includeApiUser)))

  override def getPastActivitiesWithAudienceByPublisherId(publisherId: String, limit: Int, includeApiUser:Boolean): Seq[ActivityRenderWithAudience] =
    mixinAudience(db.withConnection(implicit c => dao.getPastActivitiesByPublisherId(publisherId, limit, includeApiUser)))

  override def getActivityWithAudience(id: String): Option[ActivityRenderWithAudience] =
    mixinAudience(db.withConnection(implicit c => dao.getActivityRenderById(id).toSeq)).headOption

  private def mixinAudience(activities: Seq[ActivityRender]): Seq[ActivityRenderWithAudience] = {
    db.withConnection { implicit c =>
      val audiences = activities.map(a => a.activity.id -> a.activity.audienceId.map(audienceDao.getAudience).getOrElse(Audience())).toMap
      val audienceSizes = dao.getAudienceSizes(activities.map(_.activity.id))
      val sentCounts = dao.getSentCounts(activities.map(_.activity.id))
      val users = userLookupService.getUsers(activities.map(_.activity.createdBy)).getOrElse(Map.empty)
      activities.map(a => ActivityRenderWithAudience.applyWithAudience(
        activityRender = a,
        audienceSize = audienceSizes(a.activity.id),
        createdBy = users.getOrElse(a.activity.createdBy, User.unknown(a.activity.createdBy)),
        audience = audiences(a.activity.id),
        sentCount = sentCounts(a.activity.id))
      )
    }
  }

  override def getActivityIcon(providerId: String): Option[ActivityIcon] =
    db.withConnection(implicit c => dao.getActivityIcon(providerId))

  override def getActivityMutes(
    activity: Activity,
    tags: Seq[ActivityTag],
    recipients: Set[Usercode],
    now: DateTime = DateTime.now
  ): Seq[ActivityMute] = {
    val recipientsToSend: Set[Usercode] = if (recipients.size < 100) recipients else Set.empty
    val mutes = db.withConnection(implicit c => muteDao.mutesForActivity(activity, recipientsToSend))
    mutes.filterNot(_.expired(now)).filter(_.matchesTags(tags))
  }

  override def getActivityMutesForRecipient(recipient: Usercode, now: DateTime = DateTime.now): Seq[ActivityMuteRender] = {
    val mutes = db.withConnection(implicit c => muteDao.mutesForRecipient(recipient))
    mutes.filterNot(_.expired(now))
  }

  override def save(activityMute: ActivityMuteSave): Either[Seq[ActivityError], String] = {
    if (activityMute.activityType.isEmpty && activityMute.providerId.isEmpty && activityMute.tags.isEmpty) {
      Left(Seq(MuteNoOptions))
    } else {
      Right(db.withConnection(implicit c => muteDao.save(activityMute)))
    }
  }

  override def update(id: String, activityMute: ActivityMuteSave): Either[Seq[ActivityError], Unit] = {
    if (activityMute.activityType.isEmpty && activityMute.providerId.isEmpty && activityMute.tags.isEmpty) {
      Left(Seq(MuteNoOptions))
    } else {
      Right(db.withConnection(implicit c => muteDao.update(id, activityMute)))
    }
  }

  override def expireActivityMute(recipient: Usercode, id: String): Either[Seq[ActivityError], ActivityMuteRender] = {
    db.withConnection(implicit c => muteDao.mutesForRecipient(recipient)).find(_.id == id).map { mute =>
      db.withTransaction(implicit c => muteDao.expire(mute))
      Right(mute)
    }.getOrElse(Left(Seq(MuteDoesNotExist)))
  }

  private def publishJobKey(activityId: String): JobKey =
    new JobKey(activityId, PublishActivityJob.name)

  private def schedulePublishJob(activityId: String, audienceId: String, publishDate: DateTime): Unit = {
    val key = publishJobKey(activityId)

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
    scheduler.deleteJob(publishJobKey(activityId))
  }

  override def allProviders: Seq[(ActivityProvider, Option[ActivityIcon])] = {
    val providers = db.withConnection(implicit c => dao.allProviders)
    providers.map(p => (p, db.withConnection(implicit c => dao.getActivityIcon(p.id))))
  }

  override def getProvider(id: String): Option[ActivityProvider] = {
    db.withConnection(implicit c => dao.getProvider(id))
  }

  override def countNotificationsByPublishersInLast48Hours: Seq[PublisherActivityCount] =
    db.withConnection(implicit c =>
      dao.countNotificationsSinceDateGroupedByPublisher(
        NotificationData.publishNotificationType,
        DateTime.now.minusHours(48)
      )
    )

  override def updateAudienceCount(activityId: String, audienceId: String, recipients: Set[Usercode]): Unit =
    db.withTransaction { implicit c =>
      val audienceSize = audienceDao.getAudience(audienceId) match {
        case Audience.Public => AudienceSize.Public
        case _ => AudienceSize.Finite(recipients.size)
      }
      dao.updateAudienceCount(activityId, audienceSize)
    }

  override def markProcessed(id: String, usercode: Usercode): Unit =
    db.withTransaction(implicit c => recipientDao.markProcessed(id, usercode.string))

  override def getActivitiesForDateTimeRange(from: DateTime, to: DateTime): Seq[Activity] = {
    db.withConnection(implicit c => {
      dao.getActivitiesForDateTimeRange(from, to)
    })
  }

  override def getActivityReadCountSincePublishedDate(activityId: String): Int =
    db.withConnection(implicit c => dao.getActivityReadCountSincePublishedDate(activityId))

  override def getActivitiesForDateTimeRange(interval: Interval): Seq[Activity] = {
    this.getActivitiesForDateTimeRange(interval.getStart, interval.getEnd)
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

  case class InvalidUsercodeAudience(invalidUsercodes: Seq[Usercode]) extends ActivityError {
    def message = s"The request contains one or more invalid usercode: $invalidUsercodes"
  }

  object InvalidJSON extends ActivityError {
    val message = s"The request was not valid JSON"
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

  object MuteNoOptions extends ActivityError {
    val message = "Activity type or provider or at least one tag must be chosen"
  }

  object MuteDoesNotExist extends ActivityError {
    val message = "This activity mute does not exist"
  }

}
