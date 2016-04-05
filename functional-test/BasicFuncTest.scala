import java.util.logging.Level

import helpers.FuncTestBase
import org.scalatestplus.play.BrowserInfo
import play.api.test.TestBrowser

import scala.collection.JavaConversions._

/**
  * This class is currently a bit of a mishmash of things under test,
  * until the basics are actually working and we can split them up
  * better.
  */
class BasicFuncTest extends FuncTestBase {

  override def foreachBrowser(browser: TestBrowser, info: BrowserInfo): Unit = {

    "An anonymous user" - {

      "should see a cool home page" in {
        browser.goTo("/")
        browser.findFirst("#app-container") shouldNot be(null)
        browser.title should be("Start.Warwick")

        browser.findFirst(".sign-in-link").getText should be ("Sign in")

        browser.waitUntil {
          !browser.find(".id7-main-content .tile").isEmpty
        }

        val tiles = browser.find(".id7-main-content .tile")
        tiles.length should be (1)
        tiles.get(0).findFirst(".tile__title").getText should be ("Mail")
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
