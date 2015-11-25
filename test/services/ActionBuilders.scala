package services

import play.api.mvc.{Result, Request, ActionBuilder}
import warwick.sso.{AuthenticatedRequest, LoginContext}

import scala.concurrent.Future

/**
  * Some ActionBuilders that can be used in place of real ones. They mainly
  * do nothing other than run the code block they're given, letting you test
  * the code in that block.
  */
object ActionBuilders {
  /**
    * ActionBuilder that doesn't do anything extra - just runs the block.
    * wrap() needs to be implemented so it knows how to get from Request to R (which may be a subclass)
    */
  abstract class Null[R[_]] extends ActionBuilder[R] {
    def wrap[A](req: Request[A]) : R[A]
    override def invokeBlock[A](request: Request[A], block: (R[A]) => Future[Result]): Future[Result] = block(wrap(request))
  }

  /**
    * Use this for a simple Request -> Request
    */
  object Null extends Null[Request] {
    override def wrap[A](req: Request[A]): Request[A] = req
  }

  /**
    * Transforms a Request into an AuthenticatedRequest using the provided LoginContext.
    */
  class NullSecure(ctx: LoginContext) extends Null[AuthenticatedRequest] {
    override def wrap[A](req: Request[A]): AuthenticatedRequest[A] = new AuthenticatedRequest[A](ctx, req)
  }
}
