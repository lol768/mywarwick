package helpers

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Suite mix-in that provides a vanilla ActorSystem, plus an implicit
  * Materializer. Most useful in an otherwise regular unit test that
  * happens to use a bit of Play which demands a Materializer. You can
  * also create a whole Application and get the materializer from that,
  * but this is less work.
  *
  * Handles shutting down the ActorSystem at the end of the suite.
  */
trait WithActorSystem extends TestSuiteMixin { this: TestSuite =>
  implicit val akka: ActorSystem = TestActors.plainActorSystem()
  implicit val mat: Materializer = ActorMaterializer()

  abstract override def run(testName: Option[String], args: Args): Status =
    try super.run(testName, args)
    finally Await.result(akka.terminate(), 1.second)
}
