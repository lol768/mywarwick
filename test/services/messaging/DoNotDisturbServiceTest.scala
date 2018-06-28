package services.messaging

import java.sql.Connection

import helpers.BaseSpec
import models.messaging.{DoNotDisturbPeriod, Time}
import org.joda.time.{DateTime, DateTimeUtils}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import services.{Features, FeaturesService, MockDatabase}
import services.dao.{DoNotDisturbDao, MessagingDao}
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

  val testDate: DateTime = DateTime.parse("2018-06-20T00:00")
  val dayOfMonth: Int = testDate.getDayOfMonth

  def updateDnd(startHr: Int, endHr: Int, startMin: Int = 0, endMin: Int = 0): OngoingStubbing[Option[DoNotDisturbPeriod]] =
    when(dao.get(any[Usercode])(any[Connection])).thenReturn(Some(DoNotDisturbPeriod(Time(startHr, startMin), Time(endHr, endMin))))

  def setTime(hr: Int, min: Int = 0): Unit =
    DateTimeUtils.setCurrentMillisFixed(testDate.withHourOfDay(hr).withMinuteOfHour(min).getMillis)

  override def afterEach: Unit = DateTimeUtils.setCurrentMillisSystem()

  "DoNotDisturbService" should {
    "reschedule for same day" in {
      updateDnd(9, 18)
      setTime(12)
      val rescheduled = service.getRescheduleTime(fred).get
      rescheduled.getDayOfMonth must be(dayOfMonth)
      rescheduled.getHourOfDay must be(18)
    }

    "reschedule (hour is dnd start hour)" in {
      updateDnd(9, 18)
      setTime(9)
      val rescheduled = service.getRescheduleTime(fred).get
      rescheduled.getDayOfMonth must be(dayOfMonth)
      rescheduled.getHourOfDay must be(18)
    }

    "no reschedule (hour after dnd)" in {
      updateDnd(9, 18)
      setTime(21)
      service.getRescheduleTime(fred) must be (Option.empty[DateTime])
    }

    "no reschedule (hour before dnd)" in {
      updateDnd(9, 18)
      setTime(8)
      service.getRescheduleTime(fred) must be (Option.empty[DateTime])
    }

    "no reschedule (hour is dnd end hour)" in {
      updateDnd(9, 18)
      setTime(18)
      service.getRescheduleTime(fred) must be (Option.empty[DateTime])
    }

    "reschedule for next day (dnd spans day)" in {
      updateDnd(21, 7)
      setTime(23)
      val rescheduled = service.getRescheduleTime(fred).get
      rescheduled.getDayOfMonth must be(dayOfMonth + 1)
      rescheduled.getHourOfDay must be(7)
    }

    "reschedule for same day (dnd spans day)" in {
      updateDnd(21, 7)
      setTime(3)
      val rescheduled = service.getRescheduleTime(fred).get
      rescheduled.getDayOfMonth must be(dayOfMonth)
      rescheduled.getHourOfDay must be(7)
    }

    "reschedule with zero hour start" in {
      updateDnd(0, 7)
      setTime(3)
      val rescheduled = service.getRescheduleTime(fred).get
      rescheduled.getDayOfMonth must be(dayOfMonth)
      rescheduled.getHourOfDay must be(7)
    }

    "reschedule with zero hour end" in {
      updateDnd(21, 0)
      setTime(23)
      val rescheduled = service.getRescheduleTime(fred).get
      rescheduled.getDayOfMonth must be(dayOfMonth + 1)
      rescheduled.getHourOfDay must be(0)
    }

    "no reschedule with zero hour start" in {
      updateDnd(0, 7)
      setTime(8)
      service.getRescheduleTime(fred) must be (Option.empty[DateTime])
    }

    "no reschedule with zero hour end" in {
      updateDnd(21, 0)
      setTime(3)
      service.getRescheduleTime(fred) must be (Option.empty[DateTime])
    }
  }
}
