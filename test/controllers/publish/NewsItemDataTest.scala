package controllers.publish

import helpers.BaseSpec
import org.joda.time.{DateTimeUtils, LocalDateTime}
import org.scalatest.BeforeAndAfterAll
import warwick.sso.Usercode

class NewsItemDataTest extends BaseSpec with BeforeAndAfterAll {

  private val mockNow = new LocalDateTime(2019, 7, 18, 12, 14)

  override def beforeAll(): Unit = {
    DateTimeUtils.setCurrentMillisFixed(mockNow.toDateTime.getMillis)
  }

  override def afterAll(): Unit = {
    DateTimeUtils.setCurrentMillisSystem()
  }

  "NewsItemData" should {
    "generate valid future publish date" in {
      val futureDate = mockNow.plusHours(1)
      NewsItemData("title", "text", None, None, publishDateSet = true, futureDate, None).toSave(Usercode("custard"), "publisher").publishDate mustBe futureDate.toDateTime
    }

    "use now if publish date not set" in {
      val futureDate = mockNow.plusHours(1)
      NewsItemData("title", "text", None, None, publishDateSet = false, futureDate, None).toSave(Usercode("custard"), "publisher").publishDate mustBe mockNow.toDateTime
    }

    "use now if publish date in past" in {
        val pastDate = mockNow.minusHours(1)
        NewsItemData("title", "text", None, None, publishDateSet = true, pastDate, None).toSave(Usercode("custard"), "publisher").publishDate mustBe mockNow.toDateTime
    }
  }
}
