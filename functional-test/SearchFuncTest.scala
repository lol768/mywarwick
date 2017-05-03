import helpers.remote.RemoteFuncTestBase
import org.openqa.selenium.interactions.Actions
import org.scalatestplus.play.BrowserInfo

class SearchFuncTest extends RemoteFuncTestBase {

  override def sharedTests(info: BrowserInfo): Unit = {

    "Someone lost" should {

      s"be able to search ${info.name}" taggedAs(MobileTest) in withScreenshot {
        val searchResults = className("search-result")

        go to homepage

        signInAs(config.users.student1)

        go to search

        eventually {
          val activeTabBarItem = find(className("tab-bar-item--active")).get
          activeTabBarItem.text shouldBe "Search"

          find(cssSelector(".suggested-links")) shouldBe defined

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
          pageText should include("Website Search Results")
        }

        val searchResultCount = findAll(searchResults).length

        // Checking that we get more search results

        val moreWebResultsLink = linkText("More website search results")

        // get more results.
        // have to be able to see button first, so scroll to it
        new Actions(webDriver)
          .moveToElement(moreWebResultsLink.webElement)
          .perform()

        // plus some more pixels so it's not under a tab bar.
        scrollBy(0, 200)

        click on moreWebResultsLink

        findAll(searchResults).length should be > searchResultCount

        scrollTo(0,0)
        click on cssSelector(".search-form .clearButton")
        soon(find(cssSelector(".suggested-links")) shouldBe defined)
      }

    }
  }
}
