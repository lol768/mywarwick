package views

import helpers.{BaseSpec, Fixtures}
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec

import org.scalatestplus.play._

import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

class EmailTest extends BaseSpec {

  val user = Fixtures.user.makeFoundUser()
  val activity = Fixtures.activity.fromSave("1", Fixtures.activitySave.submissionDue)
  val date = "2049"
  val baseUrl = "https://my.warwick.invalid"
  val optOutRoute = "/optout"
  val loggedInBefore = true

  "email.scala.html" should {
    "render title link" in {
      val result = views.html.email(user, activity, date, baseUrl, optOutRoute, loggedInBefore)
      val content = contentAsString(result)
      content must include(s"""<a href="http://tabula.warwick.ac.uk">Coursework due</a>""")
    }

    "render title as text" in {
      val result = views.html.email(user,
        Fixtures.activity.fromSave("1", Fixtures.activitySave.submissionDueWithoutUrl),
        date,
        baseUrl,
        optOutRoute,
        loggedInBefore
      )
      val content = contentAsString(result)
      content must include(s"""<p>Coursework due</p>""")
    }

    "render optout link" in {
      var result = views.html.email(user, activity, date, baseUrl, optOutRoute, loggedInBefore)
      contentAsString(result) must include(s"""<a href="https://my.warwick.invalid/optout">disable this</a>""")
      result = views.html.email(user, activity, date, baseUrl, optOutRoute, !loggedInBefore)
      contentAsString(result) must include(s"""<a href="https://my.warwick.invalid/optout">disable these emails</a>""")
    }
  }

}
