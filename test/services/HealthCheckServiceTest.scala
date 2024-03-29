package services

import helpers.{BaseSpec, WithActorSystem}
import org.mockito.Mockito.{atLeast => atLeastTimes, _}
import org.scalatest.concurrent.Eventually
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Span}
import services.messaging.MessagingService

import scala.concurrent.duration._

/**
  *
  */
class HealthCheckServiceTest extends BaseSpec with MockitoSugar with WithActorSystem with Eventually {
  override implicit def patienceConfig = PatienceConfig(scaled(Span(500, Millis)), scaled(Span(10, Millis)))

  "HealthCheckService" should {
    "continue after an exception" in {
      val messaging = mock[MessagingService]
      when(messaging.getQueueStatus).thenReturn(Nil)
      when(messaging.getOldestUnsentMessageCreatedAt).thenThrow(new RuntimeException("DB ERROR"))

      val activityService = mock[ActivityService]
      when(activityService.countNotificationsByPublishersInLast48Hours).thenReturn(Nil)

      val service = new HealthCheckService(messaging, akka, activityService) {
        override def frequency: FiniteDuration = 1.millis
      }

      eventually {
        // if it crashed, it will only ever run 1 time.
        verify(messaging, atLeastTimes(2)).getQueueStatus
      }
    }
  }
}
