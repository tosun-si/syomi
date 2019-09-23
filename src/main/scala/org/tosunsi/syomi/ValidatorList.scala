package org.tosunsi.syomi

/**
 * This class gives the same treatments of the [[Validator]] class but it validates each object of the given list.
 *
 * @param list generic object (with covariance)
 * @tparam A type of the generic object
 */
case class ValidatorList[A](private val list: Seq[A], private val predicatesAndMessages: Seq[(A => Boolean, String)]) {

  /**
   * Validates a fields of the generic object with the composition of a projection with a predicate on this.
   * The associated message is given.
   *
   * @param projection       projection on a field
   * @param predicateOnField predicate on this field
   * @param message          error message for this field
   * @tparam R type of the field
   * @return this
   */
  def validate[R](projection: A => R)(predicateOnField: R => Boolean)(message: String): ValidatorList[A] = {
    val composedFunctionOnObject: A => Boolean = projection andThen predicateOnField

    ValidatorList(list, predicatesAndMessages.:+((composedFunctionOnObject, message)))
  }

  /**
   * Allows to go to an object in the genric object.
   * Then, the validator will validates the fields of the mapped object.
   *
   * @param toOtherObject mapper on an another object
   * @tparam U type of the mapped object
   * @return this
   */
  def thenTo[U](toOtherObject: A => U): ValidatorList[U] = {
    val mappedList: Seq[U] = list.map(toOtherObject)

    ValidatorList(mappedList, Nil)
  }

  /**
   * Gets the given generic object or throws an IllegalArgumentException.
   */
  @throws(classOf[IllegalArgumentException])
  def getOrElseThrow: Seq[A] = {
    getOrElseThrow(classOf[IllegalArgumentException])
  }

  /**
   * Gets the given generic object or throws an exception by the given exception class.
   *
   * @param exceptionClass exception class
   * @tparam E type of exception
   */
  @throws(classOf[RuntimeException])
  def getOrElseThrow[E <: RuntimeException](exceptionClass: Class[E]): Seq[A] = {
    val errorMessages = getAllErrorMessages

    errorMessages match {
      case err if err.isEmpty => list
      case _                  => throw getErrorMessagesException(exceptionClass)
    }
  }

  private def getAllErrorMessages: Seq[String] = {
    list
      .zipWithIndex
      .flatMap(objectWithIndex => getErrorMessages(objectWithIndex._1, objectWithIndex._2))
  }

  private def getErrorMessages(element: A, index: Int): Seq[String] = {
    val validator = Validator.of(element)

    predicatesAndMessages
      .flatMap(pm => validator.validateOnObject(pm._1)(s"[$index] ${pm._2}").toErrors)
  }

  /**
   * Throws the given exception that contains all the error messages (suppressed of exception).
   */
  private def getErrorMessagesException[E <: RuntimeException](exceptionClass: Class[E]): RuntimeException = {
    val exception: RuntimeException = exceptionClass.getDeclaredConstructor().newInstance()

    addErrorMessagesToException(exception)
  }

  /**
   * Adds the error messages in the exception (suppressed).
   */
  private def addErrorMessagesToException[E <: RuntimeException](exceptionInstance: RuntimeException): RuntimeException = {
    getAllErrorMessages
      .map(new IllegalArgumentException(_))
      .foreach(exceptionInstance.addSuppressed)

    exceptionInstance
  }

  /**
   * Gets the given generic object or throws an exception by the given supplier of exception .
   *
   * @param exceptionInstance supplier of exception instance
   * @tparam E type of exception
   */
  def getOrElseThrow[E <: RuntimeException](exceptionInstance: => RuntimeException): Seq[A] = {
    val errorMessages = getAllErrorMessages

    errorMessages match {
      case err if err.isEmpty => list
      case _                  => throw addErrorMessagesToException(exceptionInstance)
    }
  }

  /**
   * Returns an Either with error messages in left and object in right.
   */
  def toEither: Either[Seq[String], Seq[A]] = {
    val errorMessages = getAllErrorMessages

    errorMessages match {
      case err if err.isEmpty => Right(list)
      case err                => Left(err)
    }
  }

  /**
   * Returns an Option that contains errors.
   */
  def toErrorsOption: Option[Seq[String]] = {
    val errorMessages = getAllErrorMessages

    errorMessages match {
      case err if err.isEmpty => None
      case err                => Some(err)
    }
  }

  /**
   * Returns errors.
   */
  def toErrors: Seq[String] = {
    getAllErrorMessages
  }
}

object ValidatorList {

  def of[A](list: Seq[A]): ValidatorList[A] = {
    ValidatorList(list, Nil)
  }
}


