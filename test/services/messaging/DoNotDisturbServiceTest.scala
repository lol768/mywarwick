package services.messaging

import java.sql.Connection
import java.time._

import helpers.BaseSpec
import models.messaging.DoNotDisturbPeriod
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import services.{Features, FeaturesService, MockDatabase}
import services.dao.{DoNotDisturbDao, MessagingDao}
import uk.ac.warwick.util.core.DateTimeUtils
import warwick.sso.Usercode

class DoNotDisturbServiceTest extends BaseSpec with MockitoSugar with BeforeAndAfterEach {
  val dao: DoNotDisturbDao = mock[DoNotDisturbDao]
  val messagingDao: MessagingDao = mock[MessagingDao]
  val featuresService: FeaturesService = mock[FeaturesService]
  val features: Features = mock[Features]
  when(features.doNotDisturb).thenReturn(true)
  when(featuresService.get(any[Usercode])).thenReturn(features)

  val service: DoNotDisturbService = new DoNotDisturbServiceImpl(new MockDatabase(), dao, messagingDao, featuresService)
  val fred: Usercode = Usercode("fred")

  val testDate: ZonedDateTime = LocalDateTime.parse("2018-06-20T00:00").atZone(ZoneId.systemDefault())
  val dayOfMonth: Int = testDate.getDayOfMonth

  def updateDnd(startHr: Int, endHr: Int, startMin: Int = 0, endMin: Int = 0): OngoingStubbing[Option[DoNotDisturbPeriod]] =
    when(dao.get(any[Usercode])(any[Connection])).thenReturn(Some(DoNotDisturbPeriod(LocalTime.of(startHr, startMin), LocalTime.of(endHr, endMin))))

  def withTime[A](hr: Int, min: Int = 0)(fn: Clock => A): A =
    fn(Clock.fixed(testDate.withHour(hr).withMinute(min).toInstant, ZoneId.systemDefault()))

  override def afterEach(): Unit = Clock.systemDefaultZone()

  "DoNotDisturbService" should {
    "reschedule for same day" in withTime(12) { implicit clock =>
      updateDnd(9, 18)
      val rescheduled = service.getRescheduleTime(fred).get
      rescheduled.getDayOfMonth must be(dayOfMonth)
      rescheduled.getHour must be(18)
    }

    "reschedule (hour is dnd start hour)" in withTime(9) { implicit clock =>
      updateDnd(9, 18)
      val rescheduled = service.getRescheduleTime(fred).get
      rescheduled.getDayOfMonth must be(dayOfMonth)
      rescheduled.getHour must be(18)
    }

    "no reschedule (hour after dnd)" in withTime(21) { implicit clock =>
      updateDnd(9, 18)
      service.getRescheduleTime(fred) must be (Option.empty[ZonedDateTime])
    }

    "no reschedule (hour before dnd)" in withTime(8) { implicit clock =>
      updateDnd(9, 18)
      service.getRescheduleTime(fred) must be (Option.empty[ZonedDateTime])
    }

    "no reschedule (hour is dnd end hour)" in withTime(18) { implicit clock =>
      updateDnd(9, 18)
      service.getRescheduleTime(fred) must be (Option.empty[ZonedDateTime])
    }

    "reschedule for next day (dnd spans day)" in withTime(23) { implicit clock =>
      updateDnd(21, 7)
      val rescheduled = service.getRescheduleTime(fred).get
      rescheduled.getDayOfMonth must be(dayOfMonth + 1)
      rescheduled.getHour must be(7)
    }

    "reschedule for same day (dnd spans day)" in withTime(3) { implicit clock =>
      updateDnd(21, 7)
      val rescheduled = service.getRescheduleTime(fred).get
      rescheduled.getDayOfMonth must be(dayOfMonth)
      rescheduled.getHour must be(7)
    }

    "reschedule with zero hour start" in withTime(3) { implicit clock =>
      updateDnd(0, 7)
      val rescheduled = service.getRescheduleTime(fred).get
      rescheduled.getDayOfMonth must be(dayOfMonth)
      rescheduled.getHour must be(7)
    }

    "reschedule with zero hour end" in withTime(23) { implicit clock =>
      updateDnd(21, 0)
      val rescheduled = service.getRescheduleTime(fred).get
      rescheduled.getDayOfMonth must be(dayOfMonth + 1)
      rescheduled.getHour must be(0)
    }

    "no reschedule with zero hour start" in withTime(8) { implicit clock =>
      updateDnd(0, 7)
      service.getRescheduleTime(fred) must be (Option.empty[ZonedDateTime])
    }

    "no reschedule with zero hour end" in withTime(3) { implicit clock =>
      updateDnd(21, 0)
      service.getRescheduleTime(fred) must be (Option.empty[ZonedDateTime])
    }
  }
}
