package actors

import akka.testkit.TestActorRef
import akka.pattern.ask
import akka.util.Timeout
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import services.PrinterService

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Success

/**
 * Just playing with actors and writing tests for them.
 *
 * Here we send a message to PrintActor and check that it calls
 * through to a fake PrinterService.
 */
@RunWith(classOf[JUnitRunner])
class PrintActorSpec extends Specification {

  class Context extends ActorContext {

    val printerService = new PrinterService {
      override def get(username: String) = Future.successful(PrinterService.Response(username, 9))
    }

    implicit val timeout = Timeout(1 second)
  }

  "PrintActor" should {

    "retrieve data from PrintService" in new Context {
      val actor = TestActorRef(PrinterActor.props(printerService))
      val future = actor ? "cusebr"
      val result = Await.result(future, 1 second).asInstanceOf[PrinterService.Response]
      result must_== (PrinterService.Response("cusebr", 9))
    }

  }
}

