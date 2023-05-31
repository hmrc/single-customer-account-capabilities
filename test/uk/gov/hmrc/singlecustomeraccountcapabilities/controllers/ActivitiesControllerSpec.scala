/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.singlecustomeraccountcapabilities.controllers

import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.singlecustomeraccountcapabilities.models.CapabilityDetails
import uk.gov.hmrc.singlecustomeraccountcapabilities.service.CapabilityDetailsService

import java.time.LocalDate
import scala.concurrent.Future

class ActivitiesControllerSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val fakeRequest = FakeRequest("GET", "/")
  private val mockCapabilitiesService: CapabilityDetailsService = mock[CapabilityDetailsService]

  val modules: Seq[GuiceableModule] =
    Seq(
      inject.bind[CapabilityDetailsService].toInstance(mockCapabilitiesService)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false).
    overrides(modules: _*).build()

  private val controller = application.injector.instanceOf[ActivitiesController]

  override protected def beforeEach(): Unit = {
    reset(mockCapabilitiesService)
    super.beforeEach()
  }

  "GET /" must {
    "return 200" in {

      val capabilityDetails: Seq[CapabilityDetails] = Seq(
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2022, 5, 19),
          descriptionContent = "Desc-1",
          url = "url-1"),
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023, 4, 9),
          descriptionContent = "Desc-2",
          url = "url-2")
      )

      when(mockCapabilitiesService.retrieveCapabilitiesData(anyString())(any(), any())).thenReturn(Future.successful(capabilityDetails))

      val result = controller.getActivities ("valid-nino")(fakeRequest)

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.arr(
          Json.obj(
            "nino" -> Json.obj(
              "hasNino" -> true,
              "nino" -> "GG012345C"
            ),
            "date" -> "2022-05-19",
            "descriptionContent" -> "Desc-1",
            "url" -> "url-1"
          ),
          Json.obj(
            "nino" -> Json.obj(
              "hasNino" -> true,
              "nino" -> "GG012345C"
            ),
            "date" -> "2023-04-09",
            "descriptionContent" -> "Desc-2",
            "url" -> "url-2"
          )
        )
      }

    }

    "return Empty List When capabilities not found with the nino" in {

      when(mockCapabilitiesService.retrieveCapabilitiesData(anyString())(any(), any())).thenReturn(Future.successful(Seq.empty))

      val result = controller.getActivities("invalid-nino")(fakeRequest)

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.arr()
      }

    }
  }
}
