import helpers.{FuncTestBase, RemoteFuncTestBase}
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.BrowserInfo

class SearchFuncTest extends RemoteFuncTestBase with BeforeAndAfter {

  override def sharedTests(info: BrowserInfo): Unit = {
    val searchResults = className("search-result")

    "Someone lost" should {

      s"be able to search ${info.name}" in withScreenshot {
        resizeWindow(iphone5Size)

        go to search

        eventually {
          val activeTabBarItem = find(className("tab-bar-item--active")).get
          activeTabBarItem.text shouldBe "Search"

          find(id("q-input")) shouldBe defined
          searchField("q-input").attribute("placeholder") should contain("Search Warwick")

          val pageText = find(cssSelector("main")).get.text
          pageText should include("Quick links")
          pageText should include("We thought this might be useful to you")
        }

        searchField("q-input").value = "elab"
        submit()

        eventually {
          val firstSearchResult = find(searchResults).get
          firstSearchResult.text should include("E-lab home page")

          val pageText = find(cssSelector("main")).get.text
          pageText should include("Website search results")
        }

        val searchResultCount = findAll(searchResults).length

        // Scroll to bottom of page so more results link is not occluded by the tab bar
        // when Selenium tries to click it
        executeScript("window.scrollTo(0, 10000)")

        click on linkText("More website search results")

        eventually(findAll(searchResults).length should be > searchResultCount)
      }

    }
  }
}
