package helpers

import helpers.embedded.EmbeddedServerConfig
import org.scalatestplus.play.WsScalaTestClient
import play.api.libs.ws.WSClient
import play.api.test.WsTestClient
import play.libs.ws.WSClient

/**
  * Starts a server but doesn't drive any browsers - use the
  * ws* methods to make calls to controllers and check the response.
  *
  * This currently doesn't work because it needs to start an embedded Play app,
  * which uses jclouds which needs an old version of Guava, but Selenium
  * needs a newer version of Guava.
  *
  * We don't need Selenium for the API tests but we would for our other embedded app tests.
  */
abstract class ApiFuncTestBase
  extends CommonFuncTestBase
    with EmbeddedServerConfig
    with WsScalaTestClient {


}
