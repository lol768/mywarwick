package controllers

import javax.inject.Inject

import models.API
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._
import play.api.mvc.Controller
import services.SecurityService

/**
  * Testing out auth. This can go away as soon as we start using it in
  * an actual API controller.
  */
class MyAPIController @Inject()(
  security: SecurityService
) extends Controller {

  import security._

  def postActivities(appId: String) = APIAction { request =>
    Ok(Json.obj(
      "app" -> appId,
      "user" -> request.context.user.get.name.full,
      "fake-response" -> "hell yes",
      "timestamp" -> ISODateTimeFormat.basicDateTime.print(DateTime.now)
    ))
  }

  def myActivity = APIAction { request =>
    request.context.user.map { user =>
      Ok(API.myActivityResponse(user))
    }.getOrElse {
      // TODO Should never get here as we require a user. Bit bums to have to handle it. Thinks...
      // APIAction should allow anon users anyway, so maybe we should handle it here and not there.
      BadRequest("No user! This was unexpected.")
    }
  }

}
