package system

/**
 * Threadpools for various purposes, to prevent certain
 * types of activity from exhausting threads for other things.
 *
 * Use by importing an individual item into a scope (method or class).
 */
object Threadpools {
  import scala.concurrent.ExecutionContext.global

  implicit val tileData = global
}
