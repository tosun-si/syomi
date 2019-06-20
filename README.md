# SYOMI

The goal of this api is to give some functional and util classes for Scala.

It can give Monad, Applicative or other composition class.

You can use this API directly with Sbt or Maven, by adding the following dependency.  

```
Maven

Sbt
```

## Validator

### 1 General presentation of the API

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

Then we can apply a predicate on the field (via the previous projection) : 

```scala
Objects.nonNull
``` 

```scala
firstName => Objects.nonNull(firstName)
``` 

```scala
firstName => firstName != null
```

The third parameter is the message for the error case.

```scala
"The first name should not be null"
```

The api stack all the messages that correspond to error cases.

If the given object (in of) is null, then all the "validate" cases are considered as errors.

### 2 Jump to a nested object

The API allows to jump to a nested object, via the "thenTo" method.
For example, if a Person object has a nested address, we can orient the validator to this : 

```scala
val address = Address(
  street = "44 street",
  zipCode = "75015",
  city = "Paris"
)

val person = Person(
  firstName = "toto",
  lastName = "tata",
  age = 30,
  address = address
)

Validator.of(person)
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

```   

