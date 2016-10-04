package services

import java.security.MessageDigest

import com.google.inject.{ImplementedBy, Inject}
import play.api.Configuration
import play.api.libs.ws.WSAPI
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
  ws: WSAPI
) extends PhotoService with Logging {

  val photosHost = configuration.getString("mywarwick.photos.host")
    .getOrElse(throw new IllegalStateException("Missing Photos host - set mywarwick.photos.host"))

  val photosKey = configuration.getString("mywarwick.photos.apiKey")
    .getOrElse(throw new IllegalStateException("Missing Photos API Key - set mywarwick.photos.apiKey"))

  def photoUrl(universityId: Option[UniversityID]): Future[String] = {
    universityId.map { universityId =>
      ws.url(s"$photosHost/start/photo/${hash(universityId)}/${universityId.string}.json?s=60")
        .get()
        .filter(_.status == 200)
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
