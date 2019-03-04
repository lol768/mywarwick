package services.elasticsearch

import models.Activity
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilder, QueryBuilders}
import org.elasticsearch.script.Script
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.joda.time.{DateTime, Interval}
import play.api.libs.json.{JsValue, Json}

trait ActivityESServiceHelper {

  val activityDocumentType = "activity" // we use the same type for both alert and activity. they are the same structure but in different indexes
  val deliveryReportDocumentType = "delivery_report"
  val indexNameForDeliveryReport: String = deliveryReportDocumentType
  val indexNameForAlert = "alert"
  val indexNameForActivity = "activity"
  val separator = "_"

  object ESFieldName {
    val activity_id = "activity_id"
    val activity_id_keyword = s"$activity_id.keyword"
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
    val api = "api"
    val created_at = "created_at"
    val created_by = "created_by"

    val usercode = "usercode"
    val usercode_keyword = s"$usercode.keyword"
    val state = "state"
    val state_keyword = s"$state.keyword"
    val output = "output"
    val distinct_users_agg = "distinct_users"

    val timestamp = "@timestamp"
  }

  def dateSuffixString(date: DateTime = DateTime.now()) = s"$separator${date.toString("yyyy_MM")}"

  def indexNameToday(isNotification: Boolean = true): String = {
    if (isNotification) {
      s"$indexNameForAlert${dateSuffixString()}"
    } else {
      s"$indexNameForActivity${dateSuffixString()}"
    }
  }

  def indexNameForDateTime(dateTime: DateTime, isNotification: Boolean = true): String = {
    if (isNotification) {
      s"$indexNameForAlert${dateSuffixString(dateTime)}"
    } else {
      s"$indexNameForActivity${dateSuffixString(dateTime)}"
    }
  }

  def indexNameForActivity(activity: Activity): String = {
    activity.publishedAt match {
      case e: DateTime => indexNameForDateTime(e, activity.shouldNotify)
      case _ => indexNameToday(activity.shouldNotify)
    }
  }

  def indexNameForAllTime(isNotification: Boolean = true): String = {
    if (isNotification) {
      s"$indexNameForAlert*"
    } else {
      s"$indexNameForActivity*"
    }
  }

  def elasticSearchContentBuilderFromActivityDocument(activityDocument: ActivityDocument): XContentBuilder = {
    import org.elasticsearch.common.xcontent.XContentFactory._
    val builder: XContentBuilder = jsonBuilder().startObject()

    builder
      .field(ESFieldName.activity_id, activityDocument.activity_id)
      .field(ESFieldName.provider_id, activityDocument.provider_id)
      .field(ESFieldName.activity_type, activityDocument.activity_type)
      .field(ESFieldName.title, activityDocument.title)
      .field(ESFieldName.url, activityDocument.url)
      .field(ESFieldName.text, activityDocument.text)
      .field(ESFieldName.replaced_by, activityDocument.replaced_by)
      .field(ESFieldName.published_at, activityDocument.published_at.toDate)
      .field(ESFieldName.publisher, activityDocument.publisher)

    builder.startArray(ESFieldName.resolved_users)
    if (activityDocument.resolvedUsers != null) {
      activityDocument.resolvedUsers.foreach(builder.value)
    } else {
      builder.value(Nil)
    }
    builder.endArray()


    builder.startArray(ESFieldName.audience_components)
    if (activityDocument.audienceComponents != null) {
      activityDocument.audienceComponents.foreach(builder.value)
    } else {
      builder.value(Nil)
    }
    builder.endArray()

    builder.field(ESFieldName.api, activityDocument.api)
      .field(ESFieldName.created_at, activityDocument.created_at.toDate)
      .field(ESFieldName.created_by, activityDocument.created_by)

    builder.endObject()
    builder
  }

  private val typeKeyword: JsValue = Json.obj("type" -> "keyword")
  private val typeText: JsValue = Json.obj("type" -> "text")
  private val typeDate: JsValue = Json.obj("type" -> "date")
  private val propsBoilerplate: JsValue = Json.obj(
    "type" -> "text",
    "fields" -> Json.obj(
      "keyword" -> Json.obj(
        "type" -> "keyword",
        "ignore_above" -> 256
      )
    )
  )

  val templatesForActivityAndAlert: JsValue = Json.obj(
    "index_patterns" -> Json.arr(s"$indexNameForActivity*", s"$indexNameForAlert*"),
    "mappings" -> Json.obj(
      "activity" -> Json.obj(
        "properties"-> Json.obj(
          "activity_id" -> typeKeyword,
          "activity_type" -> typeKeyword,
          "audience_components" -> typeKeyword,
          "provider_id" -> typeKeyword,
          "published_at" -> typeDate,
          "publisher" -> typeKeyword,
          "replaced_by" -> typeKeyword,
          "resolved_users" -> typeKeyword,
          "text" -> typeText,
          "title" -> propsBoilerplate,
          "url" -> propsBoilerplate
        )
      )
    )
  )
  
  val templatesForDeliveryReports: JsValue = Json.obj(
    "index_patterns" -> s"$indexNameForDeliveryReport*",
    "mappings" -> Json.obj(
      deliveryReportDocumentType -> Json.obj(
        "properties" -> Json.obj(
          "activity_id" -> propsBoilerplate,
          "usercode" -> propsBoilerplate,
          "output" -> propsBoilerplate,
          "state" -> propsBoilerplate
        )
      )
    )
  )
}

