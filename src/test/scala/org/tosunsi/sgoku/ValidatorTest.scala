package org.tosunsi.sgoku

import java.util.Objects

import org.scalatest.{FlatSpecLike, Matchers}
import org.tosunsi.sgoku.TestSettings._
import org.tosunsi.sgoku.ValidatorTest._
import org.tosunsi.sgoku.pojo.Person

import scala.collection.immutable.Nil

/**
 * Contains the tests of [[Validator]]
 */
class ValidatorTest extends FlatSpecLike with Matchers {

  "GIVEN an object without error WHEN validate it THEN" should "no error in response" in {
    // Given.
    val person = Person(
      firstName = PERSON_FIRST_NAME,
      lastName = PERSON_LAST_NAME,
      age = PERSON_AGE
    )

    // When.
    val errorMessages: Seq[String] = Validator.of(person)
      .validate(_.firstName)(Objects.nonNull)(FIRST_NAME_NOT_NULL)
      .validate(_.firstName)(isNotEmpty)(FIRST_NAME_NOT_EMPTY)
      .validate(_.lastName)(Objects.nonNull)(LAST_NAME_NOT_NULL)
      .validate(_.lastName)(isNotEmpty)(LAST_NAME_NOT_EMPTY)
      .validate(_.age)(Objects.nonNull)(AGE_NOT_NULL)
      .toErrors

    // Then.
    errorMessages should be(Nil)
  }

  "GIVEN an object with errors WHEN validate it with 'getOrElseThrow' THEN" should "by default throw an IllegalArgumentException with expected messages" in {
    // Given.
    val person = getPersonWithErrorFields

    // When.
    val caught = intercept[IllegalArgumentException] {
      Validator.of(person)
        .validate(_.firstName)(Objects.nonNull)(FIRST_NAME_NOT_NULL)
        .validate(_.firstName)(isNotEmpty)(FIRST_NAME_NOT_EMPTY)
        .validate(_.lastName)(isNotEmpty)(LAST_NAME_NOT_EMPTY)
        .validate(_.age)(age => age > 0)(AGE_GREATER_THAN_ZERO)
        .getOrElseThrow
    }

    // Then.
    caught.getSuppressed shouldNot be(Nil)

    val errorMessagesResult: Seq[String] = caught
      .getSuppressed
      .map(_.getMessage)

    errorMessagesResult should contain(LAST_NAME_NOT_EMPTY)
    errorMessagesResult should contain(AGE_GREATER_THAN_ZERO)
  }

  "GIVEN an object with errors WHEN validate it with 'getOrElseThrow' and a specified exception THEN" should "throw the given exception with expected messages" in {
    // Given.
    val person = getPersonWithErrorFields

    // When.
    val caught = intercept[ValidatorException] {
      Validator.of(person)
        .validate(_.firstName)(Objects.nonNull)(FIRST_NAME_NOT_NULL)
        .validate(_.firstName)(isNotEmpty)(FIRST_NAME_NOT_EMPTY)
        .validate(_.lastName)(isNotEmpty)(LAST_NAME_NOT_EMPTY)
        .validate(_.age)(age => age > 0)(AGE_GREATER_THAN_ZERO)
        .getOrElseThrow(classOf[ValidatorException])
    }

    // Then.
    caught.getSuppressed shouldNot be(Nil)

    val errorMessagesResult: Seq[String] = caught
      .getSuppressed
      .map(_.getMessage)

    errorMessagesResult should contain(LAST_NAME_NOT_EMPTY)
    errorMessagesResult should contain(AGE_GREATER_THAN_ZERO)
  }

  "GIVEN an object with errors WHEN validate it with 'toEither' THEN" should "get the error messages in left" in {
    // Given.
    val person = getPersonWithErrorFields

    // When.
    val result: Either[List[String], Person] = Validator.of(person)
      .validate(_.firstName)(Objects.nonNull)(FIRST_NAME_NOT_NULL)
      .validate(_.firstName)(isNotEmpty)(FIRST_NAME_NOT_EMPTY)
      .validate(_.lastName)(isNotEmpty)(LAST_NAME_NOT_EMPTY)
      .validate(_.age)(age => age > 0)(AGE_GREATER_THAN_ZERO)
      .toEither

    // Then.
    val errorMessagesResult: List[String] = result.left.get
    errorMessagesResult should contain(LAST_NAME_NOT_EMPTY)
    errorMessagesResult should contain(AGE_GREATER_THAN_ZERO)
  }

  "GIVEN an object with errors WHEN validate it with 'toOption' THEN" should "get an Option with error messages" in {
    // Given.
    val person = getPersonWithErrorFields

    // When.
    val result: Option[List[String]] = Validator.of(person)
      .validate(_.firstName)(Objects.nonNull)(FIRST_NAME_NOT_NULL)
      .validate(_.firstName)(isNotEmpty)(FIRST_NAME_NOT_EMPTY)
      .validate(_.lastName)(isNotEmpty)(LAST_NAME_NOT_EMPTY)
      .validate(_.age)(age => age > 0)(AGE_GREATER_THAN_ZERO)
      .toErrorsOption

    // Then.
    val errorMessagesResult: List[String] = result.get
    errorMessagesResult should contain(LAST_NAME_NOT_EMPTY)
    errorMessagesResult should contain(AGE_GREATER_THAN_ZERO)
  }

  /**
   * Gets a [[Person]] object with error fields.
   */
  private def getPersonWithErrorFields: Person = {
    Person(
      firstName = PERSON_FIRST_NAME,
      lastName = "",
      age = 0
    )
  }

  private def isNotEmpty(string: String): Boolean = {
    Objects.nonNull(string) && !string.equals("")
  }
}

object ValidatorTest {
  val FIRST_NAME_NOT_NULL = "The first name should not be null"
  val FIRST_NAME_NOT_EMPTY = "The first name should not be empty"
  val LAST_NAME_NOT_NULL = "The last name should not be null"
  val LAST_NAME_NOT_EMPTY = "The last name should not be empty"
  val AGE_NOT_NULL = "The age should not be null"
  val AGE_GREATER_THAN_ZERO = "The age should be greater that 0"
}

