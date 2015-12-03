package system

/** Threadpools for different purposes. */
object Threadpools {
  import scala.concurrent.ExecutionContext.global

  implicit val tileData = global
}
