package services.dao

import java.sql.Connection

import helpers.OneStartAppPerSuite
import anorm._
import anorm.SqlParser._
import models.AudienceSize.{Finite, Public}
import models.news.NewsItemSave
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import services.NewsService
import warwick.sso.Usercode

class NewsDaoTest extends PlaySpec with OneStartAppPerSuite {
  val newsDao = get[NewsDao]
  val newsService = get[NewsService]
  val userNewsCategoryDao = get[UserNewsCategoryDao]
  val newsCategoryDao = get[NewsCategoryDao]

  val ana = Usercode("cusana")
  val bob = Usercode("cusbob")
  val eli = Usercode("cuseli")
  val jim = Usercode("cusjim")
  val public = Usercode("*")

  def save(item: NewsItemSave, recipients: Seq[Usercode])(implicit c: Connection): String = {
    val id = newsDao.save(item, "audienceId")
    newsDao.setRecipients(id, recipients)
    id
  }

  val londonsBurning = NewsItemSave(
    usercode = Usercode("custard"),
    publisherId = "publisher",
    title = "London's Burning",
    text = "The capital is on fire.",
    link = None,
    publishDate = DateTime.now.minusMinutes(5),
    imageId = None,
    ignoreCategories = true
  )

  val brumPanic = NewsItemSave(
    usercode = Usercode("custard"),
    publisherId = "publisher",
    title = "Panic on the streets of Birmingham",
    text = "I wonder to myself - burn down the disco.",
    link = None,
    publishDate = DateTime.now.minusMinutes(10),
    imageId = None,
    ignoreCategories = true
  )

  // Publish date in future, so shouldn't be shown to user.
  val futureNews = NewsItemSave(
    usercode = Usercode("custard"),
    publisherId = "publisher",
    title = "Hoverboards invented",
    text = "Finally, we can all hover to work!",
    link = None,
    publishDate = DateTime.now.plusYears(10),
    imageId = None,
    ignoreCategories = true
  )

  "latestNews" should {
    "return no news" in transaction { implicit c =>
      newsDao.latestNews(Some(jim)) mustBe empty
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
      newsDao.latestNews(Some(ana), limit = 2) must have length 2
      newsDao.latestNews(Some(ana), limit = 1) must have length 1
    }

    "return only news for subscribed categories" in transaction { implicit c =>
      val someCategory = newsCategoryDao.all().head

      val id = save(londonsBurning.copy(ignoreCategories = false), Seq(public))
      newsCategoryDao.saveNewsCategories(id, Seq(someCategory.id))

      userNewsCategoryDao.setSubscribedCategories(jim, Seq(someCategory.id))

      // Jim sees the item because he is subscribed to the category
      newsDao.latestNews(Some(jim)) must have length 1

      userNewsCategoryDao.setSubscribedCategories(jim, Seq.empty)

      // No longer sees the item when not subscribed
      newsDao.latestNews(Some(jim)) mustBe empty
    }

    "return news with IGNORE_CATEGORIES set regardless of chosen categories" in transaction { implicit c =>
      val someCategory :: anotherCategory :: _ = newsCategoryDao.all()

      val id = save(londonsBurning.copy(ignoreCategories = true), Seq(public))
      newsCategoryDao.saveNewsCategories(id, Seq(someCategory.id))

      userNewsCategoryDao.setSubscribedCategories(jim, Seq(anotherCategory.id))

      // Jim is not subscribed to the category, but sees the item anyway
      newsDao.latestNews(Some(jim)) must have length 1

      userNewsCategoryDao.setSubscribedCategories(jim, Seq(someCategory.id))

      // The same item is not returned twice
      newsDao.latestNews(Some(jim)) must have length 1
    }
  }

  "countRecipients" should {
    "return an empty result for an empty input" in transaction { implicit c =>
      newsDao.countRecipients(Nil) mustBe Map()
    }

    "return value inclusive of user news-category preferences" in transaction { implicit c =>
      val CATS_ON_FIRE = "cats-on-fire"
      val londonsBurningWithCat = londonsBurning.copy(ignoreCategories = false)
      val id = save(londonsBurningWithCat, Seq(ana, eli, jim))

      SQL"INSERT INTO news_category VALUES ($CATS_ON_FIRE, $CATS_ON_FIRE)".execute()
      userNewsCategoryDao.setSubscribedCategories(ana, Seq(CATS_ON_FIRE))
      newsCategoryDao.saveNewsCategories(id, Seq(CATS_ON_FIRE))

      val count = newsDao.countRecipients(Seq(id))
      count mustBe Map(
        id -> Finite(1)
      )
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

  "getNewsByIds" should {

    "return nothing" in transaction { implicit c =>
      newsDao.getNewsByIds(Seq.empty) mustBe Seq.empty
    }

    "return a single news item" in transaction { implicit c =>
      val id = save(londonsBurning, Seq(public))

      newsDao.getNewsByIds(Seq(id)) must have length 1
    }

    "return multiple news items" in transaction { implicit c =>
      val id1 = save(londonsBurning, Seq(public))
      val id2 = save(brumPanic, Seq(ana, eli))

      newsDao.getNewsByIds(Seq(id1, id2)) must have length 2
    }

  }

  "getNewsById" should {

    "return None for a non-existent news item" in transaction { implicit c =>
      newsDao.getNewsById("nonsense") mustBe empty
    }

    "return a news item that exists" in transaction { implicit c =>
      val id = save(londonsBurning, Seq(public))

      newsDao.getNewsById(id) mustNot be(empty)
    }

  }

}