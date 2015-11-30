package system

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[OracleDatabaseDialect])
trait DatabaseDialect {

  def limit(n: String): String

}

class OracleDatabaseDialect extends DatabaseDialect {

  override def limit(n: String): String = s"FETCH NEXT $n ROWS ONLY"

}

class H2DatabaseDialect extends DatabaseDialect {

  override def limit(n: String): String = s"LIMIT $n"

}
