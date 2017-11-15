package services.reporting

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.{ActivityMute, ActivityProvider, ActivityType}
import org.joda.time.Interval
import play.api.db.{Database, NamedDatabase}
import services.dao.{ActivityMuteDao, UserPreferencesDao}
import warwick.sso.Usercode

@ImplementedBy(classOf[PreferencesReportingServiceImpl])
trait PreferencesReportingService {

  def userWantsEmails(usercode: Usercode): Boolean

  def userWantsSMS(usercode: Usercode): Boolean

  def getMutesByProviders(providers: Seq[ActivityProvider]): Map[ActivityProvider, Seq[ActivityMute]]

  def getMutesCountByProviders(providers: Seq[ActivityProvider]): Int

  def getAllMutesGroupedByProviders(): Map[ActivityProvider, Seq[ActivityMute]]

}


@Singleton
class PreferencesReportingServiceImpl @Inject()(
  @NamedDatabase("default") db: Database,
  userPreferencesDao: UserPreferencesDao,
  activityMuteDao: ActivityMuteDao
) extends PreferencesReportingService {
  override def userWantsEmails(usercode: Usercode) = ???

  override def userWantsSMS(usercode: Usercode) = ???

  override def getMutesByProviders(providers: Seq[ActivityProvider]): Map[ActivityProvider, Seq[ActivityMute]] = {
    db.withConnection(implicit c => activityMuteDao.mutesForProviders(providers))
  }

  override def getMutesCountByProviders(providers: Seq[ActivityProvider]) = {
    db.withConnection(implicit c => activityMuteDao.mutesCountForProviders(providers))
  }

  override def getAllMutesGroupedByProviders() = {
    db.withConnection(implicit c => activityMuteDao.allMutesGroupedByProviders())
  }
}
