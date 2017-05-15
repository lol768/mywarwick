package controllers.api

import helpers.BaseSpec
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}

class JsExceptionTest extends BaseSpec with MockitoSugar {

  "JsException" should {

    "parse an error" in {
      val errorJson = Json.parse(
        """
          {
          	"source": "https://my.warwick.ac.uk/assets/js/57bf7e76e8229a5a14bb14cb924a1313-bundle.js",
          	"line": 1,
          	"stack": "TypeError: n.find is not a function\n
                at https://my.warwick.ac.uk/assets/js/57bf7e76e8229a5a14bb14cb924a1313-bundle.js:1:112245\n
          	    at Array.forEach (native)\n
          	    at Object.i [as getGroupedItems] (https://my.warwick.ac.uk/assets/js/57bf7e76e8229a5a14bb14cb924a1313-bundle.js:1:112117)\n
          	    at t.b.value (https://my.warwick.ac.uk/assets/js/57bf7e76e8229a5a14bb14cb924a1313-bundle.js:1:62593)\n
          	    at _._renderValidatedComponentWithoutOwnerOrContext (https://my.warwick.ac.uk/assets/js/f9275edb297e273242157f51b321bd2c-vendor.bundle.js:1:152832)\n
          	    at _.performInitialMount (https://my.warwick.ac.uk/assets/js/f9275edb297e273242157f51b321bd2c-vendor.bundle.js:1:148817)\n
          	    at Object.i.mountComponent (https://my.warwick.ac.uk/assets/js/f9275edb297e273242157f51b321bd2c-vendor.bundle.js:1:61910)",
          	"message": "Uncaught TypeError: n.find is not a function",
          	"column": 112245,
          	"time": 1494604999176
          }
        """.replaceAll("[\u0000-\u001f]", "")).validate[Map[String, JsValue]].get
      val result = new JsException(errorJson)
      result.getStackTrace.length mustBe 7
    }
  }

}
