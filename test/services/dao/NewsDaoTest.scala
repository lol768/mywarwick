package services.dao

import java.sql.Connection

import helpers.{BaseSpec, OneStartAppPerSuite}
import models.Audience.{DepartmentAudience, Staff, TeachingStaff, UndergradStudents}
import models.news.NewsItemSave
import models.{Audience, AudienceSize}
import org.joda.time.DateTime
import services.NewsService
import warwick.sso.{GroupName, Usercode}

class NewsDaoTest extends BaseSpec with OneStartAppPerSuite {
  val newsDao: NewsDao = get[NewsDao]
  val newsService: NewsService = get[NewsService]
  val userNewsCategoryDao: UserNewsCategoryDao = get[UserNewsCategoryDao]
  val newsCategoryDao: NewsCategoryDao = get[NewsCategoryDao]
  val audienceDao: AudienceDao = get[AudienceDao]

  val ana = Usercode("cusana")
  val bob = Usercode("cusbob")
  val eli = Usercode("cuseli")
  val jim = Usercode("cusjim")
  val public = Usercode("*")

  def save(item: NewsItemSave, recipients: Set[Usercode])(implicit c: Connection): String = {
    val id = newsDao.save(item, "audienceId", AudienceSize.Finite(recipients.size))
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
      save(londonsBurning, Set(public))
      save(brumPanic, Set(ana, eli))
      save(futureNews, Set(ana, bob, eli, jim))

      val publicNews = newsDao.latestNews(None)
      publicNews.map(_.title) must be(Seq(londonsBurning.title))

      val jimNews = newsDao.latestNews(Some(jim))
      jimNews.map(_.title) must be(Seq(londonsBurning.title))

      val anaNews = newsDao.latestNews(Some(ana))
      anaNews.map(_.title) must be(Seq(londonsBurning.title, brumPanic.title))
    }

    "limit results to requested amount" in transaction { implicit c =>
      save(londonsBurning, Set(ana))
      save(brumPanic, Set(ana))
      newsDao.latestNews(Some(ana), limit = 2) must have length 2
      newsDao.latestNews(Some(ana), limit = 1) must have length 1
    }

    "offset results by requested number" in transaction { implicit c =>
      save(londonsBurning, Set(ana))
      save(brumPanic, Set(ana))
      newsDao.latestNews(Some(ana), offset = 1) must have length 1
      newsDao.latestNews(Some(ana), limit = 1, offset = 1).head must have('title (brumPanic.title))
      newsDao.latestNews(Some(ana), limit = 1).head must have('title (londonsBurning.title))
    }

    "return only news for subscribed categories" in transaction { implicit c =>
      val someCategory = newsCategoryDao.all().head

      val id = save(londonsBurning.copy(ignoreCategories = false), Set(public))
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

      val id = save(londonsBurning.copy(ignoreCategories = true), Set(public))
      newsCategoryDao.saveNewsCategories(id, Seq(someCategory.id))

      userNewsCategoryDao.setSubscribedCategories(jim, Seq(anotherCategory.id))

      // Jim is not subscribed to the category, but sees the item anyway
      newsDao.latestNews(Some(jim)) must have length 1

      userNewsCategoryDao.setSubscribedCategories(jim, Seq(someCategory.id))

      // The same item is not returned twice
      newsDao.latestNews(Some(jim)) must have length 1
    }

    "return all public news for anonymous user" in transaction { implicit c =>
      // Anonymous user should see all news as they cannot choose categories
      save(londonsBurning.copy(ignoreCategories = false), Set(public))

      val anonNews = newsDao.latestNews(None)

      anonNews.map(_.title) must contain(londonsBurning.title)
    }
  }

  "getNewsByIds" should {

    "return nothing" in transaction { implicit c =>
      newsDao.getNewsByIds(Seq.empty) mustBe Seq.empty
    }

    "return a single news item" in transaction { implicit c =>
      val id = save(londonsBurning, Set(public))

      newsDao.getNewsByIds(Seq(id)) must have length 1
    }

    "return multiple news items" in transaction { implicit c =>
      val id1 = save(londonsBurning, Set(public))
      val id2 = save(brumPanic, Set(ana, eli))

      newsDao.getNewsByIds(Seq(id1, id2)) must have length 2
    }

  }

  "getNewsById" should {

    "return None for a non-existent news item" in transaction { implicit c =>
      newsDao.getNewsById("nonsense") mustBe empty
    }

    "return a news item that exists" in transaction { implicit c =>
      val id = save(londonsBurning, Set(public))

      newsDao.getNewsById(id) mustNot be(empty)
    }

  }

  "getNewsItemsMatchingAudience" should {
    "return an empty list if no parameters are specified" in transaction { implicit c =>
      newsDao.getNewsItemsMatchingAudience(
        webGroup = None,
        departmentCode = None,
        departmentSubset = None,
        publisherId = None,
        limit = 10
      ) mustBe empty
    }

    "find news items for a department subset" in transaction { implicit c =>
      val audienceId = audienceDao.saveAudience(Audience(Seq(DepartmentAudience("IN", Seq(Staff)))))
      val newsItemId = newsDao.save(londonsBurning, audienceId, AudienceSize.Finite(1))

      newsDao.getNewsItemsMatchingAudience(
        webGroup = None,
        departmentCode = Some("IN"),
        departmentSubset = Some(Staff),
        publisherId = None,
        limit = 10
      ) must contain only newsItemId
    }

    "find all news items for a department" in transaction { implicit c =>
      val audienceId = audienceDao.saveAudience(Audience(Seq(DepartmentAudience("IN", Seq(UndergradStudents, TeachingStaff)))))
      val newsItemId = newsDao.save(londonsBurning, audienceId, AudienceSize.Finite(1))

      newsDao.getNewsItemsMatchingAudience(
        webGroup = None,
        departmentCode = Some("IN"),
        departmentSubset = None,
        publisherId = None,
        limit = 10
      ) must contain only newsItemId
    }

    "find news items for a webgroup" in transaction { implicit c =>
      val audienceId = audienceDao.saveAudience(Audience.webGroup(GroupName("in-test")))
      val newsItemId = newsDao.save(londonsBurning, audienceId, AudienceSize.Finite(1))

      newsDao.getNewsItemsMatchingAudience(
        webGroup = Some(GroupName("in-test")),
        departmentCode = None,
        departmentSubset = None,
        publisherId = None,
        limit = 10
      ) must contain only newsItemId
    }

    "find news items created by a publisher" in transaction { implicit c =>
      val audienceId = audienceDao.saveAudience(Audience.Public)
      val newsItemId = newsDao.save(londonsBurning, audienceId, AudienceSize.Finite(1))

      newsDao.getNewsItemsMatchingAudience(
        webGroup = None,
        departmentCode = None,
        departmentSubset = None,
        publisherId = Some("publisher"),
        limit = 10
      ) must contain only newsItemId
    }

    "not match anything" in transaction { implicit c =>
      newsDao.getNewsItemsMatchingAudience(
        webGroup = None,
        departmentCode = None,
        departmentSubset = None,
        publisherId = Some("publisher"),
        limit = 10
      ) mustBe empty
    }
  }

}
