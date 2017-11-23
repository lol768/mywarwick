package filters

import javax.inject.Inject

import akka.util.ByteString
import play.api.Configuration
import play.api.libs.streams.Accumulator
import play.api.mvc.{EssentialAction, EssentialFilter, RequestHeader, Result}
import play.filters.csrf.CSRFFilter

import scala.collection.JavaConverters._
import scala.util.matching.Regex

class RouteExemptCsrfFilter @Inject() (csrfFilter: CSRFFilter, configuration: Configuration, env: play.Environment) extends EssentialFilter {

  lazy val whitelistRegexps: List[Regex] = configuration.get[Option[Seq[String]]]("mywarwick.csrfWhitelist").getOrElse(Nil).map(_.r).toList

  override def apply(proposedNext: EssentialAction) = new EssentialAction {
    override def apply(rh: RequestHeader): Accumulator[ByteString, Result] = {
      if (env.isTest) {
        return proposedNext(rh)
      }

      val routePath = rh.path

      val matchesWhitelist = whitelistRegexps.exists(r => {
        r.findFirstMatchIn(routePath).nonEmpty
      })

      val next = if (matchesWhitelist) { proposedNext } else { csrfFilter(proposedNext) }
      next(rh)
    }
  }
}
