package services

import java.io.{File, FileOutputStream, InputStream}

import org.mockito.ArgumentMatchers.{eq => isEq, _}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import helpers.BaseSpec
import services.dao.{NewsImage, NewsImageDao, NewsImageSave}
import warwick.objectstore.ObjectStorageService
import warwick.objectstore.ObjectStorageService.Metadata

import scala.util.Success

class NewsImageServiceTest extends BaseSpec with MockitoSugar {

  val dao = mock[NewsImageDao]
  val objectStorageService = mock[ObjectStorageService]
  val service = new NewsImageServiceImpl(dao, objectStorageService, new MockDatabase)

  "NewsImageService#find" should {
    "call the dao" in {
      val someImage = Some(mock[NewsImage])
      when(dao.find(isEq("image-id"))(any())).thenReturn(someImage)

      service.find("image-id") mustBe someImage
    }
  }

  "NewsImageService#fetchStream" should {
    "call the object storage service" in {
      val someStream = Some(mock[InputStream])
      when(objectStorageService.fetch("abc")).thenReturn(someStream)

      service.fetchStream("abc") mustBe someStream
    }
  }

  "NewsImageService#put" should {
    "fail with invalid file" in {
      val file = File.createTempFile("start", ".jpg")
      file.deleteOnExit()
      val result = service.put(file)

      result.isSuccess mustBe false
      result.failed.get.getMessage mustBe "Unknown content type"
    }

    "fail with plausible but unreadable file" in {
      val file = File.createTempFile("start", ".jpg")
      file.deleteOnExit()

      val fos = new FileOutputStream(file)
      val jpegHeader = Array(0xFF, 0xD8, 0xFF, 0xE0).map(_.toByte)
      fos.write(jpegHeader)
      fos.close()

      val result = service.put(file)
      result.isSuccess mustBe false
      result.failed.get.getMessage mustBe "Could not open image"
    }

    "save an image to the database and object store" in {
      val file = new File("test/resources/frog.jpg")

      val newsImageSave = NewsImageSave(
        width = 736,
        height = 983,
        contentType = "image/jpeg",
        contentLength = 184392
      )

      when(dao.save(isEq(newsImageSave))(any())).thenReturn("image-id")

      service.put(file) mustBe Success("image-id")

      val metadata = Metadata(
        contentLength = 184392,
        contentType = "image/jpeg",
        fileHash = None
      )

      verify(dao).save(isEq(newsImageSave))(any())
      verify(objectStorageService).put(isEq("image-id"), any(), isEq(metadata))
    }
  }

}
