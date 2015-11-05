package models

import play.api.libs.json.{JsValue, Json, Writes}
import warwick.sso.User

trait APIWriters {
  implicit val userWrite = Writes { user: User => Json.obj(
    "@type" -> "Person",
    "@id" -> user.usercode.string,
    "displayName" -> user.name.full
  )}

  implicit val writes = Writes { item: NewsItem => Json.obj(
    "id" -> item.id,
    "title" -> item.title,
    "url" -> Json.obj(
      "href" -> item.url
    ),
    "content" -> item.content,
    "publicationDate" -> item.publicationDate.getMillis,
    "source" -> item.source
  )}
}

/**
  * Builder for API response objects.
  */
object API extends APIWriters {

  /**
   * This particular method is a bit nonsense, but it does
   * show how we might compose bits of JSON to re-use certain
   * structures like users
   */
  def myActivityResponse(user: User): JsValue = Json.obj(
    "user" -> user,
    "activities" -> activityStream()
  )

  def activityStream(/** TODO arguments */): JsValue = Json.obj(
    "@context" -> "http://www.w3.org/ns/activitystreams"
  )

}
