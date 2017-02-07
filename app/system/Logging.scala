package system

import play.api.Logger

/**
  * Provides a Logger object.
  */
trait Logging {
  self =>
  lazy val logger = Logger(self.getClass)

  lazy val auditLogger = Logger("uk.ac.warwick.AUDIT")

  def auditLog(action: Symbol, data: (Symbol, Any)*)(implicit context: UserProperties): Unit = {
    val requestContextData = Seq(
      'user -> context.user.map(_.usercode.string),
      'actualUser -> context.actualUser.map(_.usercode.string)
    )

    val keyValuePairs = (data ++ requestContextData).map {
      case (k, Some(v)) => s"${k.name}=$v"
      case (k, v) => s"${k.name}=$v"
    }.mkString(" ")

    auditLogger.info(s"${action.name} $keyValuePairs")
  }

}
