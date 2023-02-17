
lazy val root = (project in file(".")).
  settings(
    name := "com.luxoft.sensor.stx",
    version := "1.0",
    scalaVersion := "2.13.10",
    mainClass in Compile := Some("com.fp.org.parallel.StatisticsAnalyze")
  )

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.4.0",
  "org.typelevel" %% "cats-effect" % "2.4.0",
  "org.scala-lang.modules" %% "scala-async" % "1.0.1"
)

// Your username to login to Databricks Cloud
val dbcUsername = System.getenv("USERNAME")

// Your password (Can be set as an environment variable)
val dbcPassword = System.getenv("DBC_PASSWORD")

// The URL to the Databricks Cloud DB Api.7.3
val dbcApiUrl = System.getenv("DBC_URL") + "/api/7.3"

// Add any clusters that you would like to deploy your work to. e.g. "My Cluster or run dbcExecuteCommand Add "ALL_CLUSTERS" if you want to attach your work to all clusters
val dbcClusters = System.getenv("DBC_CLUSTER")

// The location to upload your libraries to in the workspace e.g. "/Users/alice"
val dbcLibraryPath = "/Users/" + System.getenv("DBC_USERNAME") + "/lib"

//required for spart-testing-base
val parallelExecution = false

val publishTo = Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))


