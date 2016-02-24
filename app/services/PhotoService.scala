package services

import java.security.MessageDigest

import com.google.inject.{ImplementedBy, Inject}
import play.api.Configuration
import warwick.sso.UniversityID

@ImplementedBy(classOf[PhotoServiceImpl])
trait PhotoService {
  def photo(uniId: UniversityID): String
}

class PhotoServiceImpl @Inject()(
  configuration: Configuration
) extends PhotoService {

  def photo(uniId: UniversityID): String = {
    val photosHost = configuration.getString("start.photos.host")
      .getOrElse(throw new IllegalStateException("Missing Photos host - set start.photos.host"))

    val photosKey = configuration.getString("start.photos.apiKey")
      .getOrElse(throw new IllegalStateException("Missing Photos API Key - set start.photos.apiKey"))

    val photosKeyIdHash = MessageDigest.getInstance("MD5").digest(s"$photosKey${uniId.string}".getBytes)
      .map("%02x".format(_)).mkString

    s"https://$photosHost/start/photo/$photosKeyIdHash/${uniId.string}?s=60"
  }
}
