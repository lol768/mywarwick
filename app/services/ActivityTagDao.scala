package services

import java.sql.Connection
import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[ActivityTagDaoImpl])
trait ActivityTagDao {

  def save(activityId: String, name: String, value: String)(implicit connection: Connection): String

  def getActivitiesWithTags(tags: Map[String, String], providerId: String): Seq[String]

}

class ActivityTagDaoImpl @Inject()(@NamedDatabase("default") val db: Database) extends ActivityTagDao {

  override def save(activityId: String, name: String, value: String)(implicit c: Connection): String = {
    val tagId = UUID.randomUUID().toString

    SQL("INSERT INTO ACTIVITY_TAG (ACTIVITY_ID, ID, NAME, VALUE, CREATED_AT) VALUES ({activityId}, {id}, {name}, {value}, {createdAt})")
      .on(
        'activityId -> activityId,
        'id -> tagId,
        'name -> name,
        'value -> value,
        'createdAt -> DateTime.now()
      )
      .execute()

    tagId
  }

  override def getActivitiesWithTags(tags: Map[String, String], providerId: String): Seq[String] = {
    db.withConnection { implicit c =>
      /*
      val tagsParam = SeqParameter(
        seq = tags.map { case (name, value) =>
            // s"$name, $value"
           Vector(name, value)
        }.toSeq,
        pre = "(",
        post = ")"
      )

      SQL("SELECT ACTIVITY_ID FROM ACTIVITY_TAG JOIN ACTIVITY ON ACTIVITY.ID = ACTIVITY_TAG.ACTIVITY_ID WHERE (NAME, VALUE) IN ({tags}) AND PROVIDER_ID = {providerId} GROUP BY ACTIVITY_ID HAVING COUNT(*) = {count}")
        .on(
          'tags -> tagsParam,
          'providerId -> providerId,
          'count -> tags.size
        )
        .as(str("ACTIVITY_ID").*)
      */

      // TODO fix
      SQL("SELECT ACTIVITY_ID FROM ACTIVITY_TAG JOIN ACTIVITY ON ACTIVITY.ID = ACTIVITY_TAG.ACTIVITY_ID WHERE (VALUE) IN ({tagValues}) AND PROVIDER_ID = {providerId} GROUP BY ACTIVITY_ID HAVING COUNT(*) = {count}")
        .on(
          'tagValues -> tags.values.toSeq,
          'providerId -> providerId,
          'count -> tags.size
        )
        .as(str("ACTIVITY_ID").*)
    }
  }

}