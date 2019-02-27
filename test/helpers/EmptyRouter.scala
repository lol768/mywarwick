package helpers

import play.api.routing.Router

class EmptyRouter extends Router {
  override def documentation: Seq[(String, String, String)] = Nil
  override def withPrefix(prefix: String): Router = this
  override def routes: Router.Routes = PartialFunction.empty
}
