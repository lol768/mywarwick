package integration

import java.io.PrintWriter

import controllers.HomeController
import helpers.{BaseSpec, OneStartAppPerSuite}
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Configuration
import play.api.Environment
import play.api.db.Database
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import services.{ActivityService, TileContentService}
import warwick.sso._
import anorm._
import anorm.SqlParser._

/**
  * Eventually we'll have a suite of integration tests, that actually
  * drive the app and test what happens. For now this is good just to
  * check that the application starts and isn't horribly misconfigured.
  */
class ApplicationTest extends BaseSpec with OneStartAppPerSuite {

  "The application" should {

    "start up successfully" in {

      app.injector.instanceOf[HomeController]
      app.injector.instanceOf[TileContentService]

      // FIXME This would fail because it uses AkkaPubSub, which doesn't
      // work with a local-only actor system. Should be override PubSub for tests?
      //app.injector.instanceOf[ActivityService]

      // Output the test schema to a file, just so we can see what the tables look like
      // after all migrations have been applied.
      val db = app.injector.instanceOf[Database]
      exportH2Schema(db, "target/ddl.sql")

    }

  }

  def exportH2Schema(db: Database, path: String): Unit = {
    db.withConnection { implicit conn =>
      val writer = new PrintWriter(path)
      try {
        SQL("SCRIPT NODATA").as(str(1).*).foreach(writer.println)
      } finally {
        writer.close()
      }
    }
  }

}
