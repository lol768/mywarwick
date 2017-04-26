package helpers.remote

import com.typesafe.config.{Config, ConfigFactory}
import helpers.{CommonFuncTestBase, UserData}
import org.scalatestplus.play.PortNumber

/**
  *
  */
trait RemoteServerConfig { self: CommonFuncTestBase =>

  override def baseUrl = config.url

  implicit val portNumber: PortNumber = PortNumber(443)
}
