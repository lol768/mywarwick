package services.elasticsearch

import models.Activity
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilder, QueryBuilders}
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.joda.time.{DateTime, Interval}
import play.api.libs.json.{JsValue, Json}

trait ActivityESServiceHelper {

  val activityDocumentType = "activity" // we use the same type for both alert and activity. they are the same structure but in different indexes
  val messageSentDocumentType = "message_send"
  val indexNameForMessageSent = "message_sent"
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
  }

  def dateSuffixString(date: DateTime = DateTime.now()) = s"$separator${date.toString("yyyy_MM")}"

  def indexNameToday(isNotification: Boolean = true): String = {
    isNotification match {
      case true => s"$indexNameForAlert${dateSuffixString()}"
      case false => s"$indexNameForActivity${dateSuffixString()}"
    }
  }

  def indexNameForDateTime(dateTime: DateTime, isNotification: Boolean = true) = {
    isNotification match {
      case true => s"$indexNameForAlert${dateSuffixString(dateTime)}"
      case false => s"$indexNameForActivity${dateSuffixString(dateTime)}"
    }
  }

  def indexNameForActivity(activity: Activity): String = {
    activity.publishedAt match {
      case e: DateTime => indexNameForDateTime(e, activity.shouldNotify)
      case _ => indexNameToday(activity.shouldNotify)
    }
  }

  def indexNameForAllTime(isNotification: Boolean = true): String = {
    isNotification match {
      case true => s"$indexNameForAlert*"
      case false => s"$indexNameForActivity*"
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

  val templatesForActivityAndAlert: JsValue = getTemplatesForActivityAndAlert()
  val messageSentEsTemplates: JsValue = getMessageSentEsTemplates()

  def getTemplatesForActivityAndAlert(
    indexNameForActivity: String = indexNameForActivity,
    indexNameForAlert:String = indexNameForAlert
  ): JsValue = Json.parse({
    s"""
      {
        "index_patterns": ["$indexNameForActivity*", "$indexNameForAlert"],
        "mappings": {
          "activity": {
            "properties": {
              "activity_id": {
                "type": "keyword"
              },
              "activity_type": {
                "type": "keyword"
              },
              "audience_components": {
                "type": "keyword"
              },
              "provider_id": {
                "type": "keyword"
              },
              "published_at": {
                "type": "date"
              },
              "publisher": {
                "type": "keyword"
              },
              "replaced_by": {
                "type": "keyword"
              },
              "resolved_users": {
                "type": "keyword"
              },
              "text": {
                "type": "text"
              },
              "title": {
                "type": "text",
                "fields": {
                  "keyword": {
                    "type": "keyword",
                    "ignore_above": 256
                  }
                }
              },
              "url": {
                "type": "text",
                "fields": {
                  "keyword": {
                    "type": "keyword",
                    "ignore_above": 256
                  }
                }
              }
            }
          }
        }
      }
    """
  })

  def getMessageSentEsTemplates(
    indexNameForMessageSent: String = indexNameForMessageSent
  ): JsValue = Json.parse({
    s"""
      {
        "index_patterns": "$indexNameForMessageSent*",
        "mappings": {
          "message_sent": {
            "properties": {
              "activity_id": {
                "type": "keyword"
              },
              "usercode": {
                "type": "keyword"
              },
              "output": {
                "type": "keyword"
              },
              "state": {
                "type": "keyword"
              }
            }
          }
        }
      }
    """
  })
}

object ActivityESServiceUpdateHelper extends ActivityESServiceHelper {
  def makeUpdateRequest(indexName: String, docType: String, docId: String, docSource: XContentBuilder): UpdateRequest = {
    new UpdateRequest(indexName, docType, docId).doc(docSource)
  }

}

object ActivityESServiceSearchHelper extends ActivityESServiceHelper {

  def indexNameForActivitySearchQuery(query: ActivityESSearchQuery): String = {
    val indexForInterval = query.publish_at match {
      case i: Some[Interval] => i.map(partialIndexNameForInterval).getOrElse("*")
      case _ => "*"
    }

    val indexForActivityType = query.isAlert match {
      case i: Some[Boolean] => i.map(partialIndexNameForActivityType).getOrElse("*")
      case _ => "*"
    }

    (indexForActivityType, indexForInterval) match {
      case ("*", "*") => "*"
      case _ => s"$indexForActivityType$separator$indexForInterval"
    }
  }

  def partialIndexNameForActivityType(isAlert: Boolean): String = {
    isAlert match {
      case true => indexNameForAlert
      case false => indexNameForActivity
    }
  }

  def partialIndexNameForInterval(interval: Interval): String = {
    val start = interval.getStart
    val end = interval.getEnd

    val startYear = interval.getStart.getYear
    val endYear = interval.getEnd.getYear

    if (startYear == endYear) {
      val sameYear = startYear
      val startMonth = start.toString("MM")
      val endMonth = end.toString("MM")

      if (startMonth == endMonth) {
        val sameMonth = startMonth
        s"${sameYear}_${sameMonth}"
      } else {
        s"${sameYear}*"
      }
    } else {
      "*"
    }

  }

  def makeSearchSourceBuilder(queryBuilder: QueryBuilder): SearchSourceBuilder = {
    val searchSourceBuilder = new SearchSourceBuilder()
    searchSourceBuilder.size(10000) //TODO fixme search api is limited to 10000 results, replace with search scroll api
    searchSourceBuilder.query(queryBuilder)
  }

  def makeBoolQueryBuilder(activityESSearchQuery: ActivityESSearchQuery): BoolQueryBuilder = {

    val rootBoolQuery: BoolQueryBuilder = new BoolQueryBuilder()

    import QueryBuilders._
    import ESFieldName._

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