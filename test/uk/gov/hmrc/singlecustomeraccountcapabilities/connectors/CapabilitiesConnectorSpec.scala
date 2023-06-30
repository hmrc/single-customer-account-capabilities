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
import uk.gov.hmrc.singlecustomeraccountcapabilities.models.{ActionDetails, CapabilityDetails}

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
          "url" -> "url-1",
          "activityHeading" -> "activityHeading-1"
        ),
        Json.obj(
          "nino" -> Json.obj(
            "hasNino" -> true,
            "nino" -> "GG012345C"
          ),
          "date" -> "2023-04-09",
          "descriptionContent" -> "Desc-2",
          "url" -> "url-2",
          "activityHeading" -> "activityHeading-2"
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

  "taxCalcList" must {
    "return the tax calc data with valid Nino" in {
      val taxCalcResponseJson: JsArray = Json.arr(
        Json.obj(
          "nino" -> Json.obj(
            "hasNino" -> true,
            "nino" -> "GG012345C"
          ),
          "date" -> "2023-04-01",
          "descriptionContent" -> "Your tax calculation for the 2022-2023 is now available",
          "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          "activityHeading" -> "Your tax calculation"
        ),
      )

      server.stubFor(
        get(urlEqualTo(taxCalcUrl))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(taxCalcResponseJson.toString())
          )
      )
      capabilitiesConnector.taxCalcList(nino).map { response =>
        response mustBe taxCalcDetails
      }
    }

    "return None with valid Nino" in {

      server.stubFor(
        get(urlEqualTo(taxCalcUrl))
          .willReturn(
            notFound
          )
      )
      capabilitiesConnector.taxCalcList(nino).map { response =>
        response mustBe Seq.empty
      }
    }
  }

  "taxCodeChange" must {
//    "return the tax code data with valid Nino" in {
//      val taxCodeChangeResponseJson: JsArray = Json.arr(
//        Json.obj(
//          "data" -> Json.obj(
//            "current" ->
//              Json.obj(
//                "taxCode" -> "830L",
//                "employerName" -> "Employer Name",
//                "operatedTaxCode" -> true,
//                "p2Issued" -> true,
//                "startDate" -> "2023-06-21",
//                "endDate" -> "2023-09-21",
//                "payrollNumber" -> "1",
//                "pensionIndicator" -> true,
//                "primary" -> true
//              )
//            ,
//            "previous" ->
//              Json.obj(
//                "taxCode" -> "1150L",
//                "employerName" -> "Employer Name",
//                "operatedTaxCode" -> true,
//                "p2Issued" -> true,
//                "startDate" -> "2022-06-21",
//                "endDate" -> "2022-09-21",
//                "payrollNumber" -> "1",
//                "pensionIndicator" -> true,
//                "primary" -> true
//              )
//          ),
//          "links" -> Json.arr()
//        ))
//
//      server.stubFor(
//        get(urlEqualTo(taxCodeChangeUrl))
//          .willReturn(
//            ok
//              .withHeader("Content-Type", "application/json")
//              .withBody(taxCodeChangeResponseJson.toString())
//          )
//      )
//      capabilitiesConnector.taxCodeList(nino).map { response =>
//        response mustBe taxCodeChangeDetails
//      }
//    }

    "return None with valid Nino" in {

      server.stubFor(
        get(urlEqualTo(taxCodeChangeUrl))
          .willReturn(
            notFound
          )
      )
      capabilitiesConnector.taxCodeChange(nino).map { response =>
        response mustBe null
      }
    }
  }

  "childBenefitList" must {
    "return the child benefit data with valid Nino" in {
      val childBenefitResponseJson: JsArray = Json.arr(
        Json.obj(
          "nino" -> Json.obj(
            "hasNino" -> true,
            "nino" -> "GG012345C"
          ),
          "date" -> "2023-05-05",
          "descriptionContent" -> "HMRC paid you Child Benefit",
          "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          "activityHeading" -> "Recent Child Benefit payments"
        ),
        Json.obj(
          "nino" -> Json.obj(
            "hasNino" -> true,
            "nino" -> "GG012345C"
          ),
          "date" -> "2023-04-06",
          "descriptionContent" -> "HMRC paid you Child Benefit",
          "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          "activityHeading" -> "Recent Child Benefit payments"
        ),
        Json.obj(
          "nino" -> Json.obj(
            "hasNino" -> true,
            "nino" -> "GG012345C"
          ),
          "date" -> "2023-03-07",
          "descriptionContent" -> "HMRC paid you Child Benefit",
          "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          "activityHeading" -> "Recent Child Benefit payments"
        ),
        Json.obj(
          "nino" -> Json.obj(
            "hasNino" -> true,
            "nino" -> "GG012345C"
          ),
          "date" -> "2023-04-05",
          "descriptionContent" -> "HMRC paid you Child Benefit",
          "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          "activityHeading" -> "Recent Child Benefit payments"
        ),
        Json.obj(
          "nino" -> Json.obj(
            "hasNino" -> true,
            "nino" -> "GG012345C"
          ),
          "date" -> "2023-04-06",
          "descriptionContent" -> "HMRC paid you Child Benefit",
          "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          "activityHeading" -> "Recent Child Benefit payments"
        )
      )

      server.stubFor(
        get(urlEqualTo(childBenefitUrl))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(childBenefitResponseJson.toString())
          )
      )
      capabilitiesConnector.childBenefitList(nino).map { response =>
        response mustBe childBenefitDetails
      }
    }

    "return None with valid Nino" in {

      server.stubFor(
        get(urlEqualTo(childBenefitUrl))
          .willReturn(
            notFound
          )
      )
      capabilitiesConnector.childBenefitList(nino).map { response =>
        response mustBe Seq.empty
      }
    }
  }

  "payeIncomeList" must {
    "return the paye income data with valid Nino" in {
      val payeIncomeResponseJson: JsArray = Json.arr(
        Json.obj(
          "nino" -> Json.obj(
            "hasNino" -> true,
            "nino" -> "GG012345C"
          ),
          "date" -> "2023-04-05",
          "descriptionContent" -> "Central Perk Coffee Ltd paid you PAYE income",
          "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          "activityHeading" -> "Your PAYE income for the current tax year"
        ),
      )

      server.stubFor(
        get(urlEqualTo(payeIncomeUrl))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(payeIncomeResponseJson.toString())
          )
      )
      capabilitiesConnector.payeIncomeList(nino).map { response =>
        response mustBe payeIncomeDetails
      }
    }

    "return None with valid Nino" in {

      server.stubFor(
        get(urlEqualTo(payeIncomeUrl))
          .willReturn(
            notFound
          )
      )
      capabilitiesConnector.payeIncomeList(nino).map { response =>
        response mustBe Seq.empty
      }
    }
  }

  "actionTaxCalcList" must {
    "return tax overpayment notice data with Nino as GG012345C" in {
      val overpaymentTaxCalcResponseJson: JsArray = Json.arr(
        Json.obj(
          "nino" -> Json.obj(
            "hasNino" -> true,
            "nino" -> "GG012345C"
          ),
          "date" -> "2023-01-10",
          "descriptionContent" -> "You paid too much tax in the 2022 to 2023 tax year. HMRC owes you a £84.23 refund",
          "actionDescription" -> "Claim your tax refund",
          "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          "activityHeading" -> "Things for you to do"
        ),
      )

      server.stubFor(
        get(urlEqualTo(actionTaxCalcUrl))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(overpaymentTaxCalcResponseJson.toString())
          )
      )
      capabilitiesConnector.actionTaxCalcList(nino).map { response =>
        response mustBe overPayment
      }
    }

    "return tax underpayment notice data with Nino as AA999999A" in {
      val underpaymentTaxCalcResponseJson: JsArray = Json.arr(
        Json.obj(
          "nino" -> Json.obj(
            "hasNino" -> true,
            "nino" -> "AA999999A"
          ),
          "date" -> "2023-01-10",
          "descriptionContent" -> "You did not pay enough tax in the 2022 to 2023 tax year. You must pay HMRC by 31 January 2023.",
          "actionDescription" -> "Make a tax payment",
          "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          "activityHeading" -> "Things for you to do"
        ),
      )

      server.stubFor(
        get(urlEqualTo(actionTaxCalcUrl))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(underpaymentTaxCalcResponseJson.toString())
          )
      )
      capabilitiesConnector.actionTaxCalcList(nino).map { response =>
        response mustBe underPayment
      }
    }

    "return None with valid Nino" in {

      server.stubFor(
        get(urlEqualTo(actionTaxCalcUrl))
          .willReturn(
            notFound
          )
      )
      capabilitiesConnector.taxCalcList(nino).map { response =>
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
      url = "url-1",
      activityHeading = "activityHeading-1"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 4, 9),
      descriptionContent = "Desc-2",
      url = "url-2",
      activityHeading = "activityHeading-2")
  )

  private val taxCalcDetails: Seq[CapabilityDetails] = Seq(
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 4, 1),
      descriptionContent = "Your tax calculation for the 2022-2023 is now available",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Your tax calculation")

  )

