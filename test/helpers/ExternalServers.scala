package helpers

import play.api.routing.Router
import play.api.test.TestServer

object ExternalServers {

  def runServer[A](routes: Router.Routes)(block: (Int) => A): A = {
    val port = 19000 // pick a random port?
    val app = TestApplications.miniserver(Router.from(routes))
    val server = TestServer(port = port, application = app)
    server.start()
    try block(port)
    finally server.stop()
  }

}
