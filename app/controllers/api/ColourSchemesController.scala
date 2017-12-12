package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.MyController
import models.API
import play.api.Configuration
import play.api.libs.json._
import services.{SecurityService, UserPreferencesService}

import scala.collection.JavaConverters._

case class ColourScheme(schemeId: Int, highContrast: Boolean)

@Singleton
class ColourSchemesController @Inject()(
  security: SecurityService,
  configuration: Configuration,
  userPreferencesService: UserPreferencesService
) extends MyController {

  import security._

  sealed case class Background(
    id: Int,
    url: String,
    name: String,
    schemeColour: String
  )

  implicit private val BackgroundWriter = Json.writes[Background]

  val backgrounds: Seq[Background] = configuration.underlying.getConfigList("mywarwick.backgrounds")
    .asScala.map(e => {
    Background(
      e.getInt("id"),
      e.getString("url"),
      e.getString("name"),
      e.getString("schemeColour")
    )
  })

  lazy val backgroundsMap: Map[Int, Background] = backgrounds.groupBy(_.id).mapValues(_.head)

  def get = UserAction { request =>

    val chosenColourScheme = request.context.user.map(
      u => userPreferencesService.getChosenColourScheme(u.usercode)
    )

    val data = JsObject(Map(
      "chosen" -> JsNumber(BigDecimal(chosenColourScheme.map(_.schemeId).getOrElse(1))),
      "isHighContrast" -> JsBoolean(chosenColourScheme.exists(_.highContrast)),
      "schemes" -> JsArray(backgrounds.map(e => {
        Json.toJson(e)
      }))
    ))

    Ok(Json.toJson(API.Success(data = data)))
  }

  def persist = RequiredUserAction { request =>
    val user = request.context.user.get
    request.body.asJson.collect { case o: JsObject => o }.map { jsObject =>
      val highContrast = (jsObject \ "isHighContrast").getOrElse(JsBoolean(false)).as[Boolean]
      // generally be forgiving here and default to 1
      val intendedSchemeId = jsObject.value.getOrElse("colourScheme", 1)
      val chosenSchemeId = intendedSchemeId match {
        case a: JsNumber => if (backgroundsMap.contains(a.value.intValue())) a.value.intValue() else 1
        case _ => 1
      }
      val newScheme = ColourScheme(chosenSchemeId, highContrast)

      userPreferencesService.setChosenColourScheme(user.usercode, newScheme)
      Ok(Json.toJson(API.Success("ok", Json.toJson(backgroundsMap(newScheme.schemeId)))))
    }.getOrElse(BadRequest(Json.toJson(API.Failure[JsObject]("bad request", Seq(API.Error("invalid-body", "Body must be JSON-formatted"))))))

  }

}
