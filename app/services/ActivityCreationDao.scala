package services

import com.google.inject.{ImplementedBy, Inject}
import models.IncomingActivity
import play.api.db.{Database, NamedDatabase}

@ImplementedBy(classOf[ActivityCreationDaoImpl])
trait ActivityCreationDao {

  def createActivity(activity: IncomingActivity, replaces: Seq[String], shouldNotify: Boolean): String

}

class ActivityCreationDaoImpl @Inject()(
  @NamedDatabase("default") val db: Database,
  activityDao: ActivityDao,
  activityTagDao: ActivityTagDao
) extends ActivityCreationDao {

  override def createActivity(incomingActivity: IncomingActivity, replaceIds: Seq[String], shouldNotify: Boolean): String = {

    db.withTransaction { implicit c =>
      val activity = activityDao.save(incomingActivity, replaceIds, shouldNotify)

      incomingActivity.tags.foreach {
        case (name, value) => activityTagDao.save(activity, name, value)
      }

      activity
    }

  }
}
