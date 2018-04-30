package system

import akka.stream.Materializer
import helpers.{BaseSpec, WithActorSystem}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.mvc._
import play.api.mvc.request.RequestTarget
import play.api.test.FakeRequest
import play.api.test.Helpers._
import system.WarwickSSOFilter._
import uk.ac.warwick.userlookup
import uk.ac.warwick.userlookup.UserLookupInterface

import scala.concurrent.Future
import Results._
import helpers.Tap._

object WarwickSSOFilterTest {

  private class Context(implicit val mat: Materializer) extends MockitoSugar {
    val lookup = mock[UserLookupInterface]
    val filter: Filter = new WarwickSSOFilter(lookup)

    val cusxxxToken = "12345"
    val cusxxxCookie = Cookie(WARWICK_SSO_COOKIE_NAME, cusxxxToken)
    val cusxxx = new userlookup.User("cusxxx").tap { u =>
      u.setFoundUser(true)
    }

    when(lookup.getUserByToken(cusxxxToken)).thenReturn(cusxxx)

    def reqWithToken =
      FakeRequest()
        .withCookies(cusxxxCookie)

    def reqWithTokenForPath =
      FakeRequest(GET, controllers.api.routes.NotificationsSnapshotController.unreads().url)
        .withCookies(cusxxxCookie)

    def action(req: RequestHeader): Future[Result] =
      getUser(req).map { user =>
        Future.successful(Ok(s"Hello ${user.usercode.string}"))
      }.getOrElse {
        Future.successful(Forbidden("No user"))
      }

    def runFilter(req: RequestHeader, body: RequestHeader => Future[Result] = action): Future[Result] =
      filter(body)(req)
  }

}

class WarwickSSOFilterTest extends BaseSpec with WithActorSystem {

  import system.WarwickSSOFilterTest._

  private def assertNoUser(result: Future[Result]) = {
    status(result) mustBe FORBIDDEN
    contentAsString(result) mustBe "No user"
  }

  private def assertUser(result: Future[Result], user: userlookup.User) = {
    status(result) mustBe OK
    contentAsString(result) mustBe s"Hello ${user.getUserId}"
  }

  "WarwickSSOFilter" should {
    "ignore missing cookie" in new Context {
      assertNoUser(runFilter(FakeRequest()))
    }

    "resolve user" in new Context {
      assertUser(runFilter(reqWithTokenForPath), cusxxx)
      verify(lookup).getUserByToken(cusxxxToken)
    }

    // token cookie is valid but it's not a matching URL
    "only run for certain URLs" in new Context {
      assertNoUser(runFilter(reqWithToken))
    }
  }
}
