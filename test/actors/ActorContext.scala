package actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.specs2.matcher.Scope

/**
 * A test Akka context that you can subclass and instantiate in a test,
 * mixing in any cake components as required to test individual actors
 * or sets of actors.
 */
abstract class ActorContext extends TestKit(ActorSystem("test")) with Scope with ImplicitSender