case class AlertDeliveryReport(successful: Option[Int])
object AlertDeliveryReport {
  def empty = AlertDeliveryReport(None)
}

object ActivityESServiceUpdateHelper extends ActivityESServiceHelper {
  def makeUpdateRequest(indexName: String, docType: String, docId: String, docSource: XContentBuilder): UpdateRequest = {
    new UpdateRequest(indexName, docType, docId).doc(docSource)
  }

}

object ActivityESServiceCountHelper extends ActivityESServiceHelper {

  object Aggregation {
    object TotalUserCount {
      lazy val fieldName = "totalUserCount"
      lazy val builder: SumAggregationBuilder = AggregationBuilders
        .sum(fieldName)
        .script(new Script("doc['resolved_users'].values.length"))
    }
  }
}

object ActivityESServiceSearchHelper extends ActivityESServiceHelper {

  def indexNameForActivitySearchQuery(query: ActivityESSearch.SearchQuery): String = {
    val indexForInterval: Seq[String] = query.publish_at match {
      case i: Some[Interval] => i.map(partialIndexNameForInterval).getOrElse(Seq("*"))
      case _ => Seq("*")
    }

    val indexForActivityType = query.isAlert match {
      case i: Some[Boolean] => i.map(partialIndexNameForActivityType).getOrElse("*")
      case _ => "*"
    }

    (indexForActivityType, indexForInterval) match {
      case ("*", "*" :: Nil) => "*"
      case _ => indexForInterval.map(index => s"$indexForActivityType$separator$index").mkString(",")
    }
  }

  def partialIndexNameForActivityType(isAlert: Boolean): String = {
    if (isAlert) {
      indexNameForAlert
    } else {
      indexNameForActivity
    }
  }

  def partialIndexNameForInterval(interval: Interval): Seq[String] = {
    val start: DateTime = interval.getStart
    val isSameYear: Boolean = start.getYear == interval.getEnd.getYear
    val isSameMonth: Boolean = start.getMonthOfYear == interval.getEnd.getMonthOfYear

    if (isSameYear && isSameMonth) {
      Seq(s"${start.getYear}_${start.toString("MM")}")
    } else {
      val startMonth: DateTime = start.withDayOfMonth(1)
      val endMonth: DateTime = interval.getEnd.withDayOfMonth(1)
      Iterator.iterate(startMonth) {
        _.plusMonths(1)
      }.takeWhile(!_.isAfter(endMonth))
        .map(d => s"${d.getYear}_${"%02d".format(d.getMonthOfYear)}")
        .toSeq
    }
  }

  def makeSearchSourceBuilder(queryBuilder: QueryBuilder): SearchSourceBuilder = {
    val searchSourceBuilder = new SearchSourceBuilder()
    searchSourceBuilder.size(10000) //TODO fixme search api is limited to 10000 results, replace with search scroll api
    searchSourceBuilder.query(queryBuilder)
  }

  def makeBoolQueryBuilder(activityESSearchQuery: ActivityESSearch.SearchQuery): BoolQueryBuilder = {

    val rootBoolQuery: BoolQueryBuilder = new BoolQueryBuilder()

    import ESFieldName._
    import QueryBuilders._

    for (v <- activityESSearchQuery.activity_id) rootBoolQuery.must(termQuery(activity_id, v))

    for (v <- activityESSearchQuery.provider_id) rootBoolQuery.must(termQuery(provider_id, v))

    for (v <- activityESSearchQuery.activity_type) rootBoolQuery.must(termQuery(activity_type, v))

    for (v <- activityESSearchQuery.publish_at) rootBoolQuery.must(rangeQuery(published_at).gte(v.getStart.toString()).lte(v.getEnd.toString()))

    for (v <- activityESSearchQuery.publisher) rootBoolQuery.must(termQuery(publisher, v))

    for (v <- activityESSearchQuery.text) rootBoolQuery.must(matchQuery(text, v))

    for (v <- activityESSearchQuery.title) rootBoolQuery.must(matchQuery(title, v))

    for (v <- activityESSearchQuery.url) rootBoolQuery.must(termQuery(url, v))

    for (v <- activityESSearchQuery.audienceComponents) {
      val componentBoolQueryBuilder: BoolQueryBuilder = new BoolQueryBuilder()
      v.foreach(component => componentBoolQueryBuilder.should(matchQuery(audience_components, component)))
      rootBoolQuery.must(componentBoolQueryBuilder)
    }

    for (v <- activityESSearchQuery.resolvedUsers) {
      val resolvedUsersBoolQueryBuilder: BoolQueryBuilder = new BoolQueryBuilder()
      v.foreach(resolvedUser => resolvedUsersBoolQueryBuilder.should(termQuery(resolved_users, resolvedUser)))
      rootBoolQuery.must(resolvedUsersBoolQueryBuilder)
    }

    rootBoolQuery
  }

}

object ActivityESServiceIndexHelper extends ActivityESServiceHelper {

  def makeIndexRequest(indexName: String, docType: String, docId: String, docSource: XContentBuilder): IndexRequest = {
    new IndexRequest(indexName, docType, docId).source(docSource)
  }
}

object ActivityESServiceHelper extends ActivityESServiceHelper
