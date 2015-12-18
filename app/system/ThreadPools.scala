package system

/**
 * Threadpools for various purposes, to prevent certain
 * types of activity from exhausting threads for other things.
 *
 * Use by importing an individual item into a scope (method or class).
 */
object ThreadPools {
  import scala.concurrent.ExecutionContext.global

  // TODO think about not using the default pool for all these.

  implicit val tileData = global

  implicit val email = global
  implicit val sms = global
  implicit val mobile = global
}
