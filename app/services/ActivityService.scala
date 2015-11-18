package services

import com.google.inject.{ImplementedBy, Inject}
import models.{Activity, ActivityPrototype}
import services.dao.{ActivityCreationDao, ActivityDao, ActivityTagDao}
import warwick.sso.Usercode

import scala.util.{Success, Failure, Try}

@ImplementedBy(classOf[ActivityServiceImpl])
trait ActivityService {
  def getActivityById(id: String): Option[Activity]

  def getActivitiesForUser(usercode: Usercode): Seq[Activity]

  def save(activity: ActivityPrototype): Try[String]
}

class ActivityServiceImpl @Inject()(
  activityRecipientService: ActivityRecipientService,
  activityCreationDao: ActivityCreationDao,
  activityDao: ActivityDao,
  activityTagDao: ActivityTagDao
) extends ActivityService {

  override def getActivityById(id: String): Option[Activity] = activityDao.getActivityById(id)

  implicit def stringSeqToUsercodeSeq(strings: Seq[String]): Seq[Usercode] = strings.map(Usercode)
  implicit def stringSeqToGroupNameSeq(strings: Seq[String]): Seq[GroupName] = strings.map(GroupName)

  def save(activity: ActivityPrototype): Try[String] = {
    val recipients = activityRecipientService.getRecipientUsercodes(
      activity.recipients.users.getOrElse(Seq.empty),
      activity.recipients.groups.getOrElse(Seq.empty)
    )

    if (recipients.isEmpty) {
      Failure(new NoRecipientsException)
    } else {
      val replaceIds = activityTagDao.getActivitiesWithTags(activity.replace, activity.appId)

      val activityId = activityCreationDao.createActivity(activity, recipients, replaceIds)

      Success(activityId)
    }

  }

  override def getActivitiesForUser(usercode: Usercode): Seq[Activity] =
    activityDao.getActivitiesForUser(usercode.string)
}

class NoRecipientsException extends Throwable
