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

package uk.gov.hmrc.singlecustomeraccountcapabilities.connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.HttpClientSupport
import uk.gov.hmrc.singlecustomeraccountcapabilities.helper.WireMockHelper
import uk.gov.hmrc.singlecustomeraccountcapabilities.models.CapabilityDetails

import java.time.LocalDate


class CapabilitiesConnectorSpec extends AsyncWordSpec with Matchers with WireMockHelper with HttpClientSupport with MockitoSugar {

  import CapabilitiesConnectorSpec._

  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  override protected def portConfigKeys: String = "microservice.services.sca-capabilities.port"

  private lazy val capabilitiesConnector: CapabilitiesConnector = injector.instanceOf[CapabilitiesConnector]

  "getCapabilities" must {
    "return the Capabilities data with valid Nino" in {
      val capabilitiesResponseJson: JsArray = Json.arr(
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

      server.stubFor(
        get(urlEqualTo(capabilityDetailsUrl))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(capabilitiesResponseJson.toString())
          )
      )
      capabilitiesConnector.list(nino).map { response =>
        response mustBe capabilityDetails
      }
    }

    "return None with valid Nino" in {

      server.stubFor(
        get(urlEqualTo(capabilityDetailsUrl))
          .willReturn(
            notFound
          )
      )
      capabilitiesConnector.list(nino).map { response =>
        response mustBe Seq.empty
      }
    }
  }
}

object CapabilitiesConnectorSpec {
  private val nino = "test-nino"

  private val capabilityDetails: Seq[CapabilityDetails] = Seq(
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

  private val capabilityDetailsUrl = s"/individuals/details/NINO/$nino"
}