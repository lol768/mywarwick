package helpers

import java.sql.Connection

import akka.stream.Materializer
import org.scalatest.{Suite, TestSuite}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.db.Database

import scala.reflect.ClassTag

trait OneStartAppPerSuite extends TestSuite with GuiceOneAppPerSuite {

  implicit override lazy val app: Application = TestApplications.fullNoRoutes()
  implicit lazy val mat: Materializer = app.materializer

  def get[T : ClassTag]: T = app.injector.instanceOf[T]

  def transaction[T](block: Connection => T): T =
    transaction(rollback = true)(block)

  def transaction[T](rollback: Boolean)(block: Connection => T): T = {
    val database = app.injector.instanceOf[Database]
    val connection = database.getConnection(autocommit = false)

    try block(connection)
    finally {
      if (rollback)
        connection.rollback()
      else
        connection.commit()

      connection.close()
    }
  }

}
