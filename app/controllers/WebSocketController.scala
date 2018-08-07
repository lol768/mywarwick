package controllers

import javax.inject.{Inject, Singleton}
import actors.{PubSubActor, WebSocketActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Results, WebSocket}
import services.SecurityService
import system.{AppMetrics, Logging}
import warwick.sso.LoginContext

import scala.concurrent.Future


@Singleton
class WebSocketController @Inject()(implicit
  security: SecurityService,
  metrics: AppMetrics,
  system: ActorSystem,
  mat: Materializer,
  config: Configuration,
) extends Logging {

  import security._

  private val origin: String = config.get[String]("mywarwick.rootUrl")

  // This actor lives as long as the controller
  private val pubSubActor = system.actorOf(PubSubActor.props())

  def socket: WebSocket = WebSocket.acceptOrResult[JsValue, JsValue] { request =>
    if (request.headers("Origin") == origin) {
      SecureWebsocket(request) { loginContext: LoginContext =>
        val who = loginContext.user.map(_.usercode).getOrElse("nobody")
        logger.info(s"WebSocket opening for $who")
        val flow = ActorFlow.actorRef(out => WebSocketActor.props(loginContext, metrics.websocketTracker(), pubSubActor, out))
        Future.successful(Right(flow))
      }
    } else {
      Future.successful(Left(Results.Unauthorized("Bad Origin")))
    }
  }

}
