package services.elasticsearch

import helpers.BaseSpec
import org.joda.time.{DateTime, Interval}
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.ac.warwick.util.core.jodatime.DateTimeUtils


class ActivityESServiceHelperTest extends BaseSpec with MockitoSugar {
  
  "ActivityESServiceHelper" should {

    "produce correct monthly index name for alerts and activity" in {

      DateTimeUtils.useMockDateTime(DateTime.parse("2016-06-30T01:20"), () => {

        ActivityESServiceHelper.indexNameToday() must be("alert_2016_06")

        ActivityESServiceHelper.indexNameToday(false) must be("activity_2016_06")
      })
    }

    "produce correct all time index name for alerts and activity" in {

      ActivityESServiceHelper.indexNameForAllTime() must be("alert*")

      ActivityESServiceHelper.indexNameForAllTime(false) must be("activity*")

    }
  }

  "ActivityESServiceHelper" should {

    "make elasticsearch document builder from activity document correctly" in {

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
        Seq("user1", "user2"),
        api = true,
        created_at = new DateTime(10000),
        created_by = "custard"
      )

      val result = ActivityESServiceHelper.elasticSearchContentBuilderFromActivityDocument(activityDoc)

      result.string() must be("""{"activity_id":"test0","provider_id":"test1","activity_type":"test2","title":"test3","url":"test4","text":"test5","replaced_by":"test6","published_at":"1970-01-01T00:00:00.000Z","publisher":"test1","resolved_users":["user1","user2"],"audience_components":["component1","component2"],"api":true,"created_at":"1970-01-01T00:00:10.000Z","created_by":"custard"}""")

    }

  }

  "ActivityESServiceSearchHelper" should {
    import ActivityESServiceSearchHelper._

    "give index names according to activity type" in {

      ActivityESServiceSearchHelper.partialIndexNameForActivityType(true) must be("alert")
      ActivityESServiceSearchHelper.partialIndexNameForActivityType(false) must be("activity")

    }


    "use elastic multi-index api for date intervals spanning > 1 month" in {
      val interval: Interval = new Interval(
        new DateTime().withYear(2017).withMonthOfYear(2).withDayOfMonth(9),
        new DateTime().withYear(2017).withMonthOfYear(7).withDayOfMonth(21)
      )
      val actual = ActivityESServiceSearchHelper.partialIndexNameForInterval(interval)
      val expected = Seq("2017_02","2017_03","2017_04","2017_05","2017_06","2017_07")

      actual must be(expected)
    }

    "use elastic multi-index api for date intervals spanning > 1 year" in {
      val interval: Interval = new Interval(
        new DateTime().withYear(2017).withMonthOfYear(9).withDayOfMonth(9),
        new DateTime().withYear(2018).withMonthOfYear(2).withDayOfMonth(21)
      )
      val actual = ActivityESServiceSearchHelper.partialIndexNameForInterval(interval)
      val expected = Seq("2017_09","2017_10","2017_11","2017_12","2018_01","2018_02")

      actual must be(expected)
    }

    "return correct multi-index string when end day-of-month < start day-of-month" in {
      val interval: Interval = new Interval(
        new DateTime().withYear(2017).withMonthOfYear(9).withDayOfMonth(21),
        new DateTime().withYear(2018).withMonthOfYear(2).withDayOfMonth(9)
      )
      val actual = ActivityESServiceSearchHelper.partialIndexNameForInterval(interval)
      val expected = Seq("2017_09","2017_10","2017_11","2017_12","2018_01","2018_02")

      actual must be(expected)
    }

    "give correct index name based on datetime interval" in {

      val `20170701`: DateTime = new DateTime().withYear(2017).withMonthOfYear(7).withDayOfMonth(1)
      val `20170801`: DateTime = new DateTime().withYear(2017).withMonthOfYear(8).withDayOfMonth(1)

      val sameYearDifferentMonthInterval = new Interval(`20170701`, `20170801`)

      partialIndexNameForInterval(sameYearDifferentMonthInterval) must be(Seq("2017_07","2017_08"))

      val `20150801`: DateTime = new DateTime().withYear(2015).withMonthOfYear(8).withDayOfMonth(1)
      val `20160801`: DateTime = new DateTime().withYear(2016).withMonthOfYear(8).withDayOfMonth(1)

      val differentYear = new Interval(`20150801`, `20160801`)

      partialIndexNameForInterval(differentYear) must be(Seq("2015_08","2015_09","2015_10","2015_11","2015_12","2016_01","2016_02","2016_03","2016_04","2016_05","2016_06","2016_07","2016_08"))

      val `20170810`: DateTime = new DateTime().withYear(2017).withMonthOfYear(8).withDayOfMonth(10)
      val `20170829`: DateTime = new DateTime().withYear(2017).withMonthOfYear(8).withDayOfMonth(29)

      val sameYearSameMonth = new Interval(`20170810`, `20170829`)
      partialIndexNameForInterval(sameYearSameMonth) must be(Seq("2017_08"))

    }

    "generate correct index for query with no interval or type" in {
      val query1 = ActivityESSearchQuery()
      indexNameForActivitySearchQuery(query1) must be("*")
    }

    "generate correct index for query with type but no interval" in {
      val query2 = ActivityESSearchQuery(isAlert = Some(true))
      indexNameForActivitySearchQuery(query2) must be("alert_*")
    }

    "generate correct index for query with type and interval" in {

      val `20170810`: DateTime = new DateTime().withYear(2017).withMonthOfYear(8).withDayOfMonth(10)
      val `20170829`: DateTime = new DateTime().withYear(2017).withMonthOfYear(8).withDayOfMonth(29)

      val query3 = ActivityESSearchQuery(
        isAlert = Some(true),
        publish_at = Some(new Interval(`20170810`, `20170829`))
      )
      indexNameForActivitySearchQuery(query3) must be("alert_2017_08")
    }

    "generate correct index for query with type and interval over multiple months" in {

      val `20170710`: DateTime = new DateTime().withYear(2017).withMonthOfYear(7).withDayOfMonth(10)
      val `20170829`: DateTime = new DateTime().withYear(2017).withMonthOfYear(8).withDayOfMonth(29)

      val query3 = ActivityESSearchQuery(
        isAlert = Some(true),
        publish_at = Some(new Interval(`20170710`, `20170829`))
      )
      indexNameForActivitySearchQuery(query3) must be("alert_2017_07,alert_2017_08")
    }

    "generate correct index for a with interval but no type" in {

      val `20170810`: DateTime = new DateTime().withYear(2017).withMonthOfYear(8).withDayOfMonth(10)
      val `20170829`: DateTime = new DateTime().withYear(2017).withMonthOfYear(8).withDayOfMonth(29)

      val query4 = ActivityESSearchQuery(
        publish_at = Some(new Interval(`20170810`, `20170829`))
      )
      indexNameForActivitySearchQuery(query4) must be("*_2017_08")
    }

    "generate correct index for a with interval but no type over multiple months" in {

      val `20170710`: DateTime = new DateTime().withYear(2017).withMonthOfYear(7).withDayOfMonth(10)
      val `20170829`: DateTime = new DateTime().withYear(2017).withMonthOfYear(8).withDayOfMonth(29)

      val query4 = ActivityESSearchQuery(
        publish_at = Some(new Interval(`20170710`, `20170829`))
      )
      indexNameForActivitySearchQuery(query4) must be("*_2017_07,*_2017_08")
    }

    "build an empty query if the supplied ActivityESSearchQuery is empty" in {

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


    "should have correct must term query for activity id" in {

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

    "should have correct must query for provider id" in {

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

    "should have correct must term query for activity type" in {

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

    "should have correct must term query for publisher" in {

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


    "should have correct must match query for text" in {

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


    "should have correct must match query for title" in {

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


    "should have correct must query for published_at time range" in {

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

    "should have correct must term query for url" in {

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

    "should have multiple should match queries for each element in audience components" in {


      val query = ActivityESSearchQuery(
        audienceComponents = Some(Seq("com1", "com2", "com3", "wat"))
      )

      val result = ActivityESServiceSearchHelper.makeBoolQueryBuilder(query)

      val expect = Json.parse(
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

    "should have multiple should term quieres for each usercode in resolved users" in {

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
