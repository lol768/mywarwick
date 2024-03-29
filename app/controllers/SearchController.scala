package controllers

import com.google.inject.{Inject, Singleton}
import javax.inject.Named
import org.apache.http.client.methods.HttpGet
import play.api.Configuration
import play.api.libs.ws.{InMemoryBody, WSClient}
import play.api.mvc.{Action, RawBuffer}
import services.SecurityService
import uk.ac.warwick.sso.client.trusted.{TrustedApplicationUtils, TrustedApplicationsManager}
import warwick.sso.User

import scala.concurrent.ExecutionContext

@Singleton
class SearchController @Inject()(
  securityService: SecurityService,
  ws: WSClient,
  trustedApplicationsManager: TrustedApplicationsManager,
  configuration: Configuration
)(implicit @Named("externalData") ec: ExecutionContext) extends MyController {

  import securityService._

  private val searchRoot = configuration.getOptional[String]("mywarwick.search.root")
    .getOrElse(throw new IllegalStateException("Search root configuration missing - check mywarwick.search.root in application.conf"))

  private val ForwardedHeaders = Seq(CONTENT_TYPE, ACCEPT).map(_.toLowerCase)
  private val IgnoredReturnHeaders = Seq(CONTENT_TYPE, CONTENT_LENGTH).map(_.toLowerCase)

  def proxy(path: String): Action[RawBuffer] = UserAction.async(parse.raw) { implicit request =>
    val url = searchRoot + "/" + Seq(path, request.rawQueryString).filter(_.nonEmpty).mkString("?")
    val body = InMemoryBody(request.body.asBytes(request.body.memoryThreshold).get)

    val trustedHeaders = signRequest(url, request.context.user)

    val headers = request.headers.toSimpleMap.filterKeys(key => ForwardedHeaders.contains(key.toLowerCase))

    ws
      .url(url)
      .withMethod(request.method)
      .addHttpHeaders(headers.toSeq: _*)
      .addHttpHeaders(trustedHeaders: _*)
      .withBody(body)
      .execute()
      .map { response =>
        val contentType = response.header(CONTENT_TYPE).getOrElse("text/html")

        val headers = response.headers.collect {
          case (name, value :: _) if !IgnoredReturnHeaders.contains(name.toLowerCase) => name -> value
        }

        Status(response.status)
          .apply(response.bodyAsBytes)
          .as(contentType)
          .withHeaders(headers.toSeq: _*)
      }
  }

  private def signRequest(url: String, user: Option[User]): Seq[(String, String)] = {
    user match {
      case Some(u: User) =>
        val httpRequest = new HttpGet(url)
        TrustedApplicationUtils.signRequest(trustedApplicationsManager.getCurrentApplication, u.usercode.string, httpRequest)
        httpRequest.getAllHeaders.map(h => h.getName -> h.getValue)
      case None =>
        Seq.empty
    }
  }

}
