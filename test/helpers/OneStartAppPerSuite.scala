package helpers

import java.sql.Connection

import org.scalatest.Suite
import org.scalatestplus.play.OneAppPerSuite
import play.api.db.Database
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import system.{DatabaseDialect, H2DatabaseDialect}
import warwick.sso._

trait OneStartAppPerSuite extends Suite with OneAppPerSuite {

  implicit override lazy val app = TestApplications.full()

  def transaction(block: Connection => Unit): Unit =
    transaction(rollback = true)(block)

  def transaction(rollback: Boolean)(block: Connection => Unit): Unit = {
    val database = app.injector.instanceOf[Database]
    val connection = database.getConnection(autocommit = false)

    try block(connection)
    finally {
      if (rollback)
        connection.rollback()

      connection.setAutoCommit(true)
    }
  }

}
