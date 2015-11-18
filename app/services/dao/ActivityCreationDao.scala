package services.dao

import com.google.inject.{ImplementedBy, Inject}
import models.ActivityPrototype
import play.api.db.{Database, NamedDatabase}
import warwick.sso.Usercode

@ImplementedBy(classOf[ActivityCreationDaoImpl])
trait ActivityCreationDao {

  def createActivity(activity: ActivityPrototype, recipients: Set[Usercode], replaces: Seq[String]): String

}

class ActivityCreationDaoImpl @Inject()(
  @NamedDatabase("default") val db: Database,
  activityDao: ActivityDao,
  activityTagDao: ActivityTagDao,
  activityRecipientDao: ActivityRecipientDao
) extends ActivityCreationDao {

  override def createActivity(activity: ActivityPrototype, recipients: Set[Usercode], replaces: Seq[String]): String =
    db.withTransaction { implicit c =>
      val activityId = activityDao.save(activity, replaces)

      activity.tags.foreach {
        case (name, value) => activityTagDao.save(activityId, name, value)
      }

      recipients.foreach(usercode => activityRecipientDao.create(activityId, usercode.string))

      activityId
    }

}
