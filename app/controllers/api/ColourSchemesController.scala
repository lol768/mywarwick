package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import com.sun.syndication.feed.synd._
import com.sun.syndication.io.WireFeedOutput
import controllers.BaseController
import models.Audience.DepartmentSubset
import models.news.NewsItemRender
import models.{API, PageViewHit}
import org.jdom.{Element, Namespace}
import play.api.libs.json._
import play.api.mvc.{Action, RequestHeader}
import services.analytics.AnalyticsMeasurementService
import services.{NewsService, SecurityService}
import warwick.sso.GroupName

import scala.collection.JavaConverters._

@Singleton
class ColourSchemesController @Inject()(
  security: SecurityService
) extends BaseController {


  import security._

  def getChoice = RequiredUserAction { request =>
    val user = request.context.user.get

    val data = JsObject(Map(
      "chosenColourScheme" -> JsNumber(1)
    ))

    Ok(Json.toJson(API.Success(data = data)))
  }

  def getAvailable = Action { request =>
    val data = JsObject(Map(
      "schemes" -> JsArray(Seq[JsObject](
        JsObject(Map(
          "id" -> JsNumber(1),
          "friendlyName" -> JsString("Bluebell"),
          "url" -> JsString("bg01.jpg")
        )),
        JsObject(Map(
          "id" -> JsNumber(2),
          "friendlyName" -> JsString("Westwood")
        ))
      ))
    ))

    Ok(Json.toJson(API.Success(data = data)))
  }

}
