package services

import helpers.{Fixtures, OneStartAppPerSuite}
import models.news.Audience
import models.news.Audience.{DepartmentAudience, Staff}
import org.scalatestplus.play.PlaySpec


class NewsServiceTest extends PlaySpec with OneStartAppPerSuite {

  val newsService = get[NewsService]
  val categoryIds = get[NewsCategoryService].all().map(_.id)

  val staffAudience = Audience(Seq(DepartmentAudience("IN", Seq(Staff))))
  val item = Fixtures.news.save()

  "NewsService" should {

//    "save items" in {
//      val id = newsService.save(item, Audience.Public, categoryIds.take(2))
//
//      val render = newsService.getNewsItem(id).get
//
//      render must have('title (item.title))
//    }
//
//    "update items" in {
//      val id = newsService.save(item, Audience.Public, categoryIds.take(1))
//
//      newsService.update(id, item, staffAudience, categoryIds.take(2))
//
//      val render = newsService.getNewsItem(id).get
//
//      render.categories.map(_.id) mustBe categoryIds.take(2)
//      newsService.getAudience(id) must contain(staffAudience)
//    }

  }

}
