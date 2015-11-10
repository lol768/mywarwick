package services

import java.sql.Connection
import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models.{Activity, IncomingActivity}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[ActivityDaoImpl])
trait ActivityDao {
  def save(incomingActivity: IncomingActivity,
    replaces: Seq[String])(implicit connection: Connection): String

  def getActivityById(id: String): Option[Activity] =
    getActivitiesByIds(Seq(id)).headOption

  def getActivitiesByIds(ids: Seq[String]): Seq[Activity]

}

class ActivityDaoImpl @Inject()(@NamedDatabase("default") val db: Database) extends ActivityDao {

  private def activityParser: RowParser[Activity] = {
    get[String]("id") ~
      get[String]("provider_id") ~
      get[String]("type") ~
      get[String]("title") ~
      get[String]("text") ~
      get[String]("replaced_by_id") ~
      get[DateTime]("created_at") ~
      get[Boolean]("should_notify") map {
      case id ~ providerId ~ activityType ~ title ~ text ~ replacedById ~ createdAt ~ shouldNotify =>
        Activity(id, providerId, activityType, title, text, replacedById, createdAt, shouldNotify)
    }
  }

  override def save(incomingActivity: IncomingActivity, replaces: Seq[String])(implicit c: Connection): String = {
    import incomingActivity._
    val id = UUID.randomUUID().toString
    val now = new DateTime()

    SQL("INSERT INTO ACTIVITY (id, provider_id, type, title, text, generated_at, created_at) VALUES({id}, {providerId}, {type}, {title}, {text}, {generatedAt}, {createdAt})")
      .on(
        'id -> id,
        'providerId -> providerId,
        'type -> activityType,
        'title -> title,
        'text -> text,
        'generatedAt -> generatedAt.getOrElse(now),
        'createdAt -> now
      )
      .execute()

    updateReplacedActivity(id, replaces)

    id
  }

  def updateReplacedActivity(replacedById: String, replaces: Seq[String]) = {
    db.withConnection { implicit c =>
      replaces.grouped(1000).foreach { group =>
        SQL("UPDATE ACTIVITY SET replaced_by_id = {replacedById} WHERE id IN ({replaces})")
          .on('replacedById -> replacedById,
            'replaces -> group)
          .execute()
      }
    }
  }

  def getActivitiesByIds(ids: Seq[String]): Seq[Activity] = {
    db.withConnection { implicit c =>
      SQL(s"SELECT * FROM ACTIVITY WHERE id IN ({ids})")
        .on('ids -> ids)
        .as(activityParser.*)
    }
  }
}
