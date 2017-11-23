package services

import java.security.MessageDigest

import com.google.inject.{ImplementedBy, Inject}
import play.api.Configuration
import play.api.libs.ws.WSClient
import system.Logging
import system.ThreadPools.externalData
import warwick.sso.UniversityID

import scala.concurrent.Future

@ImplementedBy(classOf[PhotoServiceImpl])
trait PhotoService {
  def photoUrl(universityId: Option[UniversityID]): Future[String]
}

class PhotoServiceImpl @Inject()(
  configuration: Configuration,
  ws: WSClient
) extends PhotoService with Logging {

  private val photosHost = configuration.getOptional[String]("mywarwick.photos.host")
    .getOrElse(throw new IllegalStateException("Missing Photos host - set mywarwick.photos.host"))

  private val photosKey = configuration.getOptional[String]("mywarwick.photos.apiKey")
    .getOrElse(throw new IllegalStateException("Missing Photos API Key - set mywarwick.photos.apiKey"))

  def photoUrl(universityId: Option[UniversityID]): Future[String] = {
    universityId.map { universityId =>
      ws.url(s"$photosHost/start/photo/${hash(universityId)}/${universityId.string}.json?s=240")
        .get()
        .map {
          case res if res.status == 200 => res
          case res => throw new RuntimeException(s"Unexpected response from photo service: ${res.status}")
        }
        .map(response => (response.json \ "photo" \ "url").as[String])
        .recover { case e =>
          logger.warn(s"Unable to retrieve photo for ${universityId.string}", e)
          throw e
        }
    }.getOrElse(Future.failed(NoUniversityID))
  }

  object NoUniversityID extends Throwable

  private def hash(universityId: UniversityID): String = {
    MessageDigest.getInstance("MD5").digest(s"$photosKey${universityId.string}".getBytes)
      .map("%02x".format(_)).mkString
  }

}
