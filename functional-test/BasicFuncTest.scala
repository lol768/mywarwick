import java.util.logging.Level

import helpers.{FuncTestBase, RemoteFuncTestBase}
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.{BrowserInfo, PortNumber}
import play.api.test.TestBrowser

import scala.collection.JavaConversions._

/**
  * This class is currently a bit of a mishmash of things under test,
  * until the basics are actually working and we can split them up
  * better.
  */
class BasicFuncTest extends RemoteFuncTestBase with BeforeAndAfter {

  after {
    webDriver.close()
  }

  override def sharedTests(info: BrowserInfo): Unit = {

    val mobileMasthead = className("start-masthead")
    val tabBar = className("tab-bar")
    val tabBarItems = className("tab-bar-item")
    val accountPopover = cssSelector(".account-information.popover")

    "An anonymous user" should {

      s"be sent to sign in ${info.name}" in withScreenshot {
        go to homepage
        eventually(currentUrl should include("/origin/hs"))
        close
      }

    }

    "A real user" ignore {
      s"see a cool home page ${info.name}" in withScreenshot {
        resizeWindow(standardSize)

        add cookie("insecure-fake-user", "cusebr")

        go to homepage
        eventually(find(className("account-link")).get.text should be("Nick Howes"))
        capture to browserScreenshot(info, "Signed in homepage")

        click on cssSelector(".masthead-popover-icons .fa-inbox")
        eventually(find(className("popover-title")).get.text should be("Notifications"))

        click on cssSelector(".popover > :last-child")
        eventually(currentUrl should endWith("/notifications"))

        click on cssSelector(".masthead-popover-icons .fa.fa-dashboard")
        eventually(find(className("popover-title")).get.text should be("Activity"))

        click on cssSelector(".popover > :last-child")
        eventually(currentUrl should endWith("/activity"))

        click on className("account-link")
        eventually(find(accountPopover) should not be empty)

        click on className("account-link")
        eventually(click on cssSelector(".id7-search-column input[type=search]"))
        eventually(find(cssSelector(".id7-search-column .popover-content")).get.text should include("Quick links"))
      }

      s"see a cool mobile home page ${info.name}" in withScreenshot {
        resizeWindow(iphone5Size)

        add cookie("insecure-fake-user", "cusebr")

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
    }

  }

  // Just messing around with getting logs.
  private def printLogs(browser: TestBrowser): Unit = {
    val logs = browser.manage.logs
    for (logtype <- logs.getAvailableLogTypes) {
      println(s"-- Examining ${logtype} logs --")
      for (entry <- logs.get(logtype).filter(Level.INFO)) {
        println(s"[${entry.getTimestamp}] - ${entry.getLevel} - ${entry.getMessage}")
      }
    }
  }

}
