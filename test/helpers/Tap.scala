package helpers

object Tap {
  implicit class Tappable[T](val any: T) extends AnyVal {
    def tap(fn: T => Unit): T = {
      fn(any)
      any
    }
  }
}
