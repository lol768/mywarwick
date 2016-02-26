package services

import models.NewsSource

class NewsService {

  def allSources: Seq[NewsSource] = Seq(
    NewsSource("insite", "/insite/news/intnews2", "#7ecbb6"),
    NewsSource("Latest News", "/newsandevents/news", "#5b3069"),
    NewsSource("IT Services", "/services/its/newschanges/newinitiatives", "#156294"),
    NewsSource("Warwick Sport", "/services/sport/news/newsfeed", "#51576d"),
    NewsSource("Student News", "/students/news/newsevents", "#1595af")
  )

}
