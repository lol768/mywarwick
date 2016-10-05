package helpers

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Await
import scala.concurrent.duration.Duration


object TestActors {
  /**
    * Some tests just need an ActorSystem to test things that need Materializer.
    * But ActorSystem() on its own will try to load from application.conf, including
    * your cluster settings and port. Side effects include test failures if the app
    * is running locally at the same time (as it's using the port).
    *
    * This method returns a totally configless Akka, with plain JVM-only actors.
    *
    * REMEMBER to terminate the system after the tests have run!
    * Also see WithActorSystem which handles this for you.
    */
  def plainActorSystem(config: Config = ConfigFactory.empty()) = ActorSystem("test", config)

  /**
    * Use an ActorSystem within a test, cleaning it up for you when it's done.
    * @param awaitTermination if you are using a memory-only ActorSystem, you shouldn't need to
    *                         wait for termination as systems should be isolated from each other
    *                         anyway. But if some setups do seem to be interacting, you can specify
    *                         to wait until it's fully shut down before finishing your test.
    */
  def using(system: ActorSystem, awaitTermination: Boolean = false)(fn: (ActorSystem) => Unit): Unit =
    try fn(system)
    finally {
      system.terminate()
      if (awaitTermination) Await.result(system.whenTerminated, Duration.Inf)
    }

}
