package services

import com.google.inject.{ImplementedBy, Inject}
import models.{Activity, IncomingActivity}

@ImplementedBy(classOf[ActivityServiceImpl])
trait ActivityService {
  def getActivityById(id: String): Option[Activity]

  def save(incomingActivity: IncomingActivity): String
}

class ActivityServiceImpl @Inject()(
  activityCreationDao: ActivityCreationDao,
  activityDao: ActivityDao,
  activityScopeDao: ActivityScopeDao
) extends ActivityService {
  override def getActivityById(id: String): Option[Activity] = activityDao.getActivityById(id)

  def save(incomingActivity: IncomingActivity): String = {
    import incomingActivity._
    val replaceIds = activityScopeDao.getActivitiesByScope(replace, providerId)

    activityCreationDao.createActivity(incomingActivity, replaceIds)

  }
}

