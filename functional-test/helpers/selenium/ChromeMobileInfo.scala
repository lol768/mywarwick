package helpers.selenium

import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.scalatestplus.play.BrowserInfo

import scala.collection.JavaConverters._

case object ChromeMobileInfo extends BrowserInfo("[Chrome Mobile]", "org.scalatest.tags.ChromeBrowser") {

  override def createWebDriver(): ChromeDriver = {
    val options = new ChromeOptions
    options.setCapability(ChromeOptions.CAPABILITY, Map(
      "mobileEmulation" -> Map("deviceName" -> "Google Nexus 5").asJava
    ).asJava)

    new ChromeDriver(options)
  }

}
