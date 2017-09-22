package services.job

import helpers.BaseSpec
import org.joda.time.{DateTime, Interval, Period}
import org.scalatest.mockito.MockitoSugar

class ReindexActivityJobHelperTest extends BaseSpec with MockitoSugar {

  import ReindexActivityJobHelper._

  "ReindexActivityJobHelper" should {
    "slice big interval into seq of smaller consecutive intervals" in {

      var start: DateTime = null
      var end: DateTime = null

      start = new DateTime().withYear(2017).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(1).withMinuteOfHour(1).withSecondOfMinute(1).withMillisOfSecond(1)
      end = new DateTime().withYear(2017).withMonthOfYear(6).withDayOfMonth(15).withHourOfDay(1).withMinuteOfHour(1).withSecondOfMinute(1).withMillisOfSecond(1)

      def bigInterval = new Interval(start, end)

      val resultPer30Days = toSmallerIntervals(bigInterval, Period.days(30))

      resultPer30Days must be(
        Seq(
          new Interval(new DateTime("2017-01-01T01:01:01.001Z"), new DateTime("2017-01-31T01:01:01.001Z")),
          new Interval(new DateTime("2017-01-31T01:01:01.001Z"), new DateTime("2017-03-02T01:01:01.001Z")),
          new Interval(new DateTime("2017-03-02T01:01:01.001Z"), new DateTime("2017-04-01T02:01:01.001+01:00")),
          new Interval(new DateTime("2017-04-01T02:01:01.001+01:00"), new DateTime("2017-05-01T02:01:01.001+01:00")),
          new Interval(new DateTime("2017-05-01T02:01:01.001+01:00"), new DateTime("2017-05-31T02:01:01.001+01:00")),
          new Interval(new DateTime("2017-05-31T02:01:01.001+01:00"), new DateTime("2017-06-15T01:01:01.001+01:00"))
        )
      )

      start = new DateTime().withYear(2017).withMonthOfYear(6).withDayOfMonth(13).withHourOfDay(1).withMinuteOfHour(1).withSecondOfMinute(1).withMillisOfSecond(1)
      end = new DateTime().withYear(2017).withMonthOfYear(6).withDayOfMonth(15).withHourOfDay(1).withMinuteOfHour(1).withSecondOfMinute(1).withMillisOfSecond(1)


      val resultPer3Hours = toSmallerIntervals(bigInterval, Period.hours(3))

      resultPer3Hours must be(
        Seq(
          new Interval(new DateTime("2017-06-13T01:01:01.001+01:00"), new DateTime("2017-06-13T04:01:01.001+01:00")),
          new Interval(new DateTime("2017-06-13T04:01:01.001+01:00"), new DateTime("2017-06-13T07:01:01.001+01:00")),
          new Interval(new DateTime("2017-06-13T07:01:01.001+01:00"), new DateTime("2017-06-13T10:01:01.001+01:00")),
          new Interval(new DateTime("2017-06-13T10:01:01.001+01:00"), new DateTime("2017-06-13T13:01:01.001+01:00")),
          new Interval(new DateTime("2017-06-13T13:01:01.001+01:00"), new DateTime("2017-06-13T16:01:01.001+01:00")),
          new Interval(new DateTime("2017-06-13T16:01:01.001+01:00"), new DateTime("2017-06-13T19:01:01.001+01:00")),
          new Interval(new DateTime("2017-06-13T19:01:01.001+01:00"), new DateTime("2017-06-13T22:01:01.001+01:00")),
          new Interval(new DateTime("2017-06-13T22:01:01.001+01:00"), new DateTime("2017-06-14T01:01:01.001+01:00")),
          new Interval(new DateTime("2017-06-14T01:01:01.001+01:00"), new DateTime("2017-06-14T04:01:01.001+01:00")),
          new Interval(new DateTime("2017-06-14T04:01:01.001+01:00"), new DateTime("2017-06-14T07:01:01.001+01:00")),
          new Interval(new DateTime("2017-06-14T07:01:01.001+01:00"), new DateTime("2017-06-14T10:01:01.001+01:00")),
          new Interval(new DateTime("2017-06-14T10:01:01.001+01:00"), new DateTime("2017-06-14T13:01:01.001+01:00")),
          new Interval(new DateTime("2017-06-14T13:01:01.001+01:00"), new DateTime("2017-06-14T16:01:01.001+01:00")),
          new Interval(new DateTime("2017-06-14T16:01:01.001+01:00"), new DateTime("2017-06-14T19:01:01.001+01:00")),
          new Interval(new DateTime("2017-06-14T19:01:01.001+01:00"), new DateTime("2017-06-14T22:01:01.001+01:00")),
          new Interval(new DateTime("2017-06-14T22:01:01.001+01:00"), new DateTime("2017-06-15T01:01:01.001+01:00"))
        )
      )

      start = new DateTime().withYear(2016).withMonthOfYear(6).withDayOfMonth(13).withHourOfDay(1).withMinuteOfHour(1).withSecondOfMinute(1).withMillisOfSecond(1)
      end = new DateTime().withYear(2017).withMonthOfYear(6).withDayOfMonth(15).withHourOfDay(1).withMinuteOfHour(1).withSecondOfMinute(1).withMillisOfSecond(1)

      val resultPer100Days = toSmallerIntervals(bigInterval, Period.days(100))
      resultPer100Days must be(
        Seq(
          new Interval(new DateTime("2016-06-13T01:01:01.001+01:00"), new DateTime("2016-09-21T01:01:01.001+01:00")),
          new Interval(new DateTime("2016-09-21T01:01:01.001+01:00"), new DateTime("2016-12-30T00:01:01.001Z")),
          new Interval(new DateTime("2016-12-30T00:01:01.001Z"), new DateTime("2017-04-09T01:01:01.001+01:00")),
          new Interval(new DateTime("2017-04-09T01:01:01.001+01:00"), new DateTime("2017-06-15T01:01:01.001+01:00"))
        )
      )

    }
  }
}
