package system

import javax.inject.Inject

import com.codahale.metrics.{Counter, Timer}
import com.google.inject.ImplementedBy
import com.kenshoo.play.metrics.Metrics
import system.AppMetrics.RequestTracker

object AppMetrics {
  trait RequestTracker {
    def stop(): Unit
  }
}

@ImplementedBy(classOf[AppMetricsImpl])
trait AppMetrics {
  def websocketTracker() : RequestTracker

  def websocketsTimer: Timer
  def websocketsCounter: Counter
}

class AppMetricsImpl @Inject() (
  metrics: Metrics
) extends AppMetrics {

  override val websocketsTimer = metrics.defaultRegistry.timer("websockets")
  override val websocketsCounter = metrics.defaultRegistry.counter("websocketsCount")

  def websocketTracker() = new RequestTracker {
    val time = websocketsTimer.time()
    websocketsCounter.inc()
    override def stop(): Unit = {
      websocketsCounter.dec()
      time.stop()
    }
  }

}
