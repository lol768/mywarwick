package services

import javax.inject.Inject

import play.api.mvc.Results._
import play.api.mvc.{ActionFilter, _}
import uk.ac.warwick.sso.client.SSOConfiguration
import uk.ac.warwick.sso.client.core.{LinkGenerator, LinkGeneratorImpl}
import warwick.sso._

import scala.concurrent.Future

class CookieLoginContext(linkGenerator: LinkGenerator, u: Option[User], r: Option[User]) extends LoginContext {
  override val user: Option[User] = u
  override val actualUser: Option[User] = r
  override def userHasRole(role: RoleName): Boolean = roles(user).contains(role)
  override def actualUserHasRole(role: RoleName): Boolean = roles(actualUser).contains(role)

  def loginUrl(target: Option[String]) = {
    target.foreach(linkGenerator.setTarget)
    linkGenerator.getLoginUrl
  }

  private def roles(u: Option[User]): Seq[RoleName] = for {
    person <- u.toSeq
    roleAttribute <- person.rawProperties.get("testroles").toSeq
    s <- roleAttribute.split(",")
  } yield RoleName(s)
}

/**
  * Completely insecure SSO client for functional tests to become a particular
  * user by setting a cookie. Also allows setting roles via a different cookie.
  */
class CookieSSOClient @Inject() (
  configuration: SSOConfiguration,
  users: UserLookupService
) extends SSOClient {

  def linkGenerator(request: RequestHeader) = {
    val req = new PlayHttpRequestHeader(request)
    new LinkGeneratorImpl(configuration, req)
  }

  lazy val Strict = Lenient andThen requireCondition(_.context.user.isDefined, otherwise = redirectToSSO)

  lazy val Lenient = FindUser

  override def RequireRole(role: RoleName, otherwise: AuthRequest[_] => Result) = Strict andThen requireCondition(_.context.userHasRole(role), otherwise)
  override def RequireActualUserRole(role: RoleName, otherwise: AuthRequest[_] => Result) = Strict andThen requireCondition(_.context.actualUserHasRole(role), otherwise)

  object FindUser extends SSOActionBuilder {
    def disallowRedirect = this

    override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
      val ctx = getLoginContext(request)
      block(new AuthenticatedRequest(ctx, request))
    }
  }

  override def withUser[A](request: RequestHeader)(block: (LoginContext) => TryAcceptResult[A]) : TryAcceptResult[A] = {
    val ctx = getLoginContext(request)
    block(ctx)
  }

  // Could support the masquerade cookie here.
  private def getLoginContext(request: RequestHeader) = {
    val user = getUser(request)
    new CookieLoginContext(linkGenerator(request), user, user)
  }

  private def getUser(request: RequestHeader) =
    request.cookies.get("insecure-fake-user").map { _.value }
      .flatMap { usercode => users.getUser(Usercode(usercode)).toOption }
      .map { user =>
        // Extra cookie for comma-separated list of roles
        request.cookies.get("insecure-fake-roles").map { cookie =>
          val newProps = user.rawProperties + ("testroles" -> cookie.value)
          user.copy(rawProperties = newProps)
        }.getOrElse(user)
      }

  private def requireCondition(block: AuthRequest[_] => Boolean, otherwise: AuthRequest[_] => Result) =
    new ActionFilter[AuthRequest] {
      override protected def filter[A](request: AuthRequest[A]) =
        Future.successful {
          block(request) match {
            case true => None
            case false => Some(otherwise(request))
          }
        }
    }

  private def redirectToSSO(request: AuthRequest[_]) = Redirect(request.context.loginUrl(None))

}