//  private val taxCodeChangeDetails: Seq[TaxCodeChangeObject] = Seq(
//    TaxCodeChangeObject(
//      data = TaxCodeChangeData(
//        current = TaxCodeChangeDetails(
//          taxCode = "830L", employerName = "Employer Name", operatedTaxCode = true, p2Issued = true, startDate = "2023-06-21", endDate = "2023-09-21", payrollNumber = "1", pensionIndicator = true, primary = true
//        ),
//        previous = TaxCodeChangeDetails(
//          taxCode = "1150L", employerName = "Employer Name", operatedTaxCode = true, p2Issued = true, startDate = "2022-06-21", endDate = "2022-09-21", payrollNumber = "1", pensionIndicator = true, primary = true
//        )
//      ),
//      links = Array.empty[String]
//    )
//  )

  private val childBenefitDetails: Seq[CapabilityDetails] = Seq(
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023,5,5),
      descriptionContent = "HMRC paid you Child Benefit",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Recent Child Benefit payments"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023,4,6),
      descriptionContent = "HMRC paid you Child Benefit",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Recent Child Benefit payments"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023,3,7),
      descriptionContent = "HMRC paid you Child Benefit",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Recent Child Benefit payments"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023,4,5),
      descriptionContent = "HMRC paid you Child Benefit",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Recent Child Benefit payments"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023,4,6),
      descriptionContent = "HMRC paid you Child Benefit",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Recent Child Benefit payments")
  )

  private val payeIncomeDetails: Seq[CapabilityDetails] = Seq(
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023,4,5),
      descriptionContent = "Central Perk Coffee Ltd paid you PAYE income",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Your PAYE income for the current tax year")
  )

  private val overPayment = Seq(
    ActionDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023,1,10),
      descriptionContent = "You paid too much tax in the 2022 to 2023 tax year. HMRC owes you a £84.23 refund",
      actionDescription = "Claim your tax refund",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Things for you to do")
  )

  private val underPayment = Seq(
    ActionDetails(
      nino = Nino(true, Some("AA999999A")),
      date = LocalDate.of(2023,1,10),
      descriptionContent = "You did not pay enough tax in the 2022 to 2023 tax year. You must pay HMRC by 31 January 2023.",
      actionDescription = "Make a tax payment",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Things for you to do")
  )

  private val capabilityDetailsUrl = s"/individuals/details/NINO/$nino"
  private val taxCalcUrl = s"/individuals/activities/tax-calc/NINO/$nino"
  private val taxCodeChangeUrl = s"/individuals/activities/tax-code-change/NINO/$nino"
  private val childBenefitUrl = s"/individuals/activities/child-benefit/NINO/$nino"
  private val payeIncomeUrl = s"/individuals/activities/paye-income/NINO/$nino"
  private val actionTaxCalcUrl = s"/individuals/actions/tax-calc/NINO/$nino"

}