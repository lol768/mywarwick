package helpers

import java.sql.Connection

import org.scalatest.Suite
import org.scalatestplus.play.OneAppPerSuite
import play.api.db.Database

import scala.reflect.ClassTag

trait OneStartAppPerSuite extends Suite with OneAppPerSuite {

  implicit override lazy val app = TestApplications.full()
  implicit lazy val mat = app.materializer

  def get[T : ClassTag]: T = app.injector.instanceOf[T]

  def transaction(block: Connection => Unit): Unit =
    transaction(rollback = true)(block)

  def transaction(rollback: Boolean)(block: Connection => Unit): Unit = {
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
