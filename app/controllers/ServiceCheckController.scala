package controllers

import javax.inject.Singleton

import com.google.inject.Inject

import play.api.inject.ApplicationLifecycle
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._
import services.healthcheck.HealthCheck

import scala.collection.JavaConverters._
import scala.concurrent.Future

@Singleton
class ServiceCheckController @Inject()(
  life: ApplicationLifecycle,
  healthChecks: java.util.Set[HealthCheck[_]]
) extends InjectedController {

  private val checks = healthChecks.asScala.toList

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
    Ok(Json.obj("data" -> JsArray(checks.sortBy(_.name).map(_.toJson).toSeq)))
  }

}
