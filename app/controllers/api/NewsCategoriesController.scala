package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.MyController
import controllers.publish.CategoryOptions
import models.API
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json._
import services.{NewsCategoryService, SecurityService, UserNewsCategoryService}

@Singleton
class NewsCategoriesController @Inject()(
  security: SecurityService,
  val newsCategoryService: NewsCategoryService,
  userNewsCategoryService: UserNewsCategoryService
) extends MyController with CategoryOptions {

  import security._

  def categories = UserAction { request =>
    val allCategories = newsCategoryService.all()
    val subscribedCategories = request.context.user.map(_.usercode).map(userNewsCategoryService.getSubscribedCategories).getOrElse(allCategories.map(_.id))

    Ok(Json.toJson(API.Success(data = Json.obj(
      "items" -> allCategories,
      "subscribed" -> subscribedCategories
    ))))
  }

  val categoriesForm = Form(single("categories" -> categoryMappingAllowingEmpty))

  def update = RequiredUserAction { implicit request =>
    val user = request.context.user.get // RequiredUserAction

    categoriesForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(Json.toJson(API.Failure[Seq[API.Error]]("Bad Request", formWithErrors.errors.map(e => API.Error(e.key, e.message)))))
      },
      categories => {
        userNewsCategoryService.setSubscribedCategories(user.usercode, categories)

        // TODO: allow returning empty data with API.Success, use API.Success here
        Ok(Json.obj(
          "success" -> true,
          "status" -> "ok"
        ))
      })
  }
}
