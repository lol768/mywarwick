package helpers.remote

import helpers._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

/**
  * A test that runs against a deployed version of the app,
  * rather than an embedded instance. Can test various SSO behaviours,
  * but obviously don't have access to the app's innards.
  */
abstract class RemoteFuncTestBase
  extends FuncTestBase
    with RemoteServerConfig {

  implicit val ec: ExecutionContext = app.actorSystem.dispatcher

  private def getUserInfo: UserInfoResponse = {
    // Check /user/info to see if we're authenticated already.
    val cookies = webDriver.manage().getCookies().asScala
    val cookiesAsString = cookies.map(_.toString).mkString("; ")
    client.url(s"${config.url}/user/info")
      .addHttpHeaders("Cookie" -> cookiesAsString)
      .get()
      .map { response => new UserInfoResponse(response.json) }
      .futureValue
  }

  // go through websignon
  def signInAs(user: UserData): Unit = {
    val userInfo = getUserInfo
    val currentUser = userInfo.usercode

    if (currentUser.contains(user.username)) {
      println(s"Already signed in as ${user.username}")
    } else {
      if (currentUser.nonEmpty) {
        // we're signed in but as someone else. Help!
        goTo(userInfo.logoutUrl)
      }

      goTo(userInfo.loginUrl)

      // let's go
      textField("userName").value = user.username
      pwdField("password").value = user.password
      submit()

      withClue("Still on a login page; is the password correct?") {
        find("userName") shouldNot be(defined)
      }
    }
  }

  def signOut(): Unit = {
    val logoutUrl = getUserInfo.logoutUrl
    go to logoutUrl
  }
}
