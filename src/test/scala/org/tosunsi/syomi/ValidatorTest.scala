package org.tosunsi.syomi

import java.util.Objects

import org.scalatest.{FlatSpecLike, Matchers}
import org.tosunsi.syomi.TestSettings._
import org.tosunsi.syomi.ValidatorTest._
import org.tosunsi.syomi.pojo.{Address, Person}

import scala.collection.immutable.Nil

/**
 * Contains the tests of [[Validator]] class.
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

  "GIVEN an object with errors WHEN validate it with 'toErrorsOption' THEN" should "get an Option with error messages" in {
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
    result shouldNot be(None)
    val errorMessagesResult: List[String] = result.get
    errorMessagesResult should contain(LAST_NAME_NOT_EMPTY)
    errorMessagesResult should contain(AGE_GREATER_THAN_ZERO)
  }

  "GIVEN an object with errors in nested object, via 'thenTo' WHEN validate it with 'toErrors' THEN" should "get the expected error messages" in {
    // Given.
    val person = getPersonWithErrorFieldsInAddress

    // When.
    val errorMessagesResult: List[String] = Validator.of(person)
      .validate(_.firstName)(Objects.nonNull)(FIRST_NAME_NOT_NULL)
      .validate(_.firstName)(isNotEmpty)(FIRST_NAME_NOT_EMPTY)
      .validate(_.lastName)(isNotEmpty)(LAST_NAME_NOT_EMPTY)
      .validate(_.age)(age => age > 0)(AGE_GREATER_THAN_ZERO)
      .thenTo(_.address)
      .validate(_.street)(Objects.nonNull)(STREET_NOT_NULL)
      .validate(_.street)(isNotEmpty)(STREET_NOT_EMPTY)
      .validate(_.zipCode)(Objects.nonNull)(ZIP_CODE_NOT_NULL)
      .validate(_.zipCode)(isNotEmpty)(ZIP_CODE_NOT_EMPTY)
      .validate(_.city)(Objects.nonNull)(CITY_CODE_NOT_NULL)
      .validate(_.city)(isNotEmpty)(CITY_CODE_NOT_EMPTY)
      .toErrors

    // Then.
    errorMessagesResult shouldNot contain(STREET_NOT_NULL)
    errorMessagesResult shouldNot contain(STREET_NOT_EMPTY)

    errorMessagesResult shouldNot contain(ZIP_CODE_NOT_NULL)
    errorMessagesResult should contain(ZIP_CODE_NOT_EMPTY)
    errorMessagesResult shouldNot contain(CITY_CODE_NOT_NULL)
    errorMessagesResult should contain(CITY_CODE_NOT_EMPTY)
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

  /**
   * Gets a [[Person]] object with error fields in the nested [[Address]] object.
   */
  private def getPersonWithErrorFieldsInAddress: Person = {
    val address = Address(
      street = ADDRESS_STREET,
      zipCode = "",
      city = ""
    )

    Person(
      firstName = PERSON_FIRST_NAME,
      lastName = PERSON_LAST_NAME,
      age = PERSON_AGE,
      address = address
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

  val STREET_NOT_NULL = "The street should not be null"
  val STREET_NOT_EMPTY = "The street should not be empty"
  val ZIP_CODE_NOT_NULL = "The zip code should not be null"
  val ZIP_CODE_NOT_EMPTY = "The zip code should not be empty"
  val CITY_CODE_NOT_NULL = "The city should not be null"
  val CITY_CODE_NOT_EMPTY = "The city should not be empty"
}
