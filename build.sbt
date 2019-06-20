name := "syomi"

version := "0.1.0"

scalaVersion := "2.12.8"

lazy val testDependencies: Seq[ModuleID] = Seq(
  "org.scalatest" %% "scalatest" % "3.0.7",
).map(_ % Test)

libraryDependencies ++= testDependencies
