package helpers

import akka.stream.Materializer
import org.scalatest.TestSuite
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application

import scala.reflect.ClassTag

/**
  * Shortcut to creating a minimal Application that (hopefully) doesn't contain any of our
  * code, just the base Play components such as ControllerComponents and WSClient which
  * can be injected into a test.
  */
trait MinimalAppPerSuite extends TestSuite with GuiceOneAppPerSuite {

  implicit override lazy val app: Application = TestApplications.minimal()
  implicit lazy val mat: Materializer = app.materializer

  def get[T : ClassTag]: T = app.injector.instanceOf[T]

}
