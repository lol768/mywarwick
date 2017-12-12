package helpers

import akka.stream.Materializer
import org.scalatest.{Args, Status, TestSuite, TestSuiteMixin}
import play.api.libs.ws.ahc.{AhcConfigBuilder, AhcWSClient}
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.WsTestClient

/**
  * Provides a WebClient for the lifetime of the test suite, then
  * cleans up.
  *
  * Requires a Materializer which you will probably get either from
  * a trait providing a Play Application, or otherwise through
  * the WithActorSystem trait.
  */
trait WithWebClient extends TestSuiteMixin { this: TestSuite =>

  implicit def mat: Materializer

  private var webClientOpen = false

  implicit lazy val client = {
    webClientOpen = true
    AhcWSClient()
  }

  abstract override def run(testName: Option[String], args: Args): Status =
    try super.run(testName, args)
    finally if (webClientOpen) client.close()

}