# SGOKU

The goal of this api is to give some functional and util classes for Scala.

It can give Monad, Applicative or other composition class.

You can use this API directly with Sbt or Maven, by adding the following dependency.  

```
Maven

Sbt
```

## Validator

The goal of the validator is to validates the fields of a generic object.
Firstly we call a factory method in order to initialize the monad.
After that, the call of validate method allows to do a projection on a field of this object, and to apply a predicate on this.
A message is associated for the eventual error.

The api can returns all the error messages at end.

Example : 

```scala
val person = Person(
  firstName = "toto",
  lastName = "tata",
  age = 30
)

Validator.of(person)
  .validate(_.firstName)(Objects.nonNull)("The first name should not be null")
  .validate(_.firstName)(!"toto".equals(_))("The first name should different from toto")
  .validate(p => p.lastName)(_.nonEmpty)("The last name should not be empty")
  .validate(_.age)(_.equals(25))("The age should be equals to 25")
  .getOrElseThrow(classOf[IllegalStateException])
```

In this case, the validator validate the fields of an instance of the person object (Validator.of(person)).
Then each call to validate method, allows to validate a field of this object.

A function from the person, allows to do a projection on a field, for example 

```scala
person => person.firstName
```

or 

```scala
_.firstName
```

By inference, the API knows that the input of this function projection is the generic object (in this case person passed in the "of" method).

