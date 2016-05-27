package controllers.api

import javax.imageio.ImageIO

import com.google.inject.Inject
import controllers.BaseController
import models.API
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Action
import services.{ImageManipulator, NewsImageService, SecurityService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class NewsImagesController @Inject()(
  securityService: SecurityService,
  newsImageService: NewsImageService,
  imageManipulator: ImageManipulator
) extends BaseController {

  import securityService._
  import system.Roles._

  def show(id: String) = Action { request =>
    newsImageService.find(id).map { newsImage =>
      newsImageService.fetchStream(id).map { stream =>
        val targetWidth = request.getQueryString("width").map(_.toInt)

        val enumerator = targetWidth
          .filter(_ < newsImage.width) // Don't up-size images
          .map { targetWidth =>
          val formatName = newsImage.contentType.split('/').last

          val originalImage = ImageIO.read(stream)
          val image = imageManipulator.resizeToWidth(originalImage, targetWidth)
          originalImage.flush()

          Enumerator.outputStream { outputStream =>
            ImageIO.write(image, formatName, outputStream)
            outputStream.close()
          }
        }.getOrElse {
          Enumerator.fromStream(stream)
        }

        Ok.stream(enumerator).withHeaders(
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
