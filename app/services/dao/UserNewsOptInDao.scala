package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Singleton}
import models.Audience
import models.Audience.LocationOptIn
import warwick.core.Logging
import warwick.sso.Usercode

@ImplementedBy(classOf[UserNewsOptInDaoImpl])
trait UserNewsOptInDao {

  def get(usercode: Usercode)(implicit c: Connection): Seq[Audience.OptIn]

  def save(usercode: Usercode, optInType: String, optIns: Seq[Audience.OptIn])(implicit c: Connection): Unit

  def getUsercodes(optIn: Audience.OptIn)(implicit c: Connection): Set[Usercode]

}

@Singleton
class UserNewsOptInDaoImpl extends UserNewsOptInDao with Logging {

  override def get(usercode: Usercode)(implicit c: Connection): Seq[Audience.OptIn] = {
    SQL"SELECT NAME, VALUE FROM USER_NEWS_OPT_IN WHERE USERCODE = ${usercode.string}"
      .as((str("name") ~ str("value")).*).flatMap { case name ~ value => name match {
        case LocationOptIn.optInType =>
          LocationOptIn.fromValue(value).orElse {
            logger.warn(s"Unknown LocationOptIn $value")
            None
          }
        case _ =>
          logger.warn(s"Unknown optInType $name")
          None
      }}
  }

  override def save(usercode: Usercode, optInType: String, optIns: Seq[Audience.OptIn])(implicit c: Connection): Unit = {
    SQL"DELETE FROM USER_NEWS_OPT_IN WHERE USERCODE = ${usercode.string} AND NAME = $optInType"
      .execute()

    optIns.foreach(optIn =>
      SQL"INSERT INTO USER_NEWS_OPT_IN (USERCODE, NAME, VALUE) VALUES (${usercode.string}, $optInType, ${optIn.optInValue})"
        .execute()
    )
  }

  override def getUsercodes(optIn: Audience.OptIn)(implicit c: Connection): Set[Usercode] = {
    SQL"""
      SELECT USERCODE FROM USER_NEWS_OPT_IN
      WHERE NAME = ${optIn.optInType} AND VALUE = ${optIn.optInValue}
    """.as(str("usercode").*)
      .map(Usercode).toSet
  }

}
