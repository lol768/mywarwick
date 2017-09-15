package services.elasticsearch

import java.util

import models.Activity
import models.Audience.{DepartmentAudience, ModuleAudience, UsercodeAudience, WebGroupAudience}
import models.publishing.Publisher
import org.elasticsearch.action.get.GetResponse
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
  resolvedUsers: Seq[String] = null
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
      resolvedUsers.map({
        case result: Seq[Usercode] => result.map(_.string)
        case _ => serialiseResolvedUsers(activity.audienceId, audienceService)
      }).orNull
    )
  }

  def fromESSearchResponse(res: SearchResponse): Seq[ActivityDocument] = {
    res.getHits.asScala.toList.map(searchHit => {
      val hitMap = searchHit.getSourceAsMap.asScala.toMap
      val field = ActivityESServiceSearchHelper.ESFieldName
      ActivityDocument(
        hitMap.getOrElse(field.activity_id, "-").toString,
        hitMap.getOrElse(field.provider_id, "-").toString,
        hitMap.getOrElse(field.activity_type, "-").toString,
        hitMap.getOrElse(field.title, "-").toString,
        hitMap.getOrElse(field.url, "-").toString,
        hitMap.getOrElse(field.text, "-").toString,
        hitMap.getOrElse(field.replaced_by, "-").toString,
        DateTime.parse(hitMap.getOrElse(field.published_at.toString, 0).toString), // TODO test if this is right
        hitMap.getOrElse(field.publisher, "-").toString,
        hitMap.getOrElse(field.audience_components, new util.ArrayList()).asInstanceOf[util.ArrayList[String]].asScala.toList.map(_.toString),
        hitMap.getOrElse(field.resolved_users, new util.ArrayList()).asInstanceOf[util.ArrayList[String]].asScala.toList.map(_.toString)
      )
    })
  }

  def serialiseAudienceComponents(audienceId: Option[String], audienceService: AudienceService): Seq[String] = {
    def simpleClassName(o: Object) = o.getClass.getSimpleName.replace("$", "")

    audienceId match {
      case Some(id: String) => audienceService.getAudience(id).components.flatMap {
        case e: UsercodeAudience => Seq(s"${simpleClassName(e)}")
        case e: WebGroupAudience => Seq(s"${simpleClassName(e)}:${e.groupName.string}")
        case e: ModuleAudience => Seq(s"${simpleClassName(e)}:${e.moduleCode}")
        case e: DepartmentAudience => e.subset.map(subset => {
          s"${simpleClassName(e)}:${e.deptCode}:${subset.entryName}"
        })
        case _ => Nil
      }
      case _ => Nil
    }
  }

  def serialiseResolvedUsers(audienceId: Option[String], audienceService: AudienceService): Seq[String] = {
    audienceId match {
      case Some(id: String) => audienceService.resolve(audienceService.getAudience(id))
        .getOrElse(Seq(Usercode("-")))
        .map(_.string)
      case _ => Seq("-")
    }
  }

  def serialisePublisher(publisherId: Option[String], publisherService: PublisherService): String = {
    publisherId match {
      case Some(id: String) => publisherService.find(id) match {
        case Some(e: Publisher) => e.id
        case _ => "-"
      }
      case _ => "-"
    }
  }
}