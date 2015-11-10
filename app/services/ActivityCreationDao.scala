package services

import com.google.inject.{ImplementedBy, Inject}
import models.IncomingActivity
import play.api.db.{Database, NamedDatabase}

@ImplementedBy(classOf[ActivityCreationDaoImpl])
trait ActivityCreationDao {

  def createActivity(activity: IncomingActivity, replaces: Seq[String]): String

}

class ActivityCreationDaoImpl @Inject()(
  @NamedDatabase("default") val db: Database,
  activityDao: ActivityDao,
  activityScopeDao: ActivityScopeDao
) extends ActivityCreationDao {

  override def createActivity(incomingActivity: IncomingActivity, replaceIds: Seq[String]): String = {

    db.withTransaction { implicit c =>
      import incomingActivity._
      val activity = activityDao.save(incomingActivity, replaceIds)(c)

      scopes.foreach {
        case (name, value) => activityScopeDao.save(activity, name, value)(c)
      }

      activity
    }

  }
}
