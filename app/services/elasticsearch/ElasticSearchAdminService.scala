package services.elasticsearch

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[ElasticSearchAdminServiceImpl])
trait ElasticSearchAdminService {

  // config template

  // remove index

  // etc.

}

@Singleton
class ElasticSearchAdminServiceImpl @Inject()(

)
  extends ElasticSearchAdminService {

}
