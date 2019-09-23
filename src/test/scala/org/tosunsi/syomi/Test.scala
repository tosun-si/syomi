package org.tosunsi.syomi

import java.util.Objects

import org.tosunsi.syomi.pojo.Person

object Test {

  def main(args: Array[String]): Unit = {
    println("Test")

    val toto = "test"

    val res1 = toto match {
      case "g"    => () => 10
      case "test" => () => 400
    }

    val res2 = toto match {
      case x if test(x)  => "toto"
      case x if test2(x) => "tata"
    }

    Option(res2)
      .filter(r => r.isEmpty)
      .filter(r => !r.equals("toti"))
      .map(_ => 5)
      .getOrElse(0)

    println(s"Res1 : ${res1.apply()}")
    println(s"Res2 : $res2")

    val personObject = Person(
      firstName = "toto",
      lastName = "tata",
      age = 30
    )

    val personObject2 = Person(
      firstName = "toto",
      lastName = "tata",
      age = 30
    )

    Validator.of(personObject)
      .validate(_.firstName)(Objects.nonNull)("The first name should not be null")
      .validate(_.lastName)(_.nonEmpty)("The last name should not be empty")
      .validate(_.firstName)(!"toto".equals(_))("The first name should different from toto")
      .validate(_.age)(_.equals(25))("The age should be equals to 25")
      .thenTo(_.address)
      .validate(_.city)(city => test3(city))("")
      .getOrElseThrow(classOf[IllegalStateException])

    val testEx = new ValidatorException

    Validator.of(personObject)
      .validate(_.firstName)(Objects.nonNull)("The first name should not be null")
      .validate(_.lastName)(_.nonEmpty)("The last name should not be empty")
      .validate(_.firstName)(!"toto".equals(_))("The first name should different from toto")
      .validate(_.age)(_.equals(25))("The age should be equals to 25")
      .getOrElseThrow(new ValidatorException)

    // Either.
    val result: Either[Seq[String], Person] = Validator.of(personObject)
      .validate(_.firstName)(Objects.nonNull)("The first name should not be null")
      .validate(_.lastName)(_.nonEmpty)("The last name should not be empty")
      .validate(_.firstName)(!"toto".equals(_))("The first name should different from toto")
      .validate(_.age)(_.equals(25))("The age should be equals to 25")
      .toEither

    result match {
      case Right(obj)   => println(obj)
      case Left(errors) => throw new IllegalArgumentException(s"Errors : $errors")
    }


    val persons = List(personObject, personObject2)

    ValidatorList.of(persons)
      .validate(_.firstName)(Objects.nonNull)("")
      .validate(_.lastName)(Objects.nonNull)("")
      .validate(_.age)(Objects.nonNull)("")
      .toErrors

    val tuple: Option[(Int, Int)] = for {
      number1 <- Option(2)
      number2 <- Option(5)
    } yield (number1, number2)
  }

  def test(value: String): Boolean = {
    value.equals("test2")
  }

  def test2(value: String): Boolean = {
    value.equals("test")
  }

  private def test3(toto: String): Boolean = {
    toto.nonEmpty
  }
}
