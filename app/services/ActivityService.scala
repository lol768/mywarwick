package services

import com.google.inject.{ImplementedBy, Inject}
import models.{Activity, ActivityPrototype}
import services.dao.{ActivityCreationDao, ActivityDao, ActivityTagDao}

@ImplementedBy(classOf[ActivityServiceImpl])
trait ActivityService {
  def getActivityById(id: String): Option[Activity]

  def save(activity: ActivityPrototype): String
}

class ActivityServiceImpl @Inject()(
  activityCreationDao: ActivityCreationDao,
  activityDao: ActivityDao,
  activityTagDao: ActivityTagDao
) extends ActivityService {
  override def getActivityById(id: String): Option[Activity] = activityDao.getActivityById(id)

  def save(activity: ActivityPrototype): String = {
    val replaceIds = activityTagDao.getActivitiesWithTags(activity.replace, activity.appId)

    activityCreationDao.createActivity(activity, replaceIds)
  }
}

