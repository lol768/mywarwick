package helpers

import org.junit.AfterClass
import org.scalatest.{Suite, TestSuite}
import org.scalatestplus.play.OneServerPerSuite
import play.api.db.Database

/**
  * Test trait that creates the functional test version of our
  * app for the duration of the suite, including some database
  * fixtures.
  */
trait FunctionalAppPerSuite
  extends OneServerPerSuite
  with SqlScriptRunner { self: TestSuite =>

  def origin = s"localhost:$port"

  override lazy val app = TestApplications.functional()

  val db = app.injector.instanceOf[Database]

  db.withConnection { implicit c =>
    runSqlScript("functional.sql")
  }

  @AfterClass
  def shutdownDb: Unit = {
    db.shutdown()
  }
}
