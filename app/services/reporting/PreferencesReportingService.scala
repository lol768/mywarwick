package services.reporting

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.{ActivityProvider, ActivityType}
import org.joda.time.DateTime
import services.dao.{ActivityMuteDao, UserPreferencesDao}
import warwick.sso.Usercode

@ImplementedBy(classOf[PreferencesReportingServiceImpl])
trait PreferencesReportingService {

  def userWantsEmails(usercode: Usercode): Boolean

  def userWantsSMS(usercode: Usercode): Boolean

  def usersWhoMutedProvider(
    providerId: ActivityProvider,
    activityType: Option[ActivityType],
    createdAt: Option[DateTime],
    expiresAt: Option[DateTime]
  ): Option[Seq[Usercode]]

}


@Singleton
class PreferencesReportingServiceImpl @Inject()(
  userPreferencesDao: UserPreferencesDao,
  activityMuteDao: ActivityMuteDao
) extends PreferencesReportingService {
  override def userWantsEmails(usercode: Usercode) = ???

  override def userWantsSMS(usercode: Usercode) = ???

  override def usersWhoMutedProvider(providerId: ActivityProvider, activityType: Option[ActivityType], createdAt: Option[DateTime], expiresAt: Option[DateTime]) = ???
}
