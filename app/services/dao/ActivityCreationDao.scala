package services.dao

import com.google.inject.{ImplementedBy, Inject}
import models.{ActivityResponse, Activity, ActivityPrototype}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import warwick.sso.Usercode

@ImplementedBy(classOf[ActivityCreationDaoImpl])
trait ActivityCreationDao {

  def createActivity(activity: ActivityPrototype, recipients: Set[Usercode], replaces: Seq[String]): ActivityResponse

}

class ActivityCreationDaoImpl @Inject()(
  @NamedDatabase("default") val db: Database,
  activityDao: ActivityDao,
  activityTagDao: ActivityTagDao,
  activityRecipientDao: ActivityRecipientDao
) extends ActivityCreationDao {

  override def createActivity(activity: ActivityPrototype, recipients: Set[Usercode], replaces: Seq[String]): ActivityResponse =
    db.withTransaction { implicit c =>
      val activityId = activityDao.save(activity, replaces)

      activity.tags.foreach {
        tag => activityTagDao.save(activityId, tag)
      }

      recipients.foreach(usercode => activityRecipientDao.create(activityId, usercode.string, activity.generatedAt))

      ActivityResponse(
        Activity(
          id = activityId,
          providerId = activity.providerId,
          `type` = activity.`type`,
          title = activity.title,
          text = activity.text,
          replacedBy = None,
          generatedAt = activity.generatedAt.getOrElse(DateTime.now),
          createdAt = DateTime.now,
          shouldNotify = activity.shouldNotify
        ),
        activity.tags
      )
    }

}
