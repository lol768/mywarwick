package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import play.api.cache.CacheApi
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import warwick.sso._

import scala.concurrent.Future
import scala.concurrent.duration._

@ImplementedBy(classOf[SecurityServiceImpl])
trait SecurityService {

  def UserAction: ActionBuilder[AuthenticatedRequest]
  def RequiredUserAction: ActionBuilder[AuthenticatedRequest]

  def APIAction: ActionBuilder[AuthenticatedRequest]

}

/**
  * Wraps up all the auth stuff from other libraries so that we can easily swap out
  * behaviours in controllers without them being tied into SSOClient specifics, and
  * we can combine actions together here too.
  */
class SecurityServiceImpl @Inject() (
  val ssoClient: SSOClient,
  val basicAuth: BasicAuth,
  cache: CacheApi
) extends SecurityService {

  import play.api.libs.concurrent.Execution.Implicits._

  val UserAction = ssoClient.Lenient
  val RequiredUserAction = ssoClient.Strict

  // TODO this always returns a forbidden result if no user found. We might want API calls for anonymous users.
  val APIAction = ssoClient.Lenient andThen BasicAuthFallback

  /**
    * If a user already exists in the incoming AuthenticatedRequest, we use that.
    * otherwise we try Basic Auth.
    */
  object BasicAuthFallback extends ActionFunction[AuthenticatedRequest, AuthenticatedRequest] {
    override def invokeBlock[A](request: AuthenticatedRequest[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
      if (request.context.user.exists(_.isFound)) block(request)
      else basicAuth.Check(basicAuthDenied).invokeBlock(request, block)
    }
  }

  // FIXME this response obviously sucks. Use a consistent good standard for JSON error responses
  def basicAuthDenied(request: RequestHeader) = Future {
    Forbidden(Json.obj(
      "denied" -> true
    ))
  }

}
