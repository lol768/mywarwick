package services
import warwick.sso.LoginContext

class MockNavigationService(navigation: Seq[Navigation] = Seq.empty) extends NavigationService {
  override def getNavigation(loginContext: LoginContext) = navigation
}
