package filters

import javax.inject.Inject

import akka.util.ByteString
import play.api.Configuration
import play.api.libs.streams.Accumulator
import play.api.mvc.{EssentialAction, EssentialFilter, RequestHeader, Result}
import play.filters.csrf.CSRFFilter

import scala.collection.JavaConverters._
import scala.util.matching.Regex

class RouteExemptCsrfFilter @Inject() (csrfFilter: CSRFFilter, configuration: Configuration) extends EssentialFilter {

  var whitelistCache: Option[List[Regex]] = None

  override def apply(next: EssentialAction) = new EssentialAction {
    override def apply(rh: RequestHeader): Accumulator[ByteString, Result] = {
      val routePath = rh.path

      lazy val regexStrings = configuration.getStringList("mywarwick.csrfWhitelist").map(_.asScala).getOrElse(Nil).map(_.r)
      val whitelistRegexps = whitelistCache.getOrElse(regexStrings)
      whitelistCache = Some(whitelistRegexps.toList)

      val matchingRegexps = whitelistRegexps.filter(r => {
        r.findFirstMatchIn(routePath).fold(false)(_ => true)
      })

      matchingRegexps.headOption.fold(csrfFilter(next))(_ => next).apply(rh)
    }
  }
}
