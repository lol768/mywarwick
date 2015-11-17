package services.dao

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
    if (tags.isEmpty)
      Seq.empty
    else
      db.withConnection { implicit c =>
        val tagParams = tags.zipWithIndex.flatMap {
          case ((name, value), i) => Map(
            s"tagName$i" -> name,
            s"tagValue$i" -> value
          )
        }

        val params = Map(
          "providerId" -> providerId,
          "count" -> tags.size.toString
        ) ++ tagParams

        val placeholders = 0.until(tags.size).map(i => s"({tagName$i}, {tagValue$i})").mkString(", ")

        SQL(s"SELECT ACTIVITY_ID FROM ACTIVITY_TAG JOIN ACTIVITY ON ACTIVITY.ID = ACTIVITY_TAG.ACTIVITY_ID WHERE PROVIDER_ID = {providerId} AND (NAME, VALUE) IN ($placeholders) GROUP BY ACTIVITY_ID HAVING COUNT(*) = {count}")
          .on(params.map(p => NamedParameter(p._1, p._2)).toSeq: _*)
          .as(str("ACTIVITY_ID").*)
      }
  }

}