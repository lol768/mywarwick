package services.dao

import java.sql.Connection

import helpers.OneStartAppPerSuite
import models.news.{Audience, NewsItemSave}
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import play.api.db.Database
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

  def save(item: NewsItemSave, recipients: Seq[Usercode])(implicit c: Connection): Unit = {
    val id = newsDao.save(item)
    newsDao.saveRecipients(id, item.publishDate, recipients)
  }

  val londonsBurning = NewsItemSave(
    title = "London's Burning",
    text = "The capital is on fire.",
    link = None,
    publishDate = DateTime.now.minusMinutes(5)
  )

  val brumPanic = NewsItemSave(
    title = "Panic on the streets of Birmingham",
    text = "I wonder to myself - burn down the disco.",
    link = None,
    publishDate = DateTime.now.minusMinutes(10)
  )

  // Publish date in future, so shouldn't be shown to user.
  val futureNews = NewsItemSave(
    title = "Hoverboards invented",
    text = "Finally, we can all hover to work!",
    link = None,
    publishDate = DateTime.now.plusYears(10)
  )

  "latestNews" should {
    "return no news" in transaction { implicit c =>
      newsDao.latestNews(jim) must be ('empty)
    }

    "return only published news for user" in transaction { implicit c =>
      save(londonsBurning, Seq(public))
      save(brumPanic, Seq(ana, eli))
      save(futureNews, Seq(ana, bob, eli, jim))

      val jimNews = newsDao.latestNews(jim)
      jimNews.map(_.title) must be(Seq(londonsBurning.title))

      val anaNews = newsDao.latestNews(ana)
      anaNews.map(_.title) must be(Seq(londonsBurning.title, brumPanic.title))
    }

    "limit results to requested amount" in transaction { implicit c =>
      save(londonsBurning, Seq(ana))
      save(brumPanic, Seq(ana))
      newsDao.latestNews(ana, limit = 2) must have length(2)
      newsDao.latestNews(ana, limit = 1) must have length(1)
    }
  }

}