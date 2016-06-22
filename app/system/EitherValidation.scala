package system

object EitherValidation {

  /*
  * Adds a little bit of sugar to Either for validating some input
  */
  implicit class fromEither[A, B](val either: Either[A, B]) extends AnyVal {
    def andThen[X](fn: B => Either[A, X]): Either[A, X] = either.right.flatMap(fn)

    def verifying(condition: (B) => Boolean, otherwise: A): Either[A, B] = andThen { value =>
      if (condition(value)) Right(value) else Left(otherwise)
    }
  }

}

