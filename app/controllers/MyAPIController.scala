package controllers

import javax.inject.Inject

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._
import play.api.mvc.Controller
import services.SecurityService

/**
  * Testing out auth.
  */
class MyAPIController @Inject() (security: SecurityService) extends Controller {
  import security._

  def postActivities(appId: String) = APIAction { request =>
    Ok(Json.obj(
      "app" -> appId,
      "user" -> request.context.user.get.name.full,
      "fake-response" -> "hell yes",
      "timestamp" -> ISODateTimeFormat.basicDateTime.print(DateTime.now)
    ))
  }

}
