package controllers

import javax.inject.{Singleton, Inject}

import com.kenshoo.play.metrics.{MetricsFilterImpl, MetricsFilter, Metrics}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Action

@Singleton
class MetricsController @Inject() (
  m: Metrics,
  filter: MetricsFilter // for http
) extends BaseController {

  import Json._

  private val websockets = m.defaultRegistry.timer("websockets")
  private val websocketsCount = m.defaultRegistry.counter("websocketsCount")

  def metrics = Action {
    Ok(obj(
      "websockets" -> obj(
        "current" -> websocketsCount.getCount,
        "total" -> websockets.getCount,
        "01min" -> websockets.getOneMinuteRate,
        "05min" -> websockets.getFiveMinuteRate,
        "15min" -> websockets.getFifteenMinuteRate
      ),
      "web" -> webStats
    ))
  }

  @inline
  private def webStats: JsObject = filter match {
    case f: MetricsFilterImpl => obj(
      "current" -> f.activeRequests.getCount,
      "total" -> f.requestsTimer.getCount,
      "01min" -> f.requestsTimer.getOneMinuteRate,
      "05min" -> f.requestsTimer.getFiveMinuteRate,
      "15min" -> f.requestsTimer.getFifteenMinuteRate,
      "status" -> f.statusCodes.map {
        case (status, meter) => status.toString -> obj(
          "total" -> meter.getCount,
          "01min" -> meter.getOneMinuteRate,
          "05min" -> meter.getFiveMinuteRate,
          "15min" -> meter.getFifteenMinuteRate
        )
      }
    )
    case _ => Json.obj()
  }

}
