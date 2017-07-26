package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.{API, PageViewHit}
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc.{Action, RequestHeader}
import services.{NewsService, SecurityService, UserPreferencesService}

import scala.collection.JavaConverters._

@Singleton
class ColourSchemesController @Inject()(
  security: SecurityService,
  configuration: Configuration,
  userPreferencesService: UserPreferencesService
) extends BaseController {

  import security._


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


  def get = RequiredUserAction { request =>

    val chosenColourScheme = request.context.user.map(
      u => userPreferencesService.getChosenColourScheme(u.usercode)
    )

    val data = JsObject(Map(
      "chosen" -> JsNumber(BigDecimal(chosenColourScheme.getOrElse(1))),
      "schemes" -> JsArray(backgrounds.map(e => {
        Json.toJson(e)
      }))
    ))

    Ok(Json.toJson(API.Success(data = data)))
  }

}
