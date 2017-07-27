package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.API
import play.api.Configuration
import play.api.libs.json._
import services.{SecurityService, UserPreferencesService}

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

  lazy val backgroundsMap: Map[Int, Background] = backgrounds.groupBy(_.id).map(i => i._1 -> i._2.head)

  def get = UserAction { request =>

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

  def persist = RequiredUserAction { request =>
    val user = request.context.user.get
    request.body.asJson.collect { case o: JsObject => o }.map { jsObject =>
      // generally be forgiving here and default to 1
      val intendedValue = jsObject.value.getOrElse("colourScheme", 1)
      val chosenScheme = intendedValue match {
        case a: JsNumber => if (backgroundsMap.contains(a.value.intValue())) a.value.intValue() else 1
        case _ => 1
      }

      userPreferencesService.setChosenColourScheme(user.usercode, chosenScheme)
      Ok(Json.toJson(API.Success("ok", Json.toJson(backgroundsMap(chosenScheme)))))
    }.getOrElse(BadRequest(Json.toJson(API.Failure[JsObject]("bad request", Seq(API.Error("invalid-body", "Body must be JSON-formatted"))))))

  }

}
