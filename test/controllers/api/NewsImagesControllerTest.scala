package controllers.api

import java.io.{File, FileInputStream}

import helpers.WithActorSystem
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.cache.CacheApi
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{MultipartFormData, Request, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services._
import services.dao.NewsImage
import warwick.sso._

import scala.concurrent.Future
import scala.util.{Failure, Success}


class NewsImagesControllerTest extends PlaySpec with MockitoSugar with Results with WithActorSystem {

  val ron = Users.create(usercode = Usercode("ron"))

  val mockSSOClient = new MockSSOClient(new LoginContext {
    override val user: Option[User] = Some(ron)
    override val actualUser: Option[User] = None

    override def loginUrl(target: Option[String]): String = "https://app.example.com/login"

    override def userHasRole(role: RoleName) = true

    override def actualUserHasRole(role: RoleName) = true
  })

  val securityService = new SecurityServiceImpl(mockSSOClient, mock[BasicAuth], mock[CacheApi])

  private val service = mock[NewsImageService]
  private val imageManipulator = mock[NoopImageManipulator]
  private val publisherService = mock[PublisherService]
  val cache = new MockCacheApi
  val controller = new NewsImagesController(
    securityService,
    service,
    imageManipulator,
    publisherService,
    cache
  ) {
    override val navigationService = new MockNavigationService()
    override val ssoClient = mockSSOClient
  }

  val frog = new File("test/resources/frog.jpg")
  val tempFile = File.createTempFile("frog", ".jpg")
  tempFile.deleteOnExit()
  FileUtils.copyFile(frog, tempFile, true)

  when(service.find(any())).thenReturn(None)
  when(service.fetchStream(any())).thenReturn(None)

  when(service.find("frog")).thenReturn(Some(NewsImage(
    id = "frog",
    width = 736,
    height = 983,
    contentType = "image/jpeg",
    contentLength = 184392,
    createdAt = DateTime.now
  )))

  "NewsImagesController#show" should {

    "404 on missing image" in {
      val result = call(controller.show("missing"), FakeRequest())

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "Image not found"
    }

    "404 on object missing from store" in {
      val result = call(controller.show("frog"), FakeRequest())

      status(result) mustBe NOT_FOUND
      contentAsString(result) mustBe "Object missing from store"
    }

    "send original-size image" in {
      val frogImageBytes = FileUtils.readFileToByteArray(frog)
      val frogInputStream = new FileInputStream(frog)

      when(service.fetchStream("frog")).thenReturn(Some(frogInputStream))

      val result = call(controller.show("frog"), FakeRequest())

      status(result) mustBe OK
      headers(result).get("Content-Disposition") mustBe Some("inline")
      contentAsBytes(result) mustBe frogImageBytes
    }

    "send resized image" in {
      val frogInputStream = new FileInputStream(frog)

      when(service.fetchStream("frog")).thenReturn(Some(frogInputStream))
      when(imageManipulator.resizeToWidth(any(), any())).thenCallRealMethod()

      val result = call(controller.show("frog"), FakeRequest("GET", "/?width=100"))

      status(result) mustBe OK
      headers(result).get("Content-Disposition") mustBe Some("inline")

      verify(imageManipulator).resizeToWidth(any(), Matchers.eq(100))
    }

  }

  "NewsImagesController#create" should {

    val request = mock[Request[MultipartFormData[TemporaryFile]]]

    "fail with empty request" in {
      when(request.body).thenReturn(MultipartFormData(Map.empty, Seq.empty[FilePart[TemporaryFile]], Seq.empty))

      val result = Future.successful(controller.createInternal(request))

      status(result) mustBe BAD_REQUEST

      val json = contentAsJson(result)

      (json \ "success").as[Boolean] mustBe false
      (json \ "errors" \\ "id").head.as[String] mustBe "no-image"
    }

    "fail with not-an-image" in {
      val part = FilePart("image", "image.pdf", Some("application/pdf"), TemporaryFile("start", "pdf"))
      when(request.body).thenReturn(MultipartFormData(Map.empty, Seq(part), Seq.empty))

      val result = Future.successful(controller.createInternal(request))

      status(result) mustBe BAD_REQUEST

      val json = contentAsJson(result)

      (json \ "success").as[Boolean] mustBe false
      (json \ "errors" \\ "id").head.as[String] mustBe "invalid-content-type"
    }

    "fail if service fails" in {
      val part = FilePart("image", "frog.jpg", Some("image/jpeg"), TemporaryFile(tempFile))
      when(request.body).thenReturn(MultipartFormData(Map.empty, Seq(part), Seq.empty))

      when(service.put(any())).thenReturn(Failure(new Exception("Something went wrong")))

      val result = Future.successful(controller.createInternal(request))

      status(result) mustBe INTERNAL_SERVER_ERROR

      val json = contentAsJson(result)

      (json \ "success").as[Boolean] mustBe false
      (json \ "errors" \\ "message").head.as[String] mustBe "Something went wrong"
    }

    "put an image" in {
      val part = FilePart("image", "frog.jpg", Some("image/jpeg"), TemporaryFile(tempFile))
      when(request.body).thenReturn(MultipartFormData(Map.empty, Seq(part), Seq.empty))

      when(service.put(any())).thenReturn(Success("image-id"))

      val result = Future.successful(controller.createInternal(request))

      status(result) mustBe CREATED

      val json = contentAsJson(result)

      (json \ "success").as[Boolean] mustBe true
      (json \ "data").as[String] mustBe "image-id"
    }

  }

}
