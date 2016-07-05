package services

import java.sql.Connection

import com.google.inject.{ImplementedBy, Inject, Singleton}
import play.api.db.{Database, NamedDatabase}
import services.dao.{NewsCategoryDao, UserNewsCategoryDao, UserPreferencesDao}
import system.Logging
import warwick.sso.Usercode

@ImplementedBy(classOf[UserInitialisationServiceImpl])
trait UserInitialisationService {

  def maybeInitialiseUser(usercode: Usercode): Unit

}

@Singleton
class UserInitialisationServiceImpl @Inject()(
  preferences: UserPreferencesDao,
  newsCategoryDao: NewsCategoryDao,
  userNewsCategoryDao: UserNewsCategoryDao,
  @NamedDatabase("default") db: Database
) extends UserInitialisationService with Logging {

  override def maybeInitialiseUser(usercode: Usercode) = db.withTransaction { implicit c =>
    if (!preferences.exists(usercode)) {
      logger.info(s"Performing initialisation for user $usercode")
      initialiseUser(usercode)
    }
  }

  def initialiseUser(usercode: Usercode)(implicit c: Connection): Unit = {
    preferences.save(usercode)

    initialiseNewsCategories(usercode)
  }

  def initialiseNewsCategories(usercode: Usercode)(implicit c: Connection): Unit = {
    val defaultCategories = newsCategoryDao.all()
    userNewsCategoryDao.setSubscribedCategories(usercode, defaultCategories.map(_.id))
  }

}
