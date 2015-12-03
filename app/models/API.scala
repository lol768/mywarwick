package models

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsValue, Json, Writes}
import warwick.sso.User

trait APIWriters {

  implicit val userWrite = Writes { user: User =>
    Json.obj(
      "@type" -> "Person",
      "@id" -> user.usercode.string,
      "displayName" -> user.name.full
    )
  }

  implicit val writes = Writes { item: NewsItem =>
    Json.obj(
      "id" -> item.id,
      "title" -> item.title,
      "url" -> Json.obj(
        "href" -> item.url
      ),
      "content" -> item.content,
      "publicationDate" -> item.publicationDate.getMillis,
      "source" -> item.source
    )
  }

}

/**
  * Builder for API response objects.
  */
object API extends APIWriters {

  /**
    * Standard format for a successful API response.
    */
  def success(data: (String, JsValueWrapper)*): JsValue =
    Json.obj(data : _*) ++
    Json.obj(
      "success" -> true,
      "status" -> "ok"
    )

  /**
    * Standard format for a failed API response.
    * TODO include params for standard-format error messages
    */
  def failure(status: String, data: (String, JsValueWrapper)*): JsValue =
    Json.obj(data : _*) ++
      Json.obj(
        "success" -> false,
        "status" -> status
      )

}
