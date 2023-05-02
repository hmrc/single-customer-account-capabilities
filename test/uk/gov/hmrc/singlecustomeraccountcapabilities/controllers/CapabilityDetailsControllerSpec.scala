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
import org.scalatest.BeforeAndAfter
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
import uk.gov.hmrc.singlecustomeraccountcapabilities.connectors.CapabilitiesConnector
import uk.gov.hmrc.singlecustomeraccountcapabilities.models.IfCapabilityDetails

import scala.concurrent.Future

class CapabilityDetailsControllerSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfter {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val fakeRequest = FakeRequest("GET", "/")
  private val mockCapabilitiesConnector: CapabilitiesConnector = mock[CapabilitiesConnector]

  val modules: Seq[GuiceableModule] =
    Seq(
      inject.bind[CapabilitiesConnector].toInstance(mockCapabilitiesConnector)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false).
    overrides(modules: _*).build()

  private val controller = application.injector.instanceOf[CapabilityDetailsController]

  before {
    reset(mockCapabilitiesConnector)
  }

  "GET /" must {
    "return 200" in {

      val capabilityDetails = IfCapabilityDetails(
        nino = Nino(true, Some("GG012345C")),
        date = "9 April 2023",
        descriptionContent = "Your tax code has changed",
        url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison")

      when(mockCapabilitiesConnector.find(anyString())(any())).thenReturn(Future.successful(Some(capabilityDetails)))

      val result = controller.getCapabilitiesData("valid-nino")(fakeRequest)

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.obj(
          "nino" -> Json.obj(
            "hasNino" -> true,
            "nino" -> "GG012345C"
          ),
          "date" -> "9 April 2023",
          "descriptionContent" -> "Your tax code has changed",
          "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison"
        )
      }

    }

    "return Not Found When nino is not valid" in {

      when(mockCapabilitiesConnector.find(anyString())(any())).thenReturn(Future.successful(None))

      val result = controller.getCapabilitiesData("invalid-nino")(fakeRequest)

      whenReady(result) { _ =>
        status(result) mustBe NOT_FOUND
      }

    }
  }
}
