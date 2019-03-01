package controllers.api

import com.google.inject.Inject
import controllers.MyController
import javax.inject.Singleton
import models.{API, EAPFeatureRender}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._
import services._

@Singleton
class EAPController @Inject()(
  security: SecurityService,
  userPreferences: UserPreferencesService,
  featuresService: FeaturesService,
  eapFeaturesService: EAPFeaturesService
) extends MyController {

  import security._

  private val eapDurationInMonths = 3

  def getPreference = RequiredUserAction { request =>
    val user = request.context.user.get

    val data = JsObject(Map(
      "enabled" -> JsBoolean(userPreferences.getFeaturePreferences(user.usercode).eap)
    ))

    Ok(Json.toJson(API.Success(data = data)))
  }

  def updatePreference = RequiredUserAction { implicit request =>
    request.context.user.map { user =>
      request.body.asJson.flatMap(_.asInstanceOf[JsObject].value.get("enabled")).map(_.as[Boolean]).map { enabled =>
        val oldPref = userPreferences.getFeaturePreferences(user.usercode)
        val newEnabledUntil = if (enabled) Some(DateTime.now.plusMonths(eapDurationInMonths)) else None
        val newPref = oldPref.copy(eapUntil = newEnabledUntil)
        userPreferences.setFeaturePreferences(user.usercode, newPref)
        auditLog('UpdateEAP,
          'enabled -> newEnabledUntil.nonEmpty,
          'until -> newEnabledUntil.map(ISODateTimeFormat.dateTime().print)
        )
        Ok(Json.obj(
          "success" -> true,
          "data" -> Json.obj(
            "enabled" -> newPref.eap
          ),
          "status" -> "ok"
        ))
      }.getOrElse(BadRequest("Must specify new preference value"))
    }.get
  }

  def tile = RequiredUserAction { request =>
    if (featuresService.get(request.context.user).eap) {
      Ok(Json.toJson(API.Success(data = Json.obj(
        "items" -> Json.toJson(
          eapFeaturesService.all.filter(_.available()).sorted
        )(Writes.seq(EAPFeatureRender.writes)),
        "defaultText" -> "No Early Access features are currently available"
      ))))
    } else {
      BadRequest("Early Access Program feature not enabled for user")
    }
  }
}
