package controllers

import javax.inject.{Inject, Singleton}

import actors.WebSocketActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket
import services.SecurityService
import system.{AppMetrics, Logging}
import warwick.sso.LoginContext

import scala.concurrent.Future


@Singleton
class WebSocketController @Inject()(implicit
  security: SecurityService,
  metrics: AppMetrics,
  system: ActorSystem,
  mat: Materializer
) extends Logging {

  import security._

  def socket = WebSocket.acceptOrResult[JsValue, JsValue] { request =>
    SecureWebsocket(request) { loginContext: LoginContext =>
      val who = loginContext.user.map(_.usercode).getOrElse("nobody")
      logger.info(s"WebSocket opening for $who")
      val flow = ActorFlow.actorRef(out => WebSocketActor.props(loginContext, metrics.websocketTracker())(out))
      Future.successful(Right(flow))
    }
  }

}
