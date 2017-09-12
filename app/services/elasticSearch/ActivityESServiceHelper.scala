package services.elasticSearch

import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.common.xcontent.XContentBuilder
import org.joda.time.DateTime

trait ActivityESServiceHelper {

  val documentType = "activity" // we use the same type for both alert and activity. they are the same structure but in different indexes
  val nameForAlert = "alert"
  val nameForActivity = "activity"
  val separator = "_"

  object ESFieldName {
    val provider_id = "provider_id"
    val activity_type = "activity_type"
    val title = "title"
    val url = "url"
    val text = "text"
    val replaced_by = "replaced_by"
    val published_at = "published_at"
    val publisher = "publisher"
    val resolved_users = "resolved_users"
    val audience_components = "audience_components"
  }

  def indexNameToday(isNotification: Boolean = true, today: String = DateTime.now().toString("yyyy_MM")): String = {
    isNotification match {
      case true => s"""$nameForAlert$separator$today"""
      case false => s"""$nameForActivity$separator$today"""
    }
  }

  def indexNameForAllTime(isNotification: Boolean = true): String = {
    isNotification match {
      case true => s"""$nameForAlert*"""
      case false => s"""$nameForActivity*"""
    }
  }
}

object ActivityESServiceGetHelper extends ActivityESServiceHelper

object ActivityESServiceDeleteHelper extends ActivityESServiceHelper

object ActivityESServiceSearchHelper extends ActivityESServiceHelper

object ActivityESServiceIndexHelper extends ActivityESServiceHelper {

  def makeIndexRequest(indexName: String, docType: String, docId: String, docSource: XContentBuilder): IndexRequest = {
    new IndexRequest(indexName, docType, docId).source(docSource)
  }

  def makeIndexDocBuilder(activityDocument: ActivityDocument): XContentBuilder = {
    import org.elasticsearch.common.xcontent.XContentFactory._
    val builder: XContentBuilder = jsonBuilder().startObject()

    builder
      .field(ESFieldName.provider_id, activityDocument.provider_id)
      .field(ESFieldName.activity_type, activityDocument.activity_type)
      .field(ESFieldName.title, activityDocument.title)
      .field(ESFieldName.url, activityDocument.url)
      .field(ESFieldName.text, activityDocument.text)
      .field(ESFieldName.replaced_by, activityDocument.replaced_by)
      .field(ESFieldName.published_at, activityDocument.published_at)
      .field(ESFieldName.publisher, activityDocument.publisher)

    builder.startArray(ESFieldName.resolved_users)
    activityDocument.resolvedUsers.foreach(builder.value)
    builder.endArray()

    builder.startArray(ESFieldName.audience_components)
    activityDocument.audienceComponents.foreach(builder.value)
    builder.endArray()

    builder.endObject()
    builder
  }

}