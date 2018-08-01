package services.messaging

import java.time._

import com.google.inject.{ImplementedBy, Inject}
import models.messaging.DoNotDisturbPeriod
import play.api.db.{Database, NamedDatabase}
import services.FeaturesService
import services.dao.{DoNotDisturbDao, MessagingDao}
import warwick.core.Logging
import warwick.sso.Usercode

@ImplementedBy(classOf[DoNotDisturbServiceImpl])
trait DoNotDisturbService {
  def get(user: Usercode): Option[DoNotDisturbPeriod]

  def set(user: Usercode, doNotDisturbPeriod: DoNotDisturbPeriod): Unit

  def getRescheduleTime(user: Usercode)(implicit clock: Clock = Clock.systemDefaultZone()): Option[ZonedDateTime]

  def disable(user: Usercode): Int
}

class DoNotDisturbServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  dao: DoNotDisturbDao,
  messagingDao: MessagingDao,
  featuresService: FeaturesService
) extends DoNotDisturbService with Logging {

  override def get(user: Usercode): Option[DoNotDisturbPeriod] =
    db.withConnection { implicit connection =>
      dao.get(user)
    }

  override def getRescheduleTime(usercode: Usercode)(implicit clock: Clock = Clock.systemDefaultZone()): Option[ZonedDateTime] =
    if (featuresService.get(usercode).doNotDisturb) reschedule(usercode)(clock)
    else None

  private def reschedule (user: Usercode)(implicit clock: Clock = Clock.systemDefaultZone()): Option[ZonedDateTime] = {
    val now: LocalTime = LocalTime.now(clock)

    get(user).flatMap { dnd =>
      val reschedDate: ZonedDateTime = ZonedDateTime.of(LocalDate.now(clock), dnd.end, ZoneId.systemDefault())
      if (dnd.spansDays) {
        if (!dnd.start.isAfter(now))
          Some(reschedDate.plusDays(1))
        else if (dnd.end.isAfter(now))
          Some(reschedDate)
        else
          None
      }
      else if (!dnd.start.isAfter(now) && dnd.end.isAfter(now)) {
        Some(reschedDate)
      }
      else {
        None
      }
    }
  }

  override def set(user: Usercode, doNotDisturbPeriod: DoNotDisturbPeriod): Unit = {
    db.withTransaction { implicit connection =>
      dao.set(user, doNotDisturbPeriod)
    }
    db.withTransaction { implicit connection =>
      messagingDao.updateMessageSendAtForUser(user, reschedule(user))
    }
  }

  override def disable(user: Usercode): Int =
    db.withTransaction { implicit connection =>
      messagingDao.updateMessageSendAtForUser(user, None)
      dao.disable(user)
    }
}
