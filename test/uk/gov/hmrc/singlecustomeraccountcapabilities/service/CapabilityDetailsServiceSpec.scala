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

package uk.gov.hmrc.singlecustomeraccountcapabilities.service

import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.{Application, inject}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.singlecustomeraccountcapabilities.connectors.CapabilitiesConnector
import uk.gov.hmrc.singlecustomeraccountcapabilities.models.CapabilityDetails

import java.time.LocalDate
import scala.concurrent.Future

class CapabilityDetailsServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  import CapabilityDetailsServiceSpec._

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val mockCapabilitiesConnector: CapabilitiesConnector = mock[CapabilitiesConnector]
  private val mockCapabilityDetailsRules: CapabilityDetailsRules = mock[CapabilityDetailsRules]

  val modules: Seq[GuiceableModule] =
    Seq(
      inject.bind[CapabilitiesConnector].toInstance(mockCapabilitiesConnector),
      inject.bind[CapabilityDetailsRules].toInstance(mockCapabilityDetailsRules)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false).
    overrides(modules: _*).build()

  private val capabilityDetailsService = application.injector.instanceOf[CapabilityDetailsService]

  override protected def beforeEach(): Unit = {
    reset(mockCapabilitiesConnector)
    reset(mockCapabilityDetailsRules)
    super.beforeEach()
  }

  "retrieveCapabilitiesData" must {
    "return empty Seq if capabilitiesConnector returns empty Seq" in {

      when(mockCapabilitiesConnector.list(anyString())(any(), any())).thenReturn(Future.successful(Seq.empty))

      capabilityDetailsService.retrieveCapabilitiesData("nino-for-empty-list").map { result =>
        verify(mockCapabilitiesConnector, times(1)).list(anyString())(any(), any())
        verify(mockCapabilityDetailsRules, never()).withinTaxYear(any())
        verify(mockCapabilityDetailsRules, never()).withinSixMonth(any())
        result mustBe Seq.empty
      }

    }

    "return empty Seq if capabilityDetails does not pass the rules" in {

      when(mockCapabilitiesConnector.list(anyString())(any(), any())).thenReturn(Future.successful(unOrderedCapabilityDetails))
      when(mockCapabilityDetailsRules.withinTaxYear(any())).thenReturn(false)
      when(mockCapabilityDetailsRules.withinSixMonth(any())).thenReturn(false)

      capabilityDetailsService.retrieveCapabilitiesData("nino-for-empty-list").map { result =>
        verify(mockCapabilitiesConnector, times(1)).list(anyString())(any(), any())
        verify(mockCapabilityDetailsRules, times(unOrderedCapabilityDetails.size)).withinTaxYear(any())
        verify(mockCapabilityDetailsRules, times(unOrderedCapabilityDetails.size)).withinSixMonth(any())
        result mustBe Seq.empty
      }
    }

    "return ordered Seq of capabilityDetails if retrieved unorderedCapabilityDetails withinTaxYear" in {

      when(mockCapabilitiesConnector.list(anyString())(any(), any())).thenReturn(Future.successful(unOrderedCapabilityDetails))
      when(mockCapabilityDetailsRules.withinTaxYear(any())).thenReturn(true)

      capabilityDetailsService.retrieveCapabilitiesData("nino-for-empty-list").map { result =>
        verify(mockCapabilitiesConnector, times(1)).list(anyString())(any(), any())
        verify(mockCapabilityDetailsRules, times(unOrderedCapabilityDetails.size)).withinTaxYear(any())
        verify(mockCapabilityDetailsRules, never()).withinSixMonth(any())
        result mustBe orderedByDateCapabilityDetails
      }
    }

    "return ordered Seq of capabilityDetails if retrieved unorderedCapabilityDetails withinSixMonth" in {

      when(mockCapabilitiesConnector.list(anyString())(any(), any())).thenReturn(Future.successful(unOrderedCapabilityDetails))
      when(mockCapabilityDetailsRules.withinTaxYear(any())).thenReturn(false)
      when(mockCapabilityDetailsRules.withinSixMonth(any())).thenReturn(true)

      capabilityDetailsService.retrieveCapabilitiesData("nino-for-empty-list").map { result =>
        verify(mockCapabilitiesConnector, times(1)).list(anyString())(any(), any())
        verify(mockCapabilityDetailsRules, times(unOrderedCapabilityDetails.size)).withinTaxYear(any())
        verify(mockCapabilityDetailsRules, times(unOrderedCapabilityDetails.size)).withinSixMonth(any())
        result mustBe orderedByDateCapabilityDetails
      }
    }

    "return ordered Seq of capabilityDetails if retrieved unorderedCapabilityDetails both withinTaxYear and withinSixMonth" in {

      when(mockCapabilitiesConnector.list(anyString())(any(), any())).thenReturn(Future.successful(unOrderedCapabilityDetails))
      when(mockCapabilityDetailsRules.withinTaxYear(any())).thenReturn(true)
      when(mockCapabilityDetailsRules.withinSixMonth(any())).thenReturn(true)

      capabilityDetailsService.retrieveCapabilitiesData("nino-for-empty-list").map { result =>
        verify(mockCapabilitiesConnector, times(1)).list(anyString())(any(), any())
        verify(mockCapabilityDetailsRules, times(unOrderedCapabilityDetails.size)).withinTaxYear(any())
        verify(mockCapabilityDetailsRules, never()).withinSixMonth(any())
        result mustBe orderedByDateCapabilityDetails
      }
    }
  }
}

object CapabilityDetailsServiceSpec {
  val unOrderedCapabilityDetails: Seq[CapabilityDetails] = Seq(
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2022, 5, 19),
      descriptionContent = "Desc-1",
      url = "url-1"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 4, 9),
      descriptionContent = "Desc-2",
      url = "url-2"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 1, 19),
      descriptionContent = "Desc-3",
      url = "url-3")
  )

  val orderedByDateCapabilityDetails: Seq[CapabilityDetails] = Seq(
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 4, 9),
      descriptionContent = "Desc-2",
      url = "url-2"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 1, 19),
      descriptionContent = "Desc-3",
      url = "url-3"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2022, 5, 19),
      descriptionContent = "Desc-1",
      url = "url-1")
  )
}