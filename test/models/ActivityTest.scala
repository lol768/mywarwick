package models

import helpers.BaseSpec
import org.joda.time.DateTime
import play.api.libs.json._
import warwick.sso.Usercode

class ActivityTest extends BaseSpec {

  "Activity" should {
    "not output { text: null } in Json writes" in {
      val noneText: Option[String] = None
      val activityWithNoneText = Activity("id", "providerId", "type", "title", noneText, None, None, DateTime.now, DateTime.now, Usercode("cusjau"), shouldNotify = true, api = true)
      Json.toJson(activityWithNoneText) \ "text" mustBe a [JsUndefined]

      val someText: Option[String] = Some("this is text")
      val activityWithSomeText = Activity("id", "providerId", "type", "title", someText, None, None, DateTime.now, DateTime.now, Usercode("cusjau"), shouldNotify = true, api = true)
      Json.toJson(activityWithSomeText) \ "text" mustBe a [JsDefined]
    }
  }

  "ActivityRender" should {
    "not output textAsHtml in Json writes if text doesn't include Markdown characters" in {
      val provider: ActivityProvider = ActivityProvider("id", sendEmail = false, overrideMuting = false)
      val `type`: ActivityType = ActivityType("type")

      def render(activity: Activity): ActivityRender =
        ActivityRender(activity, None, Nil, provider, `type`)

      val textWithNoMarkdownText: Option[String] = Some("this is text")
      val activityWithNoMarkdownText = Activity("id", "providerId", "type", "title", textWithNoMarkdownText, None, None, DateTime.now, DateTime.now, Usercode("cusjau"), shouldNotify = true, api = true)
      Json.toJson(render(activityWithNoMarkdownText)) \ "textAsHtml" mustBe JsDefined(JsNull)

      val textWithNewLines: Option[String] = Some("this is line 1\n\nthis is line 2")
      val activityWithNewLines = Activity("id", "providerId", "type", "title", textWithNewLines, None, None, DateTime.now, DateTime.now, Usercode("cusjau"), shouldNotify = true, api = true)
      Json.toJson(render(activityWithNewLines)) \ "textAsHtml" mustBe JsDefined(JsString("<p>this is line 1</p>\n<p>this is line 2</p>\n"))

      val textWithSpecialCharacters: Option[String] = Some("this is text with *emphasis* in it")
      val activityWithSpecialCharacters = Activity("id", "providerId", "type", "title", textWithSpecialCharacters, None, None, DateTime.now, DateTime.now, Usercode("cusjau"), shouldNotify = true, api = true)
      Json.toJson(render(activityWithSpecialCharacters)) \ "textAsHtml" mustBe JsDefined(JsString("<p>this is text with <em>emphasis</em> in it</p>\n"))

      val textThatIsntMarkdown: Option[String] = Some("*this isn't markdown")
      val activityThatIsntMarkdown = Activity("id", "providerId", "type", "title", textThatIsntMarkdown, None, None, DateTime.now, DateTime.now, Usercode("cusjau"), shouldNotify = true, api = true)
      Json.toJson(render(activityThatIsntMarkdown)) \ "textAsHtml" mustBe JsDefined(JsNull)
    }
  }
}
