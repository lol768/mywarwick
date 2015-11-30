package services.dao

import java.sql.{Connection, SQLException}

import anorm.SQL
import anorm.SqlParser._
import com.google.inject.ImplementedBy

@ImplementedBy(classOf[ProviderPermissionDaoImpl])
trait ProviderPermissionDao {

  def canUserPostForProvider(providerId: String, usercode: String)(implicit c: Connection): Boolean

  def allow(providerId: String, usercode: String)(implicit c: Connection): Unit

  def disallow(providerId: String, usercode: String)(implicit c: Connection): Unit

}

class ProviderPermissionDaoImpl extends ProviderPermissionDao {

  private val NO_PERMISSION = 0

  override def canUserPostForProvider(providerId: String, usercode: String)(implicit c: Connection): Boolean =
    SQL("SELECT COUNT(*) FROM PROVIDER_PERMISSION WHERE PROVIDER_ID = {providerId} AND USERCODE = {usercode}")
      .on(
        'providerId -> providerId,
        'usercode -> usercode
      )
      .as(scalar[Int].single) > NO_PERMISSION

  override def allow(providerId: String, usercode: String)(implicit c: Connection): Unit =
    try {
      SQL("INSERT INTO PROVIDER_PERMISSION (PROVIDER_ID, USERCODE) VALUES ({providerId}, {usercode})")
        .on(
          'providerId -> providerId,
          'usercode -> usercode
        )
        .execute()
    } catch {
      case _: SQLException => // catch UNIQUE INDEX violation if the PROVIDER_PERMISSION already exists
    }

  override def disallow(providerId: String, usercode: String)(implicit c: Connection): Unit =
    SQL("DELETE FROM PROVIDER_PERMISSION WHERE PROVIDER_ID = {providerId} and USERCODE = {usercode}")
      .on(
        'providerId -> providerId,
        'usercode -> usercode
      )
      .execute()

}
