package system

import scala.concurrent.ExecutionContext

/**
 * Threadpools for various purposes, to prevent certain
 * types of activity from exhausting threads for other things.
 *
 * Use by importing an individual item into a scope (method or class).
 */
object ThreadPools {
  private val shared = ExecutionContext.global

  implicit val tileData = shared

  implicit val email = shared
  implicit val mobile = shared

  implicit val externalData = shared

  // For Controllers to do basic processing on received futures.
  implicit val web = shared
}
