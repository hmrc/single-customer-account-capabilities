import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

val appName: String = "single-customer-account-capabilities"

lazy val microservice = Project(appName, file("."))
  .disablePlugins(JUnitXmlReportPlugin)
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    name := appName,
    PlayKeys.playDefaultPort := 8423,
    majorVersion        := 0,
    scalaVersion        := "2.13.8",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions ++= Seq("-feature", "-Xfatal-warnings", "-deprecation"),
    scalacOptions += "-Wconf:src=routes/.*:s",
    retrieveManaged := true,
    resolvers ++= Seq(
      Resolver.jcenterRepo
    )
  )
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)