package services.dao

import java.sql.Connection
import java.util.UUID

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import models.{EAPFeatureRender, EAPFeatureSave}
import org.joda.time.{DateTime, LocalDate}
import views.utils.MarkdownRenderer
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[EAPFeaturesDaoImpl])
trait EAPFeaturesDao {

  def all(implicit c: Connection): Seq[EAPFeatureRender]

  def save(feature: EAPFeatureSave)(implicit c: Connection): String

  def update(id: String, feature: EAPFeatureSave)(implicit c: Connection): Unit

  def delete(id: String)(implicit c: Connection): Unit

  def getById(id: String)(implicit c: Connection): Option[EAPFeatureRender]

}

class EAPFeaturesDaoImpl extends EAPFeaturesDao {

  private val eapFeatureParser =
    get[String]("ID") ~
    get[String]("NAME") ~
    get[Option[LocalDate]]("START_DATE") ~
    get[Option[LocalDate]]("END_DATE") ~
    get[Option[String]]("SUMMARY") ~
    get[Option[String]]("FEEDBACK_URL") map {
      case id ~ name ~ startDate ~ endDate ~ summaryRaw ~ feedbackUrl => EAPFeatureRender(
        id,
        name,
        startDate,
        endDate,
        summaryRaw.map(MarkdownRenderer.renderMarkdown),
        summaryRaw,
        feedbackUrl
      )
    }

  override def all(implicit c: Connection): Seq[EAPFeatureRender] = {
    SQL"SELECT * FROM EAP_FEATURE".as(eapFeatureParser.*)
  }

  override def save(feature: EAPFeatureSave)(implicit c: Connection): String = {
    val id = UUID.randomUUID().toString
    import feature._
    SQL"""
       INSERT INTO EAP_FEATURE (id, name, start_date, end_date, summary, feedback_url)
       VALUES ($id, $name, ${startDate.map(_.toDateTimeAtStartOfDay).orNull[DateTime]}, ${endDate.map(_.toDateTimeAtStartOfDay).orNull[DateTime]}, $summaryRaw, $feedbackUrl)
    """.executeUpdate()
    id
  }

  override def update(id: String, feature: EAPFeatureSave)(implicit c: Connection): Unit = {
    import feature._
    SQL"""
      UPDATE EAP_FEATURE SET
        NAME = $name,
        START_DATE = ${startDate.map(_.toDateTimeAtStartOfDay).orNull[DateTime]},
        END_DATE = ${endDate.map(_.toDateTimeAtStartOfDay).orNull[DateTime]},
        SUMMARY = $summaryRaw,
        FEEDBACK_URL = $feedbackUrl
      WHERE ID = $id
    """.executeUpdate()
  }

  override def delete(id: String)(implicit c: Connection): Unit = {
    SQL"DELETE FROM EAP_FEATURE WHERE ID = $id".executeUpdate()
  }

  override def getById(id: String)(implicit c: Connection): Option[EAPFeatureRender] = {
    SQL"SELECT * FROM EAP_FEATURE WHERE ID = $id".as(eapFeatureParser.singleOpt)
  }

}
