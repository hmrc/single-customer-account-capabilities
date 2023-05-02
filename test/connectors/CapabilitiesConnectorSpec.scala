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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.HttpClientSupport
import uk.gov.hmrc.singlecustomeraccountcapabilities.connectors.CapabilitiesConnector
import uk.gov.hmrc.singlecustomeraccountcapabilities.models.IfCapabilityDetails
import utils.WireMockHelper


class CapabilitiesConnectorSpec extends AsyncWordSpec with Matchers with WireMockHelper with HttpClientSupport with MockitoSugar {

  import CapabilitiesConnectorSpec._

  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  override protected def portConfigKeys: String = "microservice.services.capabilitiesData.port"

  private lazy val capabilitiesConnector: CapabilitiesConnector = injector.instanceOf[CapabilitiesConnector]

  "getCapabilities" must {
    "return the Capabilities data with valid Nino" in {
      val capabilitiesResponseJson: JsObject = Json.obj(
        "nino" -> Json.obj(
          "hasNino" -> true,
          "nino" -> "GG012345C"
        ),
        "date" -> "9 April 2023",
        "descriptionContent" -> "Your tax code has changed",
        "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison"
      )

      server.stubFor(
        get(urlEqualTo(capabilityDetailsUrl))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(capabilitiesResponseJson.toString())
          )
      )
      capabilitiesConnector.find(nino).map { response =>
        response mustBe Some(capabilityDetails)
      }
    }

    "return None with valid Nino" in {

      server.stubFor(
        get(urlEqualTo(capabilityDetailsUrl))
          .willReturn(
            notFound
          )
      )
      capabilitiesConnector.find(nino).map { response =>
        response mustBe None
      }
    }
  }
}

object CapabilitiesConnectorSpec {
  private val nino = "test-nino"

  private val capabilityDetails = IfCapabilityDetails(
    nino = Nino(true, Some("GG012345C")),
    date = "9 April 2023",
    descriptionContent = "Your tax code has changed",
    url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison")

  private val capabilityDetailsUrl = s"/single-customer-account-stub/individuals/details/NINO/$nino"
}