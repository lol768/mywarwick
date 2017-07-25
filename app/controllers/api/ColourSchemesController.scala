package controllers.api

import javax.inject.Singleton
import com.google.inject.Inject
import controllers.BaseController
import models.{API, PageViewHit}
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc.{Action, RequestHeader}
import services.{NewsService, SecurityService}
import scala.collection.JavaConverters._

@Singleton
class ColourSchemesController @Inject()(
  security: SecurityService,
  configuration: Configuration
) extends BaseController {

  sealed case class Background(
    id: Int,
    url: String,
    name: String
  )

  implicit private val BackgroundWriter = Json.writes[Background]



  val backgrounds: Seq[Background] = configuration.getConfigList("mywarwick.backgrounds")
    .getOrElse(throw new IllegalStateException("mywarwick.backgrounds is not configured in default.conf."))
    .asScala.map(e => {
    Background(
      e.getInt("id").getOrElse(throw new IllegalStateException("id is not set for this background")),
      e.getString("url").getOrElse(throw new IllegalStateException("url is not set for this background")),
      e.getString("name").getOrElse(throw new IllegalStateException("name is not set for this background"))
    )
  })

  import security._
  def getChoice = RequiredUserAction { request =>
    val user = request.context.user.get

    val data = JsObject(Map(
      "chosenColourScheme" -> JsNumber(1) // TODO get the chosen one from db
    ))

    Ok(Json.toJson(API.Success(data = data)))
  }

  def getAvailable = Action { request =>
    val data = JsObject(Map(
      "schemes" -> JsArray(backgrounds.map(e => {
        Json.toJson(e)
      }))
    ))

    Ok(Json.toJson(API.Success(data = data)))
  }

}
