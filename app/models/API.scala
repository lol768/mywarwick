package models

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._
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

  case class Error(id: String, message: String)

  object Error {
    implicit val format = Json.format[Error]
  }

  sealed abstract class Response[A : Reads : Writes](val success: Boolean, status: String) {
    implicit def reads = Response.reads[A]
    implicit def writes = Response.writes[A]
  }
  case class Success[A : Reads : Writes](status: String = "ok", data: A) extends Response[A](true, status)
  case class Failure[A : Reads : Writes](status: String, errors: Seq[Error]) extends Response[A](false, status)

  object Response {
    implicit def reads[A : Reads : Writes]: Reads[Response[A]] = new Reads[Response[A]] {
      override def reads(json: JsValue): JsResult[Response[A]] = {
        val status = (json \ "status").validate[String]
        (json \ "success").validate[Boolean].flatMap { success =>
          if (success) {
            val data = (json \ "data").validate[A]
            (status and data)(Success.apply[A] _)
          } else {
            val errors = (json \ "errors").validate[Seq[Error]]
            (status and errors) (Failure.apply[A] _)
          }
        }
      }
    }

    implicit def writes[A : Reads : Writes]: Writes[Response[A]] = new Writes[Response[A]] {
      override def writes(response: Response[A]): JsValue = response match {
        case Success(status, data) => Json.obj(
          "success" -> true,
          "status" -> status,
          "data" -> data
        )
        case Failure(status, errors) => Json.obj(
          "success" -> false,
          "status" -> status,
          "errors" -> errors
        )
      }
    }
  }


  /**
    * Standard format for a successful API response.
    */
  @deprecated("Use the API.Success object", "0")
  def success(data: (String, JsValueWrapper)*): JsValue =
    Json.obj(
      "success" -> true,
      "status" -> "ok",
      "data" -> Json.obj(data : _*)
    )

  /**
    * Standard format for a failed API response.
    */
  @deprecated("Use the API.Failure object", "0")
  def failure(status: String, data: (String, JsValueWrapper)*): JsValue =
    Json.obj(data : _*) ++
      Json.obj(
        "success" -> false,
        "status" -> status
      )

}
