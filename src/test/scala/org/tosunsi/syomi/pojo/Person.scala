package org.tosunsi.syomi.pojo

case class Person(
    firstName: String = "",
    lastName: String = "",
    age: Int = 0,
    address: Address = Address()
)
