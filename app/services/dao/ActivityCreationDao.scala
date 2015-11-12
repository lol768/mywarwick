package services.dao

import com.google.inject.{ImplementedBy, Inject}
import models.ActivityPrototype
import play.api.db.{Database, NamedDatabase}

@ImplementedBy(classOf[ActivityCreationDaoImpl])
trait ActivityCreationDao {

  def createActivity(activity: ActivityPrototype, replaces: Seq[String]): String

}

class ActivityCreationDaoImpl @Inject()(
  @NamedDatabase("default") val db: Database,
  activityDao: ActivityDao,
  activityTagDao: ActivityTagDao
) extends ActivityCreationDao {

  override def createActivity(activity: ActivityPrototype, replaceIds: Seq[String]): String =
    db.withTransaction { implicit c =>
      val activityId = activityDao.save(activity, replaceIds)

      activity.tags.foreach {
        case (name, value) => activityTagDao.save(activityId, name, value)
      }

      activityId
    }

}
