package system

import play.api.Logger
import play.api.mvc.Request
import uk.ac.warwick.util.logging.AuditLogger
import uk.ac.warwick.util.logging.AuditLogger.{Field, RequestInformation}
import warwick.sso.AuthenticatedRequest
import scala.collection.JavaConverters._

/**
  * Provides a Logger object.
  */
trait Logging {
  self =>
  lazy val logger = Logger(self.getClass)

  lazy val auditLogger: AuditLogger = AuditLogger.getAuditLogger("mywarwick")

  /**
    * Log audit data to the audit logger.
    *
    * @param context this implicit parameter is usually provided implicitly by a conversion from
    *                a Request object, which is provided by this trait, usually from BaseController.
    *                If you are mixing in Logging you probably already have the conversion in scope.
    */
  def auditLog(action: Symbol, data: (Symbol, Any)*)(implicit context: AuditLogContext): Unit = {
    val info = RequestInformation.forEventType(action.name)
    context.actualUser.map(u => info.withUsername(u.usercode.string))

    // We need to convert all Scala collections into Java collections
    def handle(in: Any): AnyRef = (in match {
      case Left(_) => throw new IllegalArgumentException("Failed actions shouldn't be audited")
      case Some(x: Object) => handle(x)
      case Some(null) => null
      case None => null
      case jcol: java.util.Collection[_] => jcol.asScala.map(handle).asJavaCollection
      case jmap: java.util.Map[_, _] => jmap.asScala.mapValues(handle).asJava
      case smap: scala.collection.SortedMap[Any @unchecked, Any @unchecked] =>
        val map = new java.util.LinkedHashMap[Any, Any]
        smap.mapValues(handle).toSeq.foreach { case (k, v) => map.put(k, v) }
        smap
      case smap: scala.collection.Map[_, _] => mapAsJavaMapConverter(smap.mapValues(handle)).asJava
      case sseq: scala.Seq[_] => seqAsJavaListConverter(sseq.map(handle)).asJava
      case scol: scala.Iterable[_] => asJavaCollectionConverter(scol.map(handle)).asJavaCollection
      case other: AnyRef => other
      case _ => null
    }) match {
      case null => "-"
      case notNull => notNull
    }

    val eventData = data.map { case (k, v) => new Field(k.name) -> handle(v) }.toMap

    auditLogger.log(info, eventData.asJava)
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
