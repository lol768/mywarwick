package services

import java.sql.{Connection, SQLIntegrityConstraintViolationException}

import com.google.inject.{ImplementedBy, Inject, Singleton}
import play.api.db.{Database, NamedDatabase}
import services.dao.{NewsCategoryDao, UserNewsCategoryDao, UserPreferencesDao}
import system.Logging
import warwick.sso.Usercode

import scala.util.{Failure, Success, Try}

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
    Try(preferences.save(usercode)) match {
      case Success(_) => initialiseNewsCategories(usercode)
      case Failure(_: SQLIntegrityConstraintViolationException) => // user already initialised, do nothing
      case Failure(e) => throw e
    }
  }

  def initialiseNewsCategories(usercode: Usercode)(implicit c: Connection): Unit = {
    val defaultCategories = newsCategoryDao.all()
    userNewsCategoryDao.setSubscribedCategories(usercode, defaultCategories.map(_.id))
  }

}
