package controllers.api

import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

import com.google.common.io.ByteStreams
import com.google.inject.Inject
import controllers.BaseController
import models.API
import play.api.cache.CacheApi
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, MultipartFormData, Request}
import services.{ImageManipulator, NewsImageService, SecurityService}

import scala.util.{Failure, Success}

class NewsImagesController @Inject()(
  securityService: SecurityService,
  newsImageService: NewsImageService,
  imageManipulator: ImageManipulator,
  cache: CacheApi
) extends BaseController {

  import securityService._
  import system.Roles._

  def show(id: String) = Action { request =>
    newsImageService.find(id).map { newsImage =>
      val targetWidth = request.getQueryString("width").map(_.toInt).filter(_ < newsImage.width)
      val cacheKey = targetWidth.map(w => s"$id@$w").getOrElse(id)

      cache
        .getOrElse(cacheKey) {
          val byteArray = newsImageService.fetchStream(id).map { stream =>
            targetWidth.map { targetWidth =>
              val formatName = newsImage.contentType.split('/').last

              val originalImage = ImageIO.read(stream)
              val image = imageManipulator.resizeToWidth(originalImage, targetWidth)
              originalImage.flush()

              val outputStream = new ByteArrayOutputStream(newsImage.contentLength)
              ImageIO.write(image, formatName, outputStream)
              outputStream.toByteArray
            }.getOrElse {
              ByteStreams.toByteArray(stream)
            }
          }

          cache.set(cacheKey, byteArray)
          byteArray
        }
        .map { byteArray =>
          Ok(byteArray).withHeaders(
            CONTENT_DISPOSITION -> "inline",
            CONTENT_LENGTH -> byteArray.length.toString,
            CONTENT_TYPE -> newsImage.contentType
          )
        }.getOrElse(NotFound("Object missing from store"))
    }.getOrElse(NotFound("Image not found"))
  }

  private val MEGABYTE = 1000 * 1000

  def create = RequiredActualUserRoleAction(Sysadmin)(parse.multipartFormData)(createInternal)

  def createInternal(request: Request[MultipartFormData[TemporaryFile]]) = {
    request.body.file("image").map { maybeValidImage =>
      Option(maybeValidImage)
        .filter(_.contentType.exists(_.startsWith("image/")))
        .filter(_.ref.file.length() <= 1 * MEGABYTE)
        .map { image =>
          newsImageService.put(image.ref.file) match {
            case Success(id) =>
              Created(Json.toJson(API.Success(data = id)))
            case Failure(e) =>
              logger.error("Error creating NewsImage", e)
              InternalServerError(Json.toJson(API.Failure[JsObject]("Internal Server Error", Seq(API.Error("image", e.getMessage)))))
          }
        }
        .getOrElse {
          BadRequest(Json.toJson(API.Failure[JsObject]("Bad Request", Seq(API.Error("Invalid image", "Image too large or in an invalid format")))))
        }
    }
      .getOrElse {
        BadRequest(Json.toJson(API.Failure[JsObject]("Bad Request", Seq(API.Error("No image", "No image provided")))))
      }
  }

}
