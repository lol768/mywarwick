package system

import akka.stream.Materializer
import javax.inject.Inject
import play.api.mvc.{Call, Filter, RequestHeader, Result}
import uk.ac.warwick.userlookup
import uk.ac.warwick.userlookup.UserLookupInterface
import warwick.sso._

import scala.concurrent.Future

/*
  Given a set of Call objects from a reverse router, will tell you
  if a request matches that call based on path and method.
 */
class RouteGuard(calls: Set[Call]) {
  def matches(rh: RequestHeader): Boolean = {
    calls.exists { call =>
      call.url == rh.path &&
      call.method == rh.method
    }
  }
}

object WarwickSSOFilter {

  val WARWICK_SSO_COOKIE_NAME: String = "WarwickSSO"

  def getUser(r: RequestHeader): Option[User] =
    r.attrs.get(AuthenticatedRequest.LoginContextDataAttr).flatMap(_.user)

}

class WarwickSSOFilter @Inject() (
  lookup: UserLookupInterface
) (
  implicit val mat: Materializer
) extends Filter {

  import WarwickSSOFilter._

  // Set of routes that need to use WarwickSSO cookie for authentication.
  val routeGuard: RouteGuard = new RouteGuard(Set(
    controllers.api.routes.NotificationsSnapshotController.unreads()
  ))

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    if (routeGuard.matches(rh)) {
      val cookie = rh.cookies.get(WARWICK_SSO_COOKIE_NAME)
      val user: Option[User] = cookie.map(_.value).map(lookup.getUserByToken).flatMap(toUser)
      val request = user.fold(rh)(addUser(rh))
      f(request)
    } else {
      f(rh)
    }
  }

  private def addUser(rh: RequestHeader)(u: User): RequestHeader = {
    val data = new LoginContextData {
      override val user: Option[User] = Option(u)
      override val actualUser: Option[User] = Option(u)
    }
    rh.addAttr(AuthenticatedRequest.LoginContextDataAttr, data)
  }

  private def toUser(u: userlookup.User): Option[User] =
    Option(u)
      .filter(_.isFoundUser)
      .map(User.apply)


}
