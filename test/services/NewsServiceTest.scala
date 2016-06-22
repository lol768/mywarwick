package services

import helpers.{Fixtures, OneStartAppPerSuite}
import models.news.{Audience, NewsItemSave}
import org.scalatestplus.play.PlaySpec


class NewsServiceTest extends PlaySpec with OneStartAppPerSuite {

  val newsService = get[NewsService]

  val allCategories = get[PublishCategoryService].all

  "NewsService" should {
    "save items" in {
      val cats = allCategories.take(2).map(_.id)
      val item = Fixtures.news.save()
      val audience = Audience.Public
      val id = newsService.save(item, audience, cats)
      val fetched = newsService.getNewsItem(id)
      fetched.get.title mustBe item.title
    }
  }

}
