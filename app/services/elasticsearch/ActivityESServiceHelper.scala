package services.elasticsearch

import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilder, QueryBuilders}
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.joda.time.DateTime

trait ActivityESServiceHelper {

  val documentType = "activity" // we use the same type for both alert and activity. they are the same structure but in different indexes
  val nameForAlert = "alert"
  val nameForActivity = "activity"
  val separator = "_"

  object ESFieldName {
    val activity_id = "activity_id"
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

  def hyphenToUnderscore(in: String) = in.replace("-", "_")

  def indexNameToday(isNotification: Boolean = true, today: String = DateTime.now().toString("yyyy_MM")): String = {
    isNotification match {
      case true => s"$nameForAlert$separator$today"
      case false => s"$nameForActivity$separator$today"
    }
  }

  def indexNameForAllTime(isNotification: Boolean = true): String = {
    isNotification match {
      case true => s"$nameForAlert*"
      case false => s"$nameForActivity*"
    }
  }

  def elasticSearchContentBuilderFromActivityDocument(activityDocument: ActivityDocument): XContentBuilder = {
    import org.elasticsearch.common.xcontent.XContentFactory._
    val builder: XContentBuilder = jsonBuilder().startObject()

    builder
      .field(ESFieldName.activity_id, hyphenToUnderscore(activityDocument.activity_id))
      .field(ESFieldName.provider_id, hyphenToUnderscore(activityDocument.provider_id))
      .field(ESFieldName.activity_type, hyphenToUnderscore(activityDocument.activity_type))
      .field(ESFieldName.title, hyphenToUnderscore(activityDocument.title))
      .field(ESFieldName.url, hyphenToUnderscore(activityDocument.url))
      .field(ESFieldName.text, hyphenToUnderscore(activityDocument.text))
      .field(ESFieldName.replaced_by, hyphenToUnderscore(activityDocument.replaced_by))
      .field(ESFieldName.published_at, activityDocument.published_at.toDate)
      .field(ESFieldName.publisher, hyphenToUnderscore(activityDocument.publisher))

    builder.startArray(ESFieldName.resolved_users)
    activityDocument.resolvedUsers.foreach(e => builder.value(hyphenToUnderscore(e)))
    builder.endArray()

    builder.startArray(ESFieldName.audience_components)
    activityDocument.audienceComponents.foreach(e => builder.value(hyphenToUnderscore(e)))
    builder.endArray()

    builder.endObject()
    builder
  }
}

object ActivityESServiceUpdateHelper extends ActivityESServiceHelper {
  def makeUpdateRequest(indexName: String, docType: String, docId: String, docSource: XContentBuilder): UpdateRequest = {
    new UpdateRequest(indexName, docType, docId).doc(docSource)
  }

}

object ActivityESServiceDeleteHelper extends ActivityESServiceHelper

object ActivityESServiceSearchHelper extends ActivityESServiceHelper {

  def makeSearchSourceBuilder(queryBuilder: QueryBuilder): SearchSourceBuilder = {
    val searchSourceBuilder = new SearchSourceBuilder()
    searchSourceBuilder.query(queryBuilder)
  }

  def makeBoolQueryBuilder(activityESSearchQuery: ActivityESSearchQuery): BoolQueryBuilder = {

    val boolQueryBuilder: BoolQueryBuilder = new BoolQueryBuilder()

    activityESSearchQuery.activity_id match {
      case Some(activity_id) => boolQueryBuilder.must(QueryBuilders.termQuery(ESFieldName.activity_id, hyphenToUnderscore(activity_id)))
      case _ =>
    }

    activityESSearchQuery.provider_id match {
      case Some(provider_id) => boolQueryBuilder.must(QueryBuilders.termQuery(ESFieldName.provider_id, hyphenToUnderscore(provider_id)))
      case _ =>
    }

    activityESSearchQuery.activity_type match {
      case Some(activity_type) => boolQueryBuilder.must(QueryBuilders.termQuery(ESFieldName.activity_type, hyphenToUnderscore(activity_type)))
      case _ =>
    }

    activityESSearchQuery.publish_at match {
      case Some(dateRange) => boolQueryBuilder.must(QueryBuilders.rangeQuery(ESFieldName.published_at).gte(dateRange.from.toString()).lte(dateRange.to.toString()))
      case _ =>
    }

    activityESSearchQuery.publisher match {
      case Some(publisher) => boolQueryBuilder.must(QueryBuilders.termQuery(ESFieldName.publisher, hyphenToUnderscore(publisher)))
      case _ =>
    }

    activityESSearchQuery.text match {
      case Some(text) => boolQueryBuilder.must(QueryBuilders.matchQuery(ESFieldName.text, text))
      case _ =>
    }

    activityESSearchQuery.title match {
      case Some(title) => boolQueryBuilder.must(QueryBuilders.matchQuery(ESFieldName.title, title))
      case _ =>
    }

    activityESSearchQuery.url match {
      case Some(url) => boolQueryBuilder.must(QueryBuilders.termQuery(ESFieldName.url, url))
      case _ =>
    }

    activityESSearchQuery.audienceComponents match {
      case Some(components) =>
        components.foreach(component => {
          boolQueryBuilder.should(QueryBuilders.matchQuery(ESFieldName.audience_components, component))
        })
      case _ =>
    }

    activityESSearchQuery.resolvedUsers match {
      case Some(resolvedUsers) =>
        resolvedUsers.foreach(resolvedUser => {
          boolQueryBuilder.should(QueryBuilders.termQuery(ESFieldName.resolved_users, resolvedUser))
        })
      case _ =>
    }
    boolQueryBuilder
  }

}

object ActivityESServiceIndexHelper extends ActivityESServiceHelper {

  def makeIndexRequest(indexName: String, docType: String, docId: String, docSource: XContentBuilder): IndexRequest = {
    new IndexRequest(indexName, docType, docId).source(docSource)
  }
}

object ActivityESServiceHelper extends ActivityESServiceHelper