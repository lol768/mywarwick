package helpers

/**
  *
  */
abstract class EmbeddedFuncTestBase extends FuncTestBase with EmbeddedServerConfig {
  // set a fake cookie
  def signInAs(user: UserData): Unit = {
    add cookie("insecure-fake-user", user.username)
    reloadPage()
  }

  def signOut(): Unit = {
    delete cookie "insecure-fake-user"
    reloadPage()
  }
}
