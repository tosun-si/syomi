import java.util.Objects

object Test {

  def main(args: Array[String]): Unit = {
    println("Test")

    val toto = "test"

    val res1 = toto match {
      case "g" => () => 10
      case "test" => () => 400
    }

    val res2 = toto match {
      case x if test(x) => "toto"
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

    Validator.of(personObject)
      .validate(_.firstName)(Objects.nonNull)("The fisrt name should not be null")
      .validate(_.lastName)(_.nonEmpty)("The last name should not be empty")
      .validate(_.firstName)(!"toto".equals(_))("The first name should different from toto")
      .validate(_.age)(_.equals(25))("The age should be equals to 25")
      .getOrElseThrow(classOf[IllegalStateException])
  }

  def test(value: String): Boolean = {
    value.equals("test2")
  }

  def test2(value: String): Boolean = {
    value.equals("test")
  }
}
