package services.messaging

import com.google.inject.{ImplementedBy, Inject}
import models.messaging.DoNotDisturbPeriod
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import services.FeaturesService
import services.dao.DoNotDisturbDao
import warwick.core.Logging
import warwick.sso.Usercode

@ImplementedBy(classOf[DoNotDisturbServiceImpl])
trait DoNotDisturbService {
  def get(user: Usercode): Option[DoNotDisturbPeriod]

  def set(user: Usercode, doNotDisturbPeriod: DoNotDisturbPeriod): Unit

  def getRescheduleTime(user: Usercode): Option[DateTime]

  def disable(user: Usercode): Int
}

class DoNotDisturbServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  dao: DoNotDisturbDao,
  featuresService: FeaturesService
) extends DoNotDisturbService with Logging {

  override def get(user: Usercode): Option[DoNotDisturbPeriod] =
    db.withConnection { implicit connection =>
      dao.get(user)
    }

  override def getRescheduleTime(usercode: Usercode): Option[DateTime] =
    if (featuresService.get(usercode).doNotDisturb) reschedule(usercode)
    else None

  private def reschedule (user: Usercode): Option[DateTime] = {
    val now: DateTime = DateTime.now
    val nowHr: Int = now.getHourOfDay
    val nowMin: Int = now.getMinuteOfHour

    get(user).flatMap { dnd =>
      val reschedDate: DateTime = now.withHourOfDay(dnd.end.hr).withMinuteOfHour(dnd.end.min)
      if (dnd.spansDays) {
        if (dnd.startIsBeforeOrEqual(nowHr, nowMin))
          Some(reschedDate.plusDays(1))
        else if (dnd.endIsAfter(nowHr, nowMin))
          Some(reschedDate)
        else
          None
      }
      else if (dnd.startIsBeforeOrEqual(nowHr, nowMin) && dnd.endIsAfter(nowHr, nowMin)) {
        Some(reschedDate)
      }
      else {
        None
      }
    }
  }

  override def set(user: Usercode, doNotDisturbPeriod: DoNotDisturbPeriod): Unit =
    db.withTransaction { implicit connection =>
      dao.set(user, doNotDisturbPeriod)
    }

  override def disable(user: Usercode): Int =
    db.withTransaction { implicit connection =>
      dao.disable(user)
    }
}
