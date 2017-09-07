package services.elasticSearch

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.Audience.{DepartmentAudience, ModuleAudience, UsercodeAudience, WebGroupAudience}
import models.{Activity, Audience}
import models.publishing.Publisher
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.index.{IndexRequest, IndexResponse}
import org.joda.time.DateTime
import services.{AudienceService, PublisherService}

@ImplementedBy(classOf[ActivityESServiceImpl])
trait ActivityESService {
  def index(activity: Activity)
}

@Singleton
class ActivityESServiceImpl @Inject()(
  eSClientConfig: ESClientConfig,
  audienceService: AudienceService,
  publisherService: PublisherService
) extends ActivityESService {

  private val client = eSClientConfig.newClient

  def indexNameToday(isNotification: Boolean): String = {
    val today = DateTime.now().toString("yyyy_0:MM")
    isNotification match {
      case true => s"""alert_$today"""
      case false => s"""activity_$today"""
    }
  }

  override def index(activity: Activity): Unit = {
    val id = activity.id
    val providerId = activity.providerId
    val activityType = activity.`type`
    val title = activity.title
    val text = activity.text.getOrElse("-")
    val url = activity.url.getOrElse("-")
    val replacedBy = activity.replacedBy.getOrElse("-")
    val publishedAt = activity.publishedAt.toDate

    val audienceComponents: Seq[AudienceComponent] = activity.audienceId match {
      case Some(id: String) => audienceService.getAudience(id).components.map {
        case e: UsercodeAudience => AudienceComponent("usercode")
        case e: WebGroupAudience => AudienceComponent("webgroup", e.groupName.string)
        case e: ModuleAudience => AudienceComponent("module", e.moduleCode)
        case e: DepartmentAudience => AudienceComponent("department", e.deptCode, e.subset.map(_.entryName.toString))
        case _ => AudienceComponent()
      }
      case _ => Seq(AudienceComponent())
    }

    val usersInAudience: Seq[String] = activity.audienceId match {
      case Some(id: String) => audienceService.resolve(audienceService.getAudience(id))
        .getOrElse(Seq("-"))
        .map(_.toString.replace("Usercode(", "").replace(")", ""))
      case _ => Seq("-")
    }

    val publisher: String = activity.publisherId match {
      case Some(id: String) => publisherService.find(id) match {
        case Some(e: Publisher) => e.id
        case _ => "-"
      }
      case _ => "-"
    }

    import org.elasticsearch.common.xcontent.XContentFactory._
    val builder = jsonBuilder().startObject()
    builder
      .field("activity_id", id)
      .field("provider_id", providerId)
      .field("type", activityType)
      .field("title", title)
      .field("url", url)
      .field("test", text)
      .field("replaced_by", replacedBy)
      .field("published_at", publishedAt)
      .field("publisher", publisher)

    builder.startArray("audience_users")
    usersInAudience.foreach(builder.value)
    builder.endArray()

    builder.startArray("audience_components")
    audienceComponents.foreach(component => {
      builder.startObject()
      builder.field("type", component.kind)
      builder.field("name", component.name)
      if (component.departmentSubSet != Nil) {
        builder.startArray("department_subset")
        component.departmentSubSet.foreach(builder.value)
        builder.endArray()
      }
      builder.endObject()
    })
    builder.endArray()

    //    builder.startArray("audience_components")
    //    audienceComponents.foreach(builder.value)
    //    builder.endArray()

    builder.endObject()

    val indexRequest = new IndexRequest(indexNameToday(activity.shouldNotify), activityType, id).source(builder)

    client.indexAsync(indexRequest, new ActionListener[IndexResponse] {
      override def onFailure(e: Exception) = {
        println("error sending activity to es")
        println(e)
      }

      override def onResponse(response: IndexResponse) = {
        println("response from es")
        println(response.toString)
      }
    })
  }
}

sealed case class AudienceComponent(kind: String = "-", name: String = "-", departmentSubSet: Seq[String] = Nil)