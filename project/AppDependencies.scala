import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val bootstrapVersion = "7.15.0"
  

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % bootstrapVersion
  )

  val test = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-test-play-28"     % bootstrapVersion            % "test, it",
    "org.scalatestplus"             %% "mockito-4-6"                % "3.2.15.0"                  % Test,
    "com.github.tomakehurst"        %  "wiremock-jre8"              % "2.35.0"                    % Test,
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"       % "2.14.2"                    % Test
  )
}
