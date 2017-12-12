package models

import helpers.BaseSpec
import warwick.sso.Usercode

class AudienceTest extends BaseSpec {

  "Activity" should {
    "be able to tell usercode is invalid" in {
      var invalideUsercode = Usercode("1,bb")
      Audience.helper.isLikelyValidUsercode(invalideUsercode) mustBe false

      invalideUsercode = Usercode("*jd0")
      Audience.helper.isLikelyValidUsercode(invalideUsercode) mustBe false

      invalideUsercode = Usercode("abcsdf,skkdsf,ds233")
      Audience.helper.isLikelyValidUsercode(invalideUsercode) mustBe false
    }

    "be able to tell usercode is valid" in {
      var valideUsercode = Usercode("u1574595")
      Audience.helper.isLikelyValidUsercode(valideUsercode) mustBe true

      valideUsercode = Usercode("u15745_95")
      Audience.helper.isLikelyValidUsercode(valideUsercode) mustBe true
    }

    "be able to tell usercodes are invalid" in {
      val invalid = Seq(
        Usercode("*jd0"),
        Usercode("abcsdf,skkdsf,ds233"),
        Usercode("u1444444"),
      )
      Audience.helper.areValidUsercodes(invalid) mustBe false
    }

    "be able to tell usercodes are valid" in {
      val valid = Seq(
        Usercode("u1771244"),
        Usercode("bspdey"),
        Usercode("giuliazanfabro@warwickgrad.net"),
        Usercode("quirin.gerstenecker@warwickgrad.net"),
        Usercode("hosdfe"),
        Usercode("edpmod"),
        Usercode("wmpgst"),
        Usercode("ecuklr"),
        Usercode("wmpgsu"),
        Usercode("bssja"),
        Usercode("u1737515"),
        Usercode("mhsibf"),
        Usercode("bsqmri"),
        Usercode("fssau"),
        Usercode("in-start-timetablefetcher"),
        Usercode("laumes"),
        Usercode("bsqmrk"),
        Usercode("bsqmrl"),
        Usercode("bsxhjy"),
        Usercode("bsxeep"),
        Usercode("lfpmap"),
        Usercode("clumac"),
        Usercode("ke.qiu@warwickgrad.net"),
        Usercode("esslbc"),
        Usercode("essjdj"),
        Usercode("bswilliamson@warwickgrad.net"),
        Usercode("elpifp"),
        Usercode("edrxax"),
        Usercode("edpmps"),
        Usercode("edpmrh"),
        Usercode("ep_secaam"),
        Usercode("ecplet"),
        Usercode("bsqiku"),
        Usercode("edpmoz"),
        Usercode("lsugdl"),
        Usercode("u1536833"),
        Usercode("eosfap"),
        Usercode("cepyag"),
        Usercode("cepnaq"),
        Usercode("damian.breen@warwickgrad.net"),
        Usercode("u4043269"),
        Usercode("ecumji"),
        Usercode("reviewer2"),
        Usercode("k.fitness@warwickgrad.net"),
        Usercode("pyukdi"),
        Usercode("mdrkbf"),
        Usercode("esuikl"),
        Usercode("cyskam"),
        Usercode("edpium"),
        Usercode("plsmav"),
        Usercode("maujjv"),
        Usercode("u1592109"),
        Usercode("avgi.daferera@warwickgrad.net"),
        Usercode("phumex"),
        Usercode("mounica.tammineedi@warwickgrad.net"),
        Usercode("bsqkvj"),
        Usercode("hi_pmackay"),
        Usercode("bsqflu"),
        Usercode("hi_inieuwenhuis"),
        Usercode("bspm5v"),
        Usercode("andrew.durham@warwickgrad.net"),
      )
      Audience.helper.areValidUsercodes(valid) mustBe true
    }


  }

}
