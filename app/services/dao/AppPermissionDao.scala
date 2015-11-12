package services.dao

import anorm.SqlParser._
import com.google.inject.{ImplementedBy, Inject}
import play.api.db.Database
import play.db.NamedDatabase

@ImplementedBy(classOf[AppPermissionDaoImpl])
trait AppPermissionDao {

  def canUserPostForApp(appId: String, usercode: String): Boolean

}

class AppPermissionDaoImpl @Inject()(@NamedDatabase("default") db: Database) extends AppPermissionDao {

  private val NO_PERMISSION = 0

  override def canUserPostForApp(appId: String, usercode: String): Boolean =
    db.withConnection { implicit c =>
      anorm.SQL("SELECT COUNT(*) FROM APP_PERMISSION WHERE APP_ID = {appId} AND USERCODE = {usercode}")
        .on(
          'appId -> appId,
          'usercode -> usercode
        )
        .as(scalar[Int].single) > NO_PERMISSION
    }

}
