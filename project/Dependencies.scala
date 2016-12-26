import sbt._

object Dependencies {
  lazy val sparkVersion = "2.0.2"

  val commons = Seq(
    "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
    "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.0-M3",
    "com.typesafe" % "config" % "1.3.1",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "log4j" % "log4j" % "1.2.17" % Provided,
    "org.apache.bahir" % "spark-streaming-twitter_2.11" % "2.0.1"
  )

  val algoDeps = commons ++ Seq(
    "org.apache.spark" %% "spark-mllib" % sparkVersion % Provided,
    "org.apache.spark" %% "spark-sql" % sparkVersion % Provided
  )
}
