package services.elasticsearch

import java.util.Date

import helpers.BaseSpec
import org.joda.time.{DateTime, Interval}
import org.scalatest.concurrent.PatienceConfiguration.Interval
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.ac.warwick.util.core.jodatime.DateTimeUtils
import uk.ac.warwick.util.core.jodatime.DateTimeUtils.Callback


class ActivityESServiceHelperTest extends BaseSpec with MockitoSugar {

  class Scope {

  }

  "ActivityESServiceHelper" should {

    "produce correct monthly index name for alerts and activity" in new Scope {

      DateTimeUtils.useMockDateTime(DateTime.parse("2016-06-30T01:20"), new Callback {
        override def doSomething() = {

          ActivityESServiceHelper.indexNameToday(true, DateTime.now().toString("yyyy_MM")) must be("alert_2016_06")

          ActivityESServiceHelper.indexNameToday(false, DateTime.now().toString("yyyy_MM")) must be("activity_2016_06")
        }
      })


    }

    "produce correct all time index name for alerts and activity" in new Scope {

      ActivityESServiceHelper.indexNameForAllTime() must be("alert*")

      ActivityESServiceHelper.indexNameForAllTime(false) must be("activity*")

    }
  }

  "ActivityESServiceHelper" should {

    "make elasticsearch document builder from activity document correctly" in new Scope {

      val activityDoc = ActivityDocument(
        "test0",
        "test1",
        "test2",
        "test3",
        "test4",
        "test5",
        "test6",
        new DateTime(0),
        "test1",
        Seq("component1", "component2"),
        Seq("user1", "user2")
      )

      val helper = ActivityESServiceHelper
      val result = helper.elasticSearchContentBuilderFromActivityDocument(activityDoc)

      result.string() must be("""{"activity_id":"test0","provider_id":"test1","activity_type":"test2","title":"test3","url":"test4","text":"test5","replaced_by":"test6","published_at":"1970-01-01T00:00:00.000Z","publisher":"test1","resolved_users":["user1","user2"],"audience_components":["component1","component2"]}""")

    }

  }

