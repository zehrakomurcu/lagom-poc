organization in ThisBuild := "com.movingimage"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"

lazy val `challenge` = (project in file("."))
  .aggregate(`challenge-api`, `challenge-impl`)

lazy val `challenge-api` = (project in file("challenge-api"))
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )

lazy val `challenge-impl` = (project in file("challenge-impl"))
  .enablePlugins(LagomJava)
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslKafkaBroker,
      lagomLogback,
      lagomJavadslTestKit,
      lombok
    )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`challenge-api`)

val lombok = "org.projectlombok" % "lombok" % "1.16.16"

def common = Seq(
  javacOptions in Compile += "-parameters"
)
