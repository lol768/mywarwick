package controllers

import com.google.inject.Inject

import play.api.inject.ApplicationLifecycle
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import services.healthcheck.HealthCheck

import scala.collection.JavaConversions._
import scala.concurrent.Future

class ServiceCheckController @Inject()(
  life: ApplicationLifecycle,
  healthChecks: java.util.Set[HealthCheck[_]]
) extends Controller {

  var stopping = false
  life.addStopHook(() => {
    stopping = true
    Future.successful(Unit)
  })

  def gtg = Action {
    if (stopping)
      ServiceUnavailable("Shutting down")
    else
      Ok("\"OK\"")
  }

  def healthcheck = Action {
    Ok(Json.obj("data" -> JsArray(healthChecks.toList.sortBy(_.name).map(_.toJson).toSeq)))
  }

}
