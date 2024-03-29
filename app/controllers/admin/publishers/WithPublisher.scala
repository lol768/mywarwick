package controllers.admin.publishers

import controllers.MyController
import models.publishing.Publisher
import play.api.mvc.Result
import services.PublisherService
import system.RequestContext

trait WithPublisher {

  self: MyController =>

  def publisherService: PublisherService

  protected def withPublisher(publisherId: String, block: (Publisher) => Result)(implicit request: RequestContext): Result = {
    publisherService.find(publisherId)
      .map(block)
      .getOrElse(NotFound(views.html.errors.notFound()))
  }

}
