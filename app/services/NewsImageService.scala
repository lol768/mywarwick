package services

import java.io.{File, InputStream}
import java.net.URLConnection
import javax.imageio.ImageIO

import com.google.common.io.{ByteSource, Files}
import com.google.inject.{ImplementedBy, Inject, Singleton}
import play.api.db.{Database, NamedDatabase}
import services.dao.{NewsImage, NewsImageDao, NewsImageSave}
import warwick.objectstore.ObjectStorageService
import warwick.objectstore.ObjectStorageService.Metadata

import scala.util.Try

@ImplementedBy(classOf[NewsImageServiceImpl])
trait NewsImageService {

  def put(file: File): Try[String]

  def find(id: String): Option[NewsImage]

  def fetchStream(id: String): Option[InputStream]

}

case class ImageDimensions(width: Int, height: Int)

@Singleton
class NewsImageServiceImpl @Inject()(
  dao: NewsImageDao,
  objectStorageService: ObjectStorageService,
  @NamedDatabase("default") db: Database
) extends NewsImageService {

  def find(id: String): Option[NewsImage] = db.withConnection(implicit c => dao.find(id))

  def fetchStream(id: String) = objectStorageService.fetch(id)

  def put(file: File): Try[String] = Try {
    val contentLength = file.length()

    val byteSource = Files.asByteSource(file)

    val contentType = getContentType(byteSource).getOrElse(throw new Exception("Unknown content type"))
    val ImageDimensions(width, height) = getImageDimensions(file).getOrElse(throw new Exception("Could not open image"))

    val metadata = Metadata(contentLength, contentType, fileHash = None)

    db.withTransaction { implicit c =>
      val id = dao.save(NewsImageSave(width, height, contentType, contentLength.toInt))
      objectStorageService.put(id, byteSource, metadata)
      id
    }
  }

  private def getContentType(byteSource: ByteSource): Option[String] = {
    val stream = byteSource.openBufferedStream()
    try {
      Option(URLConnection.guessContentTypeFromStream(stream))
    } finally {
      stream.close()
    }
  }

  private def getImageDimensions(file: File): Try[ImageDimensions] = {
    Try(ImageIO.read(file)).map { image =>
      require(image != null, "Could not get image dimensions")

      val dimensions = ImageDimensions(image.getWidth, image.getHeight)
      image.flush()
      dimensions
    }
  }

}
