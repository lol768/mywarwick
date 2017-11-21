package services.elasticsearch

import java.util

import models.Audience._
import models.publishing.Publisher
import models.{Activity, Audience}
import org.elasticsearch.action.search.SearchResponse
import org.joda.time.DateTime
import services.{AudienceService, PublisherService}
import warwick.sso.Usercode

import scala.collection.JavaConverters._

case class ActivityDocument(
  activity_id: String = null,
  provider_id: String = null,
  activity_type: String = null,
  title: String = null,
  url: String = null,
  text: String = null,
  replaced_by: String = null,
  published_at: DateTime = null,
  publisher: String = null,
  audienceComponents: Seq[String] = null,
  resolvedUsers: Seq[String] = null,
  api: Boolean = false,
  created_at: DateTime = null,
  created_by: String = null
)

object ActivityDocument {
  def fromActivityModel(
    activity: Activity,
    audienceService: AudienceService,
    publisherService: PublisherService,
    resolvedUsers: Option[Seq[Usercode]] = None
  ): ActivityDocument = {

    ActivityDocument(
      activity.id,
      activity.providerId,
      activity.`type`,
      activity.title,
      activity.url.orNull,
      activity.text.orNull,
      activity.replacedBy.orNull,
      activity.publishedAt,
      serialisePublisher(activity.publisherId, publisherService),
      serialiseAudienceComponents(activity.audienceId, audienceService),
      resolvedUsers match {
        case e: Some[Seq[Usercode]] => e.map(_.map(_.string)).orNull
        case _ => serialiseResolvedUsers(activity.audienceId, audienceService)
      },
      activity.api,
      activity.createdAt,
      activity.createdBy.string
    )
  }

  def fromESSearchResponse(res: SearchResponse): Seq[ActivityDocument] = {
    res.getHits.asScala.toList.map(searchHit =>
      fromMap(searchHit.getSourceAsMap.asScala.toMap)
    )
  }

  def fromMap(hitMap: Map[String, AnyRef]) = {
    val field = ActivityESServiceSearchHelper.ESFieldName
    val map = hitMap.filterNot { case (_, v) => v == null }

    type SList = util.ArrayList[String]

    def getStringSeq(field: String) =
      map.getOrElse(field, new SList())
        .asInstanceOf[SList]
        .asScala
        .map(_.toString)

    def getString(field: String) =
      map.getOrElse(field, "-").toString

    def getDate(field: String) =
      map.get(field).map(_.toString).map(DateTime.parse).orNull

    def getBoolean(field: String, default: Boolean) =
      Boolean.unbox(map.getOrElse(field, Boolean.box(default)))

    ActivityDocument(
      getString(field.activity_id),
      getString(field.provider_id),
      getString(field.activity_type),
      getString(field.title),
      getString(field.url),
      getString(field.text),
      getString(field.replaced_by),
      getDate(field.published_at),
      getString(field.publisher),
      getStringSeq(field.audience_components),
      getStringSeq(field.resolved_users),
      getBoolean(field.api, default = false),
      getDate(field.created_at),
      getString(field.created_by)
    )
  }

  def serialiseAudienceComponents(audienceId: Option[String], audienceService: AudienceService): Seq[String] = {
    def simpleClassName(o: Object) = o.getClass.getSimpleName.replace("$", "")

    audienceId match {
      case Some(id: String) => audienceService.getAudience(id).components.flatMap {
        case e: WebGroupAudience => Seq(s"${simpleClassName(e)}:${e.groupName.string}")
        case e: ModuleAudience => Seq(s"${simpleClassName(e)}:${e.moduleCode}")
        case e: SeminarGroupAudience => Seq(s"${simpleClassName(e)}:${e.groupId}")
        case e: RelationshipAudience => Seq(s"${simpleClassName(e)}:${e.relationshipType}:${e.agentId.string}")
        case e: DepartmentAudience => e.subset.map(subset => {
          s"${simpleClassName(e)}:${e.deptCode}:${subset.entryName}"
        })
        case e: Audience.Component => Seq(simpleClassName(e))
      }
      case _ => Nil
    }
  }

  def serialiseResolvedUsers(audienceId: Option[String], audienceService: AudienceService): Seq[String] = {
    audienceId match {
      case Some(id: String) => audienceService
        .resolve(audienceService.getAudience(id))
        .map(e => e.toSeq.map(_.string)).recover({ case _ => Seq() }).getOrElse(Seq())
      case _ => Seq()
    }
  }

  def serialisePublisher(publisherId: Option[String], publisherService: PublisherService): String = {
    publisherId match {
      case Some(id: String) => publisherService.find(id) match {
        case Some(e: Publisher) => e.id
        case _ => null
      }
      case _ => null
    }
  }
}