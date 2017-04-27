package helpers.embedded

import java.io.InputStreamReader
import java.sql.Connection
import java.util.Scanner

import anorm._
import com.google.common.base.Charsets

/**
  * For running fixture scripts on a database.
  */
trait SqlScriptRunner {

  def runSqlScript(name: String)(implicit c: Connection) = {
    val input = getClass.getClassLoader.getResourceAsStream(name)
    if (input == null) throw new IllegalArgumentException(s"Resource ${name} not found")
    val scanner = new Scanner(new InputStreamReader(input, Charsets.UTF_8)).useDelimiter(";")
    while (scanner.hasNext) {
      val stmt = scanner.next
      SQL(stmt).execute()
    }
  }

}
