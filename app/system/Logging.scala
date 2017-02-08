package system

import play.api.Logger
import play.api.mvc.Request
import warwick.sso.{AuthenticatedRequest, User}

/**
  * Provides a Logger object.
  */
trait Logging {
  self =>
  lazy val logger = Logger(self.getClass)

  lazy val auditLogger = Logger("uk.ac.warwick.AUDIT")

  /**
    * Log audit data to the audit logger.
    *
    * @param context this implicit parameter is usually provided implicitly by a conversion from
    *                a Request object, which is provided by this trait, usually from BaseController.
    *                If you are mixing in Logging you probably already have the conversion in scope.
    */
  def auditLog(action: Symbol, data: (Symbol, Any)*)(implicit context: AuditLogContext): Unit = {
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

  /**
    * Converts a Request to AuditLogContext, to provide the properties it needs.
    * It's important that this conversion is cheap and fast.
    */
  implicit def implicitAuditLogContext(implicit request: Request[_]): AuditLogContext = request match {
    case req: AuthenticatedRequest[_] => AuditLogContext(req.context.user, req.context.actualUser)
    case _ => AuditLogContext(None, None)
  }

}
