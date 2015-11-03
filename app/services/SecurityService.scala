package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{ActionBuilder, RequestHeader}
import warwick.sso.{AuthenticatedRequest, BasicAuth, SSOClient}

import scala.concurrent.Future

@ImplementedBy(classOf[SecurityServiceImpl])
trait SecurityService {

  def UserAction: ActionBuilder[AuthenticatedRequest]
  def RequiredUserAction: ActionBuilder[AuthenticatedRequest]

  val APIAction: ActionBuilder[AuthenticatedRequest]

}

/**
  * Wraps up all the auth stuff from other libraries so that we can easily swap out
  * behaviours in controllers without them being tied into SSOClient specifics, and
  * we can combine actions together here too.
  */
class SecurityServiceImpl @Inject() (val ssoClient: SSOClient, val basicAuth: BasicAuth) extends SecurityService {
  import play.api.libs.concurrent.Execution.Implicits._

  val UserAction = ssoClient.Lenient
  val RequiredUserAction = ssoClient.Strict

  val APIAction = basicAuth.Check(basicAuthDenied)

  // FIXME this response obviously sucks. Use a consistent good standard for JSON error responses
  def basicAuthDenied(request: RequestHeader) = Future {
    Forbidden(Json.obj(
      "denied" -> true
    ))
  }

}
