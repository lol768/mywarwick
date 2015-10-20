package services

import com.google.inject.Singleton
import models.NewsSource

@Singleton
class NewsService {

  def allSources: Seq[NewsSource] = Seq(
    NewsSource("insite", "/insite/news/intnews2"),
    NewsSource("Computer Science", "/fac/sci/dcs/news")
  )

}
