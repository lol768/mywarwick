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
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{Action, MultipartFormData, Request, Result}
import services.{ImageManipulator, NewsImageService, SecurityService}
import system.EitherValidation

import scala.util.{Failure, Success}

class NewsImagesController @Inject()(
  securityService: SecurityService,
  newsImageService: NewsImageService,
  imageManipulator: ImageManipulator,
  cache: CacheApi
) extends BaseController {

  import EitherValidation._
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
          Ok(byteArray).as(newsImage.contentType).withHeaders(
            CONTENT_DISPOSITION -> "inline",
            CACHE_CONTROL -> "max-age=31536000"
          )
        }.getOrElse(NotFound("Object missing from store"))
    }.getOrElse(NotFound("Image not found"))
  }

  private val MEGABYTE = 1000 * 1000

  def create = RequiredActualUserRoleAction(Sysadmin)(parse.multipartFormData)(createInternal)

  def createInternal(request: Request[MultipartFormData[TemporaryFile]]): Result = {
    request.body.file("image").map { maybeValidImage =>
      Right[Result, FilePart[TemporaryFile]](maybeValidImage)
        .verifying(_.contentType.exists(_.startsWith("image/")), API.Error("invalid-content-type", "Invalid image content type"))
        .verifying(_.ref.file.length() <= 1 * MEGABYTE, API.Error("image-content-length", "The uploaded image is too large (1MB max)"))
        .andThen { image =>
          newsImageService.put(image.ref.file) match {
            case Success(id) =>
              Right(id)
            case Failure(e) =>
              logger.error("Error creating NewsImage", e)
              Left(InternalServerError(Json.toJson(API.Failure[JsObject]("Internal Server Error", Seq(API.Error("internal-server-error", e.getMessage))))))
          }
        }
        .fold(
          e => e,
          id => {
            auditLog('CreateNewsImage, 'id -> id)(requestContext(request))
            Created(Json.toJson(API.Success(data = id)))
          }
        )
    }.getOrElse(API.Error("no-image", "No image provided"))
  }

  implicit def apiError2result(e: API.Error): Result =
    BadRequest(Json.toJson(API.Failure[JsObject]("Bad Request", Seq(e))))

}
