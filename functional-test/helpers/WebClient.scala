package helpers

import akka.stream.Materializer
import org.scalatest.{Args, Status, TestSuite, TestSuiteMixin}
import play.api.libs.ws.{WSAPI, WSClient, WSRequest}
import play.api.libs.ws.ahc.{AhcConfigBuilder, AhcWSClient}


class WebClient(mat: Materializer) extends WSAPI {
  private val config = new AhcConfigBuilder().build()

  override val client: WSClient = new AhcWSClient(config)(mat)
  override def url(url: String): WSRequest = client.url(url)
}

/**
  * Provides a WebClient for the lifetime of the test suite, then
  * cleans up.
  *
  * Requires a Materializer which you will probably get either from
  * a trait providing a Play Application, or otherwise through
  * the WithActorSystem trait.
  */
trait WithWebClient extends TestSuiteMixin { this: TestSuite =>

  def mat: Materializer

  private var webClientOpen = false

  lazy val web = {
    webClientOpen = true
    new WebClient(mat)
  }

  abstract override def run(testName: Option[String], args: Args): Status =
    try super.run(testName, args)
    finally if (webClientOpen) web.client.close()

}