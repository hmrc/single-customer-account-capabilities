import sbt._

object AppDependencies {

  private val bootstrapVersion = "7.15.0"


  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"        % bootstrapVersion,
    "uk.gov.hmrc"             %% "tax-year"                         % "3.2.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-test-play-28"     % bootstrapVersion,
    "com.vladsch.flexmark"          %  "flexmark-all"               % "0.62.2",
    "org.scalatestplus"             %% "mockito-4-6"                % "3.2.15.0",
    "com.github.tomakehurst"        %  "wiremock-jre8"              % "2.35.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"       % "2.14.2"
  ).map(_ % "test,it")
}
