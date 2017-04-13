package services.dao

import helpers.OneStartAppPerSuite
import helpers.BaseSpec

class NewsImageDaoTest extends BaseSpec with OneStartAppPerSuite {

  val dao = app.injector.instanceOf[NewsImageDao]

  "NewsImageDao" should {
    "save then find an image" in transaction { implicit c =>
      val id = dao.save(NewsImageSave(
        width = 123,
        height = 456,
        contentType = "image/png",
        contentLength = 789
      ))

      val maybeImage = dao.find(id)
      maybeImage.nonEmpty mustBe true

      val image = maybeImage.get

      image.id mustBe id
      image.width mustBe 123
      image.height mustBe 456
      image.contentType mustBe "image/png"
      image.contentLength mustBe 789
      assert(image.createdAt.plusMinutes(1).isAfterNow)
    }

    "not find a non-existent image" in transaction { implicit c =>
      dao.find("nonsense") mustBe None
    }
  }

}

