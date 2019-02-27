import java.util.logging.Level

import helpers.FuncTestBase
import helpers.remote.RemoteFuncTestBase
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.{BrowserInfo, PortNumber}
import play.api.test.TestBrowser
import uk.ac.warwick.util.web.Uri

import scala.collection.JavaConverters._

/**
  * This class is currently a bit of a mishmash of things under test,
  * until the basics are actually working and we can split them up
  * better.
  */
class BasicFuncTest extends RemoteFuncTestBase {

  override def sharedTests(info: BrowserInfo): Unit = {

    val mobileMasthead = className("start-masthead")
    val tabBar = className("tab-bar")
    val tabBarItems = className("tab-bar-item")
    val accountPopover = cssSelector(".account-information.popover")

    "An anonymous user" should {

      s"be sent to sign in ${info.name}" taggedAs(DesktopTest, MobileTest) in withScreenshot {
        go to homepage
        eventually(currentUrl should include("/origin/hs"))
      }

    }

    "A real user" should {
      s"see a cool home page ${info.name}" taggedAs(MobileTest) in withScreenshot {
        signInAs(config.users.student1)

        go to homepage

        eventually {
          find(mobileMasthead) should be(defined)
          find(tabBar) should be(defined)
          findAll(tabBarItems) should have length 5
          findAll(cssSelector(".tab-bar-item.disabled")) should be(empty)

          val profilePhoto = find(cssSelector(".account-link .img-circle")).get
          profilePhoto.attribute("src").get should endWith("no-photo.png")
        }

        capture to browserScreenshot(info, "Signed in mobile homepage")
      }

      s"see some notifications ${info.name}" taggedAs(MobileTest) in withScreenshot {
        signInAs(config.users.student1)
        go to notifications
        soon {
          findAll(cssSelector(".activity-item")).length should be > 0
        }

        eventually {
          executeScript("window.scrollBy(0, 10000)")
          find("app-container").get.text should include ("There are no older notifications")
        }
      }
    }

  }

  // Just messing around with getting logs.
  private def printLogs(browser: TestBrowser): Unit = {
    val logs = browser.manage.logs
    for (logtype <- logs.getAvailableLogTypes.asScala) {
      println(s"-- Examining $logtype logs --")
      for (entry <- logs.get(logtype).getAll.asScala.filter(_.getLevel.intValue() >= Level.INFO.intValue())) {
        println(s"[${entry.getTimestamp}] - ${entry.getLevel} - ${entry.getMessage}")
      }
    }
  }

}
