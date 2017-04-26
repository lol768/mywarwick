package helpers

import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures, ScaledTimeSpans}
import org.scalatest.{Matchers, OptionValues, WordSpec}

/**
  *
  */
protected abstract class CommonFuncTestBase
  extends WordSpec
    with Eventually
    with Matchers
    with OptionValues
    with ScalaFutures
    with ScaledTimeSpans
    with IntegrationPatience
    with WithWebClient {
  def baseUrl: String
}