  "ActivityESServiceSearchHelper" should {

    "build an empty query if the supplied ActivityESSearchQuery is empty" in new Scope {

      val query = ActivityESSearchQuery()

      val result = ActivityESServiceSearchHelper.makeBoolQueryBuilder(query)

      val expect = Json.parse(
        """
          {
            "bool" : {
              "adjust_pure_negative" : true,
              "boost" : 1.0
            }
          }
        """)
      Json.parse(result.toString()) must be(expect)
    }


    "should have correct must term query for activity id" in new Scope {

      var query = ActivityESSearchQuery(Some("sdflsdkfj-sdflksdjf_1231"))
      val result = ActivityESServiceSearchHelper.makeBoolQueryBuilder(query)

      val expect = Json.parse(
        """
          {
            "bool": {
              "must": [
                {
                  "term": {
                    "activity_id": {
                      "value": "sdflsdkfj-sdflksdjf_1231",
                      "boost": 1
                    }
                  }
                }
              ],
              "adjust_pure_negative": true,
              "boost": 1
            }
          }
        """
      )

      Json.parse(result.toString) must be(expect)

    }

    "should have correct must query for provider id" in new Scope {

      var query = ActivityESSearchQuery(None, Some("super-provider_id-id"))
      val result = ActivityESServiceSearchHelper.makeBoolQueryBuilder(query)

      val expect = Json.parse(
        """
          {
            "bool": {
              "must": [
                {
                  "term": {
                    "provider_id": {
                      "value": "super-provider_id-id",
                      "boost": 1
                    }
                  }
                }
              ],
              "adjust_pure_negative": true,
              "boost": 1
            }
          }
        """
      )
      Json.parse(result.toString()) must be(expect)
    }

    "should have correct must term query for activity type" in new Scope {

      var query = ActivityESSearchQuery(None, None, Some("cool-activity-type"))
      val result = ActivityESServiceSearchHelper.makeBoolQueryBuilder(query)

      val expect = Json.parse(
        """
          {
            "bool": {
              "must": [
                {
                  "term": {
                    "activity_type": {
                      "value": "cool-activity-type",
                      "boost": 1
                    }
                  }
                }
              ],
              "adjust_pure_negative": true,
              "boost": 1
            }
          }
        """
      )
      Json.parse(result.toString()) must be(expect)
    }

    "should have correct must term query for publisher" in new Scope {

      var query = ActivityESSearchQuery(
        publisher = Some("cool-publisher")
      )
      val result = ActivityESServiceSearchHelper.makeBoolQueryBuilder(query)

      val expect = Json.parse(
        """
          {
            "bool": {
              "must": [
                {
                  "term": {
                    "publisher": {
                      "value": "cool-publisher",
                      "boost": 1
                    }
                  }
                }
              ],
              "adjust_pure_negative": true,
              "boost": 1
            }
          }
        """
      )
      Json.parse(result.toString()) must be(expect)
    }


    "should have correct must match query for text" in new Scope {

      var query = ActivityESSearchQuery(text = Some("this and that"))
      val result = ActivityESServiceSearchHelper.makeBoolQueryBuilder(query)

      val expect = Json.parse(
        """
          {
            "bool": {
              "must": [
                {
                  "match": {
                    "text": {
                      "query": "this and that",
                      "operator": "OR",
                      "prefix_length": 0,
                      "max_expansions": 50,
                      "fuzzy_transpositions": true,
                      "lenient": false,
                      "zero_terms_query": "NONE",
                      "boost": 1
                    }
                  }
                }
              ],
              "adjust_pure_negative": true,
              "boost": 1
            }
          }
        """
      )
      Json.parse(result.toString()) must be(expect)
    }


    "should have correct must match query for title" in new Scope {

      var query = ActivityESSearchQuery(title = Some("wonderful title"))
      val result = ActivityESServiceSearchHelper.makeBoolQueryBuilder(query)

      val expect = Json.parse(
        """
          {
            "bool": {
              "must": [
                {
                  "match": {
                    "title": {
                      "query": "wonderful title",
                      "operator": "OR",
                      "prefix_length": 0,
                      "max_expansions": 50,
                      "fuzzy_transpositions": true,
                      "lenient": false,
                      "zero_terms_query": "NONE",
                      "boost": 1
                    }
                  }
                }
              ],
              "adjust_pure_negative": true,
              "boost": 1
            }
          }
        """
      )
      Json.parse(result.toString()) must be(expect)
    }


    "should have correct must query for published_at time range" in new Scope {

      var range = new Interval(DateTime.parse("2017-09-13T12:33:28.855Z"), DateTime.parse("2017-09-13T12:35:28.855Z"))

      var query = ActivityESSearchQuery(
        publish_at = Some(range)
      )
      val result = ActivityESServiceSearchHelper.makeBoolQueryBuilder(query)

      val expect = Json.parse(
        s"""
        {
          "bool": {
            "must": [
              {
                "range": {
                  "published_at": {
                    "from": "${range.getStart}",
                    "to": "${range.getEnd}",
                    "include_lower": true,
                    "include_upper": true,
                    "boost": 1
                  }
                }
              }
            ],
            "adjust_pure_negative": true,
            "boost": 1
          }
        }
      """
      )
      Json.parse(result.toString()) must be(expect)
    }

    "should have correct must term query for url" in new Scope {

      var query = ActivityESSearchQuery(
        url = Some("https://fake.fake.com")
      )
      val result = ActivityESServiceSearchHelper.makeBoolQueryBuilder(query)

      val expect = Json.parse(
        """
          {
            "bool": {
              "must": [
                {
                  "term": {
                    "url": {
                      "value": "https://fake.fake.com",
                      "boost": 1
                    }
                  }
                }
              ],
              "adjust_pure_negative": true,
              "boost": 1
            }
          }
        """
      )
      Json.parse(result.toString()) must be(expect)
    }

    "should have multiple should match queries for each element in audience components" in new Scope {


      val query = ActivityESSearchQuery(
        audienceComponents = Some(Seq("com1", "com2", "com3", "wat"))
      )

      val result = ActivityESServiceSearchHelper.makeBoolQueryBuilder(query)

      val expect =Json.parse(
        """
           {
             "bool": {
               "must": [
                 {
                   "bool": {
                     "should": [
                       {
                         "match": {
                           "audience_components": {
                             "query": "com1",
                             "operator": "OR",
                             "prefix_length": 0,
                             "max_expansions": 50,
                             "fuzzy_transpositions": true,
                             "lenient": false,
                             "zero_terms_query": "NONE",
                             "boost": 1
                           }
                         }
                       },
                       {
                         "match": {
                           "audience_components": {
                             "query": "com2",
                             "operator": "OR",
                             "prefix_length": 0,
                             "max_expansions": 50,
                             "fuzzy_transpositions": true,
                             "lenient": false,
                             "zero_terms_query": "NONE",
                             "boost": 1
                           }
                         }
                       },
                       {
                         "match": {
                           "audience_components": {
                             "query": "com3",
                             "operator": "OR",
                             "prefix_length": 0,
                             "max_expansions": 50,
                             "fuzzy_transpositions": true,
                             "lenient": false,
                             "zero_terms_query": "NONE",
                             "boost": 1
                           }
                         }
                       },
                       {
                         "match": {
                           "audience_components": {
                             "query": "wat",
                             "operator": "OR",
                             "prefix_length": 0,
                             "max_expansions": 50,
                             "fuzzy_transpositions": true,
                             "lenient": false,
                             "zero_terms_query": "NONE",
                             "boost": 1
                           }
                         }
                       }
                     ],
                     "adjust_pure_negative": true,
                     "boost": 1
                   }
                 }
               ],
               "adjust_pure_negative": true,
               "boost": 1
             }
           }
        """)
      Json.parse(result.toString()) must be(expect)

    }

    "should have multiple should term quieres for each usercode in resolved users" in new Scope {

      val query = ActivityESSearchQuery(
        activity_id = Some("123456"),
        publisher = Some("its"),
        resolvedUsers = Some(Seq("user1", "user2", "user3", "cat"))
      )

      val result = ActivityESServiceSearchHelper.makeBoolQueryBuilder(query)

      val expect = Json.parse(
        """
          {
            "bool": {
              "must": [
                {
                  "term": {
                    "activity_id": {
                      "value": "123456",
                      "boost": 1
                    }
                  }
                },
                {
                  "term": {
                    "publisher": {
                      "value": "its",
                      "boost": 1
                    }
                  }
                },
                {
                  "bool": {
                    "should": [
                      {
                        "term": {
                          "resolved_users": {
                            "value": "user1",
                            "boost": 1
                          }
                        }
                      },
                      {
                        "term": {
                          "resolved_users": {
                            "value": "user2",
                            "boost": 1
                          }
                        }
                      },
                      {
                        "term": {
                          "resolved_users": {
                            "value": "user3",
                            "boost": 1
                          }
                        }
                      },
                      {
                        "term": {
                          "resolved_users": {
                            "value": "cat",
                            "boost": 1
                          }
                        }
                      }
                    ],
                    "adjust_pure_negative": true,
                    "boost": 1
                  }
                }
              ],
              "adjust_pure_negative": true,
              "boost": 1
            }
          }
        """
      )
      Json.parse(result.toString()) must be(expect)
    }

  }


}
