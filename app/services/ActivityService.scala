package services

import com.google.inject.{ImplementedBy, Inject}
import models.{Activity, ActivityPrototype}
import warwick.sso.Usercode

@ImplementedBy(classOf[ActivityServiceImpl])
trait ActivityService {
  def getActivityById(id: String): Option[Activity]

  def getActivitiesForUser(usercode: Usercode): Seq[Activity]

  def save(activity: ActivityPrototype): String
}

class ActivityServiceImpl @Inject()(
  activityTargetService: ActivityTargetService,
  activityCreationDao: ActivityCreationDao,
  activityDao: ActivityDao,
  activityTagDao: ActivityTagDao
) extends ActivityService {

  override def getActivityById(id: String): Option[Activity] = activityDao.getActivityById(id)

  implicit def stringSeqToUsercodeSeq(strings: Seq[String]): Seq[Usercode] = strings.map(Usercode)
  implicit def stringSeqToGroupNameSeq(strings: Seq[String]): Seq[GroupName] = strings.map(GroupName)

  def save(activity: ActivityPrototype): String = {
    val recipients = activityTargetService.getRecipients(
      activity.target.users.getOrElse(Seq.empty),
      activity.target.groups.getOrElse(Seq.empty)
    )

    val replaceIds = activityTagDao.getActivitiesWithTags(activity.replace, activity.appId)

    activityCreationDao.createActivity(activity, recipients, replaceIds)
  }

  override def getActivitiesForUser(usercode: Usercode): Seq[Activity] =
    activityDao.getActivitiesForUser(usercode.string)
}

