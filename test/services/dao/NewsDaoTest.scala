package services.dao

import java.sql.Connection

import helpers.OneStartAppPerSuite
import models.news.AudienceSize.{Finite, Public}
import models.news.NewsItemSave
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import services.NewsService
import warwick.sso.Usercode

class NewsDaoTest extends PlaySpec with OneStartAppPerSuite {
  val newsDao = get[NewsDao]
  val newsService = get[NewsService]

  val ana = Usercode("cusana")
  val bob = Usercode("cusbob")
  val eli = Usercode("cuseli")
  val jim = Usercode("cusjim")
  val public = Usercode("*")

  def save(item: NewsItemSave, recipients: Seq[Usercode])(implicit c: Connection): String = {
    val id = newsDao.save(item, "audienceId")
    newsDao.saveRecipients(id, item.publishDate, recipients)
    id
  }

  val londonsBurning = NewsItemSave(
    title = "London's Burning",
    text = "The capital is on fire.",
    link = None,
    publishDate = DateTime.now.minusMinutes(5),
    imageId = None
  )

  val brumPanic = NewsItemSave(
    title = "Panic on the streets of Birmingham",
    text = "I wonder to myself - burn down the disco.",
    link = None,
    publishDate = DateTime.now.minusMinutes(10),
    imageId = None
  )

  // Publish date in future, so shouldn't be shown to user.
  val futureNews = NewsItemSave(
    title = "Hoverboards invented",
    text = "Finally, we can all hover to work!",
    link = None,
    publishDate = DateTime.now.plusYears(10),
    imageId = None
  )

  "latestNews" should {
    "return no news" in transaction { implicit c =>
      newsDao.latestNews(Some(jim)) must be ('empty)
    }

    "return only published news for user" in transaction { implicit c =>
      save(londonsBurning, Seq(public))
      save(brumPanic, Seq(ana, eli))
      save(futureNews, Seq(ana, bob, eli, jim))

      val publicNews = newsDao.latestNews(None)
      publicNews.map(_.title) must be(Seq(londonsBurning.title))

      val jimNews = newsDao.latestNews(Some(jim))
      jimNews.map(_.title) must be(Seq(londonsBurning.title))

      val anaNews = newsDao.latestNews(Some(ana))
      anaNews.map(_.title) must be(Seq(londonsBurning.title, brumPanic.title))
    }

    "limit results to requested amount" in transaction { implicit c =>
      save(londonsBurning, Seq(ana))
      save(brumPanic, Seq(ana))
      newsDao.latestNews(Some(ana), limit = 2) must have length(2)
      newsDao.latestNews(Some(ana), limit = 1) must have length(1)
    }
  }

  "countRecipients" should {
    "return an empty result for an empty input" in transaction { implicit c =>
      newsDao.countRecipients(Nil) mustBe Map()
    }

    "return results for public and non-public news" in transaction { implicit c =>
      val id1 = save(londonsBurning, Seq(public))
      val id2 = save(brumPanic, Seq(ana, eli))
      val id3 = save(futureNews, Seq(ana, bob, eli, jim))
      val id4 = save(futureNews, Seq(public)) // should be ignored

      val counts = newsDao.countRecipients(Seq(id1,id2,id3))
      counts mustBe Map(
        id1 -> Public,
        id2 -> Finite(2),
        id3 -> Finite(4)
      )
    }
  }

}