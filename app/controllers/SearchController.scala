package controllers

import com.google.inject.{Inject, Singleton}
import org.apache.http.Header
import org.apache.http.client.methods.HttpGet
import play.api.Configuration
import play.api.libs.ws.{InMemoryBody, WSClient}
import play.api.mvc.Result
import services.SecurityService
import system.ThreadPools.externalData
import uk.ac.warwick.sso.client.trusted.{TrustedApplicationUtils, TrustedApplicationsManager}
import warwick.sso.User

@Singleton
class SearchController @Inject()(
  securityService: SecurityService,
  ws: WSClient,
  trustedApplicationsManager: TrustedApplicationsManager,
  configuration: Configuration
) extends BaseController {

  import securityService._

  val searchRoot = configuration.getString("start.search.root")
    .getOrElse(throw new IllegalStateException("Search root configuration missing - check start.search.root in application.conf"))

  val ExcludedHeaders = Seq(CONTENT_TYPE, CONTENT_LENGTH)

  def proxy(path: String) = UserAction.async(parse.raw) { implicit request =>
    val url = searchRoot + "/" + Seq(path, request.rawQueryString).filter(_.nonEmpty).mkString("?")
    val body = InMemoryBody(request.body.asBytes(request.body.memoryThreshold).get)

    val trustedHeaders = signRequest(url, request.context.user)

    ws
      .url(url)
      .withMethod(request.method)
      .withHeaders(request.headers.toSimpleMap.toSeq: _*)
      .withHeaders(trustedHeaders: _*)
      .withBody(body)
      .execute()
      .map { response =>
        val contentType = response.header(CONTENT_TYPE).getOrElse("text/html")

        val headers = response.allHeaders.collect {
          case (name, value :: _) if !ExcludedHeaders.contains(name) => name -> value
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
