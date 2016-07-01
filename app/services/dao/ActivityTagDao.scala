package services.dao

import java.sql.Connection
import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import models.ActivityTag
import org.joda.time.DateTime
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[ActivityTagDaoImpl])
trait ActivityTagDao {

  def save(activityId: String, tag: ActivityTag)(implicit c: Connection): String

  def getActivitiesWithTags(tags: Map[String, String], providerId: String)(implicit c: Connection): Seq[String]

}

class ActivityTagDaoImpl extends ActivityTagDao {

  override def save(activityId: String, tag: ActivityTag)(implicit c: Connection): String = {
    val tagId = UUID.randomUUID().toString

    SQL"INSERT INTO ACTIVITY_TAG (ACTIVITY_ID, ID, NAME, VALUE, DISPLAY_VALUE, CREATED_AT) VALUES ($activityId, $tagId, ${tag.name}, ${tag.value.internalValue}, ${tag.value.displayValue}, ${DateTime.now})"
      .execute()

    tagId
  }

  override def getActivitiesWithTags(tags: Map[String, String], providerId: String)(implicit c: Connection): Seq[String] = {
    if (tags.isEmpty) {
      Seq.empty
    } else {
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