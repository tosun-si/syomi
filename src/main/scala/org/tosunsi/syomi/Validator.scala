package org.tosunsi.syomi

/**
 * Monad that allows to validate a generic object.
 * The goal is to validates the desired fields of this object.
 * The api allows to do a projection on a field and to apply a predicate to this.
 * The associated error message is passed.
 *
 * All the messages are stacked and the client of api can get them with different form (list, option, either).
 * We can perform a side effect and throw a runtime exception if there is an error.
 * This exception is passed in a final method with a class or a supplier.
 *
 * @param element       generic object (with covariance)
 * @param errorMessages error messages
 * @tparam A type of the generic object
 */
case class Validator[+A](private val element: A, errorMessages: Seq[String]) {

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
  def validate[R](projection: A => R)(predicateOnField: R => Boolean)(message: String): Validator[A] = {
    Option(element)
      .map(projection)
      .filter(predicateOnField)
      .map(_ => this)
      .getOrElse(Validator(element, errorMessages.:+(message)))
  }

  def validateOnObject[R](predicateOnObject: A => Boolean)(message: String): Validator[A] = {
    Option(element)
      .filter(predicateOnObject)
      .map(_ => this)
      .getOrElse(Validator(element, errorMessages.:+(message)))
  }

  /**
   * Allows to go to an object in the genric object.
   * Then, the validator will validates the fields of the mapped object.
   *
   * @param toOtherObject mapper on an another object
   * @tparam U type of the mapped object
   * @return this
   */
  def thenTo[U](toOtherObject: A => U): Validator[U] = {
    Validator(toOtherObject(element), errorMessages)
  }

  /**
   * Gets the given generic object or throws an IllegalArgumentException.
   */
  @throws(classOf[IllegalArgumentException])
  def getOrElseThrow: A = {
    getOrElseThrow(classOf[IllegalArgumentException])
  }

  /**
   * Gets the given generic object or throws an exception by the given exception class.
   *
   * @param exceptionClass exception class
   * @tparam E type of exception
   */
  @throws(classOf[RuntimeException])
  def getOrElseThrow[E <: RuntimeException](exceptionClass: Class[E]): A = {
    errorMessages match {
      case err if err.isEmpty => element
      case _                  => throw getErrorMessagesException(exceptionClass)
    }
  }

  /**
   * Gets the given generic object or throws an exception by the given supplier of exception .
   *
   * @param exceptionInstance supplier of exception instance
   * @tparam E type of exception
   */
  def getOrElseThrow[E <: RuntimeException](exceptionInstance: => RuntimeException): A = {
    errorMessages match {
      case err if err.isEmpty => element
      case _                  => throw addErrorMessagesToException(exceptionInstance)
    }
  }

  /**
   * Returns an Either with error messages in left and object in right.
   */
  def toEither: Either[Seq[String], A] = {
    errorMessages match {
      case err if err.isEmpty => Right(element)
      case err                => Left(err)
    }
  }

  /**
   * Returns an Option that contains errors.
   */
  def toErrorsOption: Option[Seq[String]] = {
    errorMessages match {
      case err if err.isEmpty => None
      case err                => Some(err)
    }
  }

  /**
   * Returns errors.
   */
  def toErrors: Seq[String] = {
    errorMessages
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
    errorMessages
      .map(new IllegalArgumentException(_))
      .foreach(exceptionInstance.addSuppressed)

    exceptionInstance
  }
}

object Validator {

  def of[A](element: A): Validator[A] = {
    Validator(element, Nil)
  }
}
