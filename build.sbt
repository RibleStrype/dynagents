name := "dynagents"

scalaVersion := "2.12.7"
scalacOptions ++= Seq(
  "-language:_",
  "-Ypartial-unification",
  "-Xfatal-warnings"
)

libraryDependencies ++= Seq(
  "com.github.mpilquist" %% "simulacrum"  % "0.12.0",
  "org.scalaz"           %% "scalaz-core" % "7.2.22",
  "com.propensive"       %% "contextual"  % "1.1.0",
  "eu.timepit"           %% "refined"     % "0.9.2",
  "org.scalatest"        %% "scalatest"   % "3.0.5"   % "test"
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
