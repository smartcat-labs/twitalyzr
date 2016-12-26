lazy val commons = Seq(
  name := "twicas",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.11.0"
)

lazy val twicas = project.in(file("."))
  .settings(commons: _*)
  .settings(
    libraryDependencies ++= Dependencies.algoDeps ++ Dependencies.commons
  )

assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "spark", "streaming", "twitter", _*) => MergeStrategy.deduplicate
  case PathList("org", "apache", "spark", _*)              => MergeStrategy.discard
  case PathList("org", "spark_project", _*)                => MergeStrategy.discard
  case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
  case "log4j.properties"                                  => MergeStrategy.deduplicate
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case "reference.conf"                                    => MergeStrategy.concat
  case _                                                   => MergeStrategy.first
}
    