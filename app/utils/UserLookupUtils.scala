package utils

import warwick.sso.{Department, UniversityID, User, UserLookupService}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

object UserLookupUtils {

  implicit class ExtendedUserLookupService(val service: UserLookupService) extends AnyVal {
    def getUsersChunked(ids: Seq[UniversityID], includeDisabled: Boolean = false)(implicit ec: ExecutionContext): Try[Map[UniversityID, User]] =
      // return first failure if any, otherwise join up all the results.
      Try {
        val results = parallelChunks(ids) { ids =>
          service.getUsers(ids, includeDisabled)
        }
        results.flatMap { t => t.get }.toMap.seq
      }


    /**
      * Break list of things down into chunks, and process all those chunks in parallel.
      */
    private def parallelChunks[A,B](seq: Seq[A])(forEach: Seq[A] => B)(implicit ec: ExecutionContext): Seq[B] = {
      val futures: Seq[Future[B]] = seq.grouped(100).toSeq.map { item =>
        Future(forEach(item))
      }
      Await.result(Future.sequence(futures), Duration.Inf)
    }

  }

  implicit class UserStringer(val user: User) {
    def toTypeString: String = {
      if (user.isStudent) "Student"
      else if (user.isStaffNotPGR) "Staff"
      else if (user.isStaffOrPGR) "Research student"
      else if (user.isAlumni) "Graduate"
      else if (user.userSource.isDefined) s"${user.userSource.get} user"
      else if (user.isFound) "Non-member"
      else "Non-existent user"
    }
  }

  implicit class DepartmentStringer(val dept: Option[Department]) {
    def toSafeString: String = {
      dept match {
        case Some(d) if d.name.isDefined => d.name.get
        case Some(d) if d.code.isDefined => s"Unknown department (${d.code.get})"
        case _ => "Unknown department"
      }
    }
  }

}
