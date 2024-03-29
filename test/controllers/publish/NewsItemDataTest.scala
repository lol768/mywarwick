package controllers.publish

import org.joda.time.LocalDateTime
import helpers.BaseSpec
import warwick.sso.Usercode

class NewsItemDataTest extends BaseSpec {

  "NewsItemData" should {

    "generate valid publish date" in {
      val data = NewsItemData("title", "text", None, None, publishDateSet = true, new LocalDateTime(2016, 6, 1, 15, 14), None)
      data.toSave(Usercode("custard"), "publisher").publishDate.toString must be("2016-06-01T15:14:00.000+01:00")
    }

    "use current time if publish date not set" in {
      val data = NewsItemData("title", "text", None, None, publishDateSet = false, new LocalDateTime(2016, 6, 1, 15, 14), None)
      data.toSave(Usercode("custard"), "publisher").publishDate.toString mustNot be("2016-06-01T15:14:00.000+01:00")
    }

  }
}
