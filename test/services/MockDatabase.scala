package services

import java.sql._

import javax.sql.DataSource
import org.mockito.Mockito
import play.api.db.{Database, TransactionIsolationLevel}

/** Knows just enough to return a do-nothing Connection when requested. */
class MockDatabase extends Database {
  private def conn() = Mockito.mock(classOf[Connection])

  override def name: String = "mock"

  override def shutdown(): Unit = {}

  override def withConnection[A](block: (Connection) => A): A = block(conn())
  override def withConnection[A](autocommit: Boolean)(block: (Connection) => A): A = block(conn())
  override def withTransaction[A](block: (Connection) => A): A = block(conn())
  override def withTransaction[A](isolationLevel: TransactionIsolationLevel)(block: Connection => A): A = block(conn())
  override def getConnection(): Connection = conn()
  override def getConnection(autocommit: Boolean): Connection = conn()

  override def dataSource: DataSource = ???
  override def url: String = ???

}
