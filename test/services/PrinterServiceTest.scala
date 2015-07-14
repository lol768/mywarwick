package services

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.concurrent.Await
import scala.concurrent.duration._


class PrinterServiceTest extends Specification {

  class Context extends Scope

  /**
   * This test only really works because the concrete class has
   * a fake implementation. Once it actually starts calling out
   * to an external service it may need removing.
   */
  "PrinterServiceTest" should {
    "get print balance" in new Context {
      val service = new PrinterServiceImpl
      val result = Await.result(service.get("cusebr"), 1 second)
      result.username must be ("cusebr")
      result.balance must beBetween(0, 500)
    }

  }
}

