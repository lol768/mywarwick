package system

object EitherValidation {

  implicit def fromEither[A, B](either: Either[A, B]): EitherValidation[A, B] = new EitherValidation[A, B](either)

}

/*
 * Adds a little bit of sugar to Either for validating some input
 */
class EitherValidation[A, B](either: Either[A, B]) {

  def verifying(condition: (B) => Boolean, otherwise: A): Either[A, B] =
    either.fold(
      Left(_),
      value => if (condition(value)) Right(value) else Left(otherwise)
    )

  def andThen[X](fn: B => Either[A, X]): Either[A, X] =
    either.fold(Left(_), fn)

}