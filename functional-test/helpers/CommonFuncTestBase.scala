package helpers

import com.typesafe.config.{Config, ConfigFactory}
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
    with OneStartAppPerSuite
    with WithWebClient {
  def baseUrl: String

  override lazy val app = TestApplications.minimal()

  val rawConfig: Config = ConfigFactory.load("functional-test")

  object config {
    def url: String = rawConfig.getString("url")
    object users {
      lazy val student1 = UserData(rawConfig.getConfig("users.student1"))
    }
  }
}
