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

  lazy val config = Configuration.load(
    Environment.simple(),
    Map("config.file" -> "test/test.conf")
  )

  implicit override lazy val app =
    new GuiceApplicationBuilder(loadConfiguration = _ => config)
      .in(Environment.simple())
      .configure(inMemoryDatabase("default", Map("MODE" -> "Oracle")))
      .bindings(
        bind[LoginContext].toInstance(new LoginContext {
          override val user: Option[User] = None
          override val actualUser: Option[User] = None

          override def loginUrl(target: Option[String]): String = "https://example.com/login"
        })
      )
      .overrides(
        bind[SSOClient].to[MockSSOClient],
        bind[DatabaseDialect].to[H2DatabaseDialect]
      )
      .build()

  def database(block: Connection => Unit): Unit =
    transaction(rollback = false)(block)

  def transaction(block: Connection => Unit): Unit =
    transaction(rollback = true)(block)

  def transaction(rollback: Boolean)(block: Connection => Unit): Unit = {
    val database = app.injector.instanceOf[Database]
    val connection = database.getConnection(autocommit = false)

    block(connection)

    if (rollback)
      connection.rollback()

    connection.setAutoCommit(true)
  }

}
