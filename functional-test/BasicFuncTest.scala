import java.util.logging.Level

import helpers.FuncTestBase

import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.BrowserInfo
import play.api.test.TestBrowser

import scala.collection.JavaConversions._

/**
  * This class is currently a bit of a mishmash of things under test,
  * until the basics are actually working and we can split them up
  * better.
  */
class BasicFuncTest extends FuncTestBase with BeforeAndAfter {

  override def sharedTests(info: BrowserInfo): Unit = {
    "An anonymous user" should {

      s"see a cool home page ${info.name}" in withScreenshot {
        resizeWindow(standardSize)

        go to homepage

        find("app-container") should be('defined)
        pageTitle should be ("Start.Warwick")

        eventually {
          find(className("sign-in-link")).get.text should be ("Sign in")
          findAll(cssSelector(".id7-main-content .tile")) shouldNot be (empty)
        }

        capture to browserScreenshot(info, "Anonymous homepage")

        val tiles = findAll(cssSelector(".id7-main-content .tile")).toList
        tiles.length should be (1)
        tiles.head.text should be ("Mail")
      }

    }

    "A real user" should {
      s"see a cool home page ${info.name}" in withScreenshot {
        resizeWindow(standardSize)

        add cookie ("insecure-fake-user", "cusebr")

        go to homepage

        eventually {
          find(className("account-link")).get.text should be ("Nick Howes")
        }

        capture to browserScreenshot(info, "Signed in homepage")
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
