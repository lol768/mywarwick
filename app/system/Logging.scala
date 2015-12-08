package system

import play.api.Logger

/**
  * Provides a Logger object.
  */
trait Logging { self =>
  lazy val logger = Logger(self.getClass)
}
