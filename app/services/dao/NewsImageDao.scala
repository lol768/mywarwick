package services.dao

import java.sql.Connection
import java.util.UUID
import javax.inject.Singleton

import anorm.SqlParser._
import anorm._
import com.google.inject.ImplementedBy
import org.joda.time.DateTime

case class NewsImageSave(
  width: Int,
  height: Int,
  contentType: String,
  contentLength: Long
)

case class NewsImage(
  id: String,
  width: Int,
  height: Int,
  contentType: String,
  contentLength: Long,
  createdAt: DateTime
)

@ImplementedBy(classOf[NewsImageDaoImpl])
trait NewsImageDao {

  def save(newsImageSave: NewsImageSave)(implicit c: Connection): String

  def find(id: String)(implicit c: Connection): Option[NewsImage]

}

@Singleton
class NewsImageDaoImpl extends NewsImageDao {

  import warwick.anorm.converters.ColumnConversions._

  def save(newsImageSave: NewsImageSave)(implicit c: Connection) = {
    import newsImageSave._
    val id = UUID.randomUUID().toString
    val createdAt = DateTime.now

    SQL"INSERT INTO NEWS_IMAGE (ID, WIDTH, HEIGHT, CONTENT_TYPE, CONTENT_LENGTH, CREATED_AT) VALUES ($id, $width, $height, $contentType, $contentLength, $createdAt)"
      .executeUpdate()

    id
  }

  def find(id: String)(implicit c: Connection) = {
    SQL"SELECT * FROM NEWS_IMAGE WHERE ID = $id"
      .as(newsImageParser.singleOpt)
  }

  val newsImageParser: RowParser[NewsImage] = {
    get[String]("ID") ~
      get[Int]("WIDTH") ~
      get[Int]("HEIGHT") ~
      get[String]("CONTENT_TYPE") ~
      get[Int]("CONTENT_LENGTH") ~
      get[DateTime]("CREATED_AT") map {
      case id ~ width ~ height ~ contentType ~ contentLength ~ createdAt =>
        NewsImage(id, width, height, contentType, contentLength, createdAt)
    }
  }

}
