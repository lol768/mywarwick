package models

import helpers.BaseSpec
import org.joda.time.DateTime
import play.api.libs.json.{JsUndefined, JsDefined, Json}
import warwick.sso.Usercode

class ActivityTest extends BaseSpec {

  "Activity" should {
    "not output { text: null } in Json writes" in {
      val noneText: Option[String] = None
      val activityWithNoneText = Activity("id", "providerId", "type", "title", noneText, None, None, DateTime.now, DateTime.now, Usercode("cusjau"), true, true)
      Json.toJson(activityWithNoneText) \ "text" mustBe a [JsUndefined]

      val someText: Option[String] = Some("this is text")
      val activityWithSomeText = Activity("id", "providerId", "type", "title", someText, None, None, DateTime.now, DateTime.now, Usercode("cusjau"), true, true)
      Json.toJson(activityWithSomeText) \ "text" mustBe a [JsDefined]
    }
  }
}
