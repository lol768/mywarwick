package controllers.admin

import org.joda.time.LocalDateTime
import org.scalatestplus.play.PlaySpec

class NewsItemDataTest extends PlaySpec {

  "NewsItemData" should {

    "generate valid publish date" in {

      val data = NewsItemData("title", "text", None, None, new LocalDateTime(2016,6,1,15,14))
      val saveable = data.toSave
      saveable.publishDate.toString must be ("2016-06-01T15:14:00.000+01:00")

    }

  }
}
