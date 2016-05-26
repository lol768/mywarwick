package controllers.api

import com.google.inject.Inject
import controllers.BaseController
import models.API
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Action
import services.{NewsImageService, SecurityService}
import system.Roles

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class NewsImagesController @Inject()(
  securityService: SecurityService,
  newsImageService: NewsImageService
) extends BaseController {

  import Roles._
  import securityService._

  def show(id: String) = Action {
    newsImageService.find(id).map { newsImage =>
      newsImageService.fetchStream(id).map { stream =>
        Ok.stream(Enumerator.fromStream(stream))
          .withHeaders(
            CONTENT_DISPOSITION -> "inline",
            CONTENT_LENGTH -> newsImage.contentLength.toString,
            CONTENT_TYPE -> newsImage.contentType
          )
      }.getOrElse(NotFound("Object missing from store"))
    }.getOrElse(NotFound("Image not found"))
  }

  def create = RequiredActualUserRoleAction(Sysadmin)(parse.multipartFormData) { request =>
    request.body.file("image").map { image =>
      newsImageService.put(image.ref.file) match {
        case Success(id) =>
          Created(Json.toJson(API.Success(data = id)))
        case Failure(e) =>
          logger.error("Error creating NewsImage", e)
          InternalServerError(Json.toJson(API.Failure[JsObject]("Internal Server Error", Seq(API.Error("image", e.getMessage)))))
      }
    }.getOrElse {
      BadRequest(Json.toJson(API.Failure[JsObject]("Bad Request", Seq(API.Error("No image", "No image provided")))))
    }
  }

}
