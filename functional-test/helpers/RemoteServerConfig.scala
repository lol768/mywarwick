package helpers

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatestplus.play.PortNumber

/**
  *
  */
trait RemoteServerConfig { self: CommonFuncTestBase =>
  val rawConfig: Config = ConfigFactory.load("functional-test")

  object config {
    def url: String = rawConfig.getString("url")
    object users {
      lazy val student1 = UserData(rawConfig.getConfig("users.student1"))
    }
  }

  override def baseUrl = config.url

  implicit val portNumber: PortNumber = PortNumber(443)
}
