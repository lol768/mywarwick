package helpers

import java.net.ServerSocket
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.eclipse.jetty.server
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.{Handler, Server}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future
import resource._

import scala.concurrent.ExecutionContext.Implicits.global

object ExternalServers {

  def runServer[A](handler: Handler)(block: Int => A): A = {
    val port = 19000 // pick a random port?
    val server = new Server(port)
    try {
      server.setStopAtShutdown(true)
      server.setHandler(handler)
      server.start()
      block(port)
    } finally {
      server.stop()
    }
  }

  /**
    * Run a TCP server that on connection prints some
    * text and closes the connection.
    */
  def runBrokenServer[A](block: Int => A): A = {
    val port = 19000
    managed(new ServerSocket(port)).acquireAndGet { acceptor =>
      @volatile var run = true
      Future {
        while (run) {
          for (socket <- managed(acceptor.accept())) {
            socket.getOutputStream.write("Error".getBytes("UTF-8"))
            socket.close()
          }
        }
      }
      try block(port)
      finally run = false
    }
  }

  case class Response(body: String, status: Int = 200, headers:Map[String,String] = Map()) {
    def json = copy(headers = headers.updated("Content-Type", "application/json"))
  }
  object Response {
    def json(value: JsValue) = Response(Json.stringify(value)).json
  }

  object JettyHandler {
    def apply(routes: PartialFunction[(String, String), Response]): Handler = new AbstractHandler {
      override def handle(s: String, req: server.Request, request: HttpServletRequest, res: HttpServletResponse): Unit = {
        routes.lift.apply((req.getMethod.toUpperCase, req.getRequestURI)) match {
          case Some(result) =>
            res.setStatus(result.status)
            for ((key, value) <- result.headers) {
              res.setHeader(key, value)
            }
            res.getWriter.write(result.body)
            res.getWriter.close()
            req.setHandled(true)
          case None =>
            res.sendError(404)
            req.setHandled(true)
        }
      }
    }
  }


}
