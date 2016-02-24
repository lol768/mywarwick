package controllers

import java.security.MessageDigest
import javax.inject.{Inject, Singleton}

import actors.WebsocketActor
import play.api.Configuration
import play.api.Play.current
import play.api.libs.json._
import play.api.mvc._
import services.SecurityService
import system.AppMetrics
import warwick.sso._

import scala.concurrent.Future

@Singleton
class HomeController @Inject()(
  security: SecurityService,
  ssoClient: SSOClient,
  metrics: AppMetrics,
  configuration: Configuration
) extends BaseController {

  import security._

  def index = Action { request =>
    implicit val links = ssoClient.linkGenerator(request)
    links.setTarget("https://" + request.host + request.path)
    Ok(views.html.index())
  }

  def socket = WebSocket.tryAcceptWithActor[JsValue, JsValue] { request =>
    SecureWebsocket(request) { loginContext: LoginContext =>
      val who = loginContext.user.map(_.usercode).getOrElse("nobody")
      logger.info(s"Websocket opening for ${who}")
      Future.successful(Right(WebsocketActor.props(loginContext, metrics.websocketTracker()) _))
    }
  }

  def photo = RequiredUserAction { request =>
    val universityId = request.context.user.get.universityId.map { id =>
      id.string
    }.getOrElse("")

    val photosHost = configuration.getString("start.photos.host")
      .getOrElse(throw new IllegalStateException("Missing Photos host - set start.photos.host"))

    val photosKey = configuration.getString("start.photos.apiKey")
      .getOrElse(throw new IllegalStateException("Missing Photos API Key - set start.photos.apiKey"))

    val photosKeyIdHash = MessageDigest.getInstance("MD5").digest(s"$photosKey$universityId".getBytes)
      .map("%02x".format(_)).mkString

    Redirect(s"https://$photosHost/start/photo/$photosKeyIdHash/$universityId")
  }
}
