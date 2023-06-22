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
import uk.gov.hmrc.singlecustomeraccountcapabilities.models.{ActionDetails, Actions, Activities, CapabilityDetails, TaxCodeChangeData, TaxCodeChangeDetails, TaxCodeChangeObject}

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

  "retrieveAllActivitiesData" must {

    "return empty Seq if capabilitiesConnector returns empty Seq" in {

      when(mockCapabilitiesConnector.taxCalcList(anyString())(any(), any())).thenReturn(Future.successful(Seq.empty))
      when(mockCapabilitiesConnector.taxCodeList(anyString())(any(), any())).thenReturn(Future.successful(Seq.empty))
      when(mockCapabilitiesConnector.childBenefitList(anyString())(any(), any())).thenReturn(Future.successful(Seq.empty))
      when(mockCapabilitiesConnector.payeIncomeList(anyString())(any(), any())).thenReturn(Future.successful(Seq.empty))

      capabilityDetailsService.retrieveAllActivitiesData("nino-for-empty-list").map { result =>
        verify(mockCapabilitiesConnector, times(1)).taxCalcList(anyString())(any(), any())
        verify(mockCapabilitiesConnector, times(1)).taxCodeList(anyString())(any(), any())
        verify(mockCapabilitiesConnector, times(1)).childBenefitList(anyString())(any(), any())
        verify(mockCapabilitiesConnector, times(1)).payeIncomeList(anyString())(any(), any())

        verify(mockCapabilityDetailsRules, never()).withinTaxYear(any())
        verify(mockCapabilityDetailsRules, never()).withinSixMonth(any())

        result mustBe Activities(Seq.empty, Seq.empty, Seq.empty, Seq.empty)
      }
    }

    "return empty Seq if capabilityDetails does not pass the rules" in {

      when(mockCapabilitiesConnector.taxCalcList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedTaxCalcDetails))
      when(mockCapabilitiesConnector.taxCodeList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedTaxCodeChangeDetails))
      when(mockCapabilitiesConnector.childBenefitList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedChildBenefitDetails))
      when(mockCapabilitiesConnector.payeIncomeList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedPayeIncomeDetails))
      when(mockCapabilityDetailsRules.withinTaxYear(any())).thenReturn(false)
      when(mockCapabilityDetailsRules.withinSixMonth(any())).thenReturn(false)

      capabilityDetailsService.retrieveAllActivitiesData("nino-for-empty-list").map { result =>
        verify(mockCapabilitiesConnector, times(1)).taxCalcList(anyString())(any(), any())
        verify(mockCapabilitiesConnector, times(1)).taxCodeList(anyString())(any(), any())
        verify(mockCapabilitiesConnector, times(1)).childBenefitList(anyString())(any(), any())
        verify(mockCapabilitiesConnector, times(1)).payeIncomeList(anyString())(any(), any())

        verify(mockCapabilityDetailsRules, times(7)).withinTaxYear(any())
        verify(mockCapabilityDetailsRules, times(7)).withinSixMonth(any())

        result mustBe Activities(Seq.empty, Seq.empty, Seq.empty, Seq.empty)
      }
    }

    "return ordered Seq of capabilityDetails if retrieved unorderedCapabilityDetails withinTaxYear" in {

      when(mockCapabilitiesConnector.taxCalcList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedTaxCalcDetails))
      when(mockCapabilitiesConnector.taxCodeList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedTaxCodeChangeDetails))
      when(mockCapabilitiesConnector.childBenefitList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedChildBenefitDetails))
      when(mockCapabilitiesConnector.payeIncomeList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedPayeIncomeDetails))
      when(mockCapabilityDetailsRules.withinTaxYear(any())).thenReturn(true)

      capabilityDetailsService.retrieveAllActivitiesData("nino-for-empty-list").map { result =>
        verify(mockCapabilitiesConnector, times(1)).taxCalcList(anyString())(any(), any())
        verify(mockCapabilitiesConnector, times(1)).taxCodeList(anyString())(any(), any())
        verify(mockCapabilitiesConnector, times(1)).childBenefitList(anyString())(any(), any())
        verify(mockCapabilitiesConnector, times(1)).payeIncomeList(anyString())(any(), any())

        verify(mockCapabilityDetailsRules, times(7)).withinTaxYear(any())
        verify(mockCapabilityDetailsRules, never()).withinSixMonth(any())

        result mustBe orderedActivities
      }
    }

    "return ordered list of Activities if retrieved unorderedCapabilityDetails withinSixMonth" in {

      when(mockCapabilitiesConnector.taxCalcList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedTaxCalcDetails))
      when(mockCapabilitiesConnector.taxCodeList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedTaxCodeChangeDetails))
      when(mockCapabilitiesConnector.childBenefitList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedChildBenefitDetails))
      when(mockCapabilitiesConnector.payeIncomeList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedPayeIncomeDetails))
      when(mockCapabilityDetailsRules.withinTaxYear(any())).thenReturn(false)
      when(mockCapabilityDetailsRules.withinSixMonth(any())).thenReturn(true)

      capabilityDetailsService.retrieveAllActivitiesData("nino-for-empty-list").map { result =>
        verify(mockCapabilitiesConnector, times(1)).taxCalcList(anyString())(any(), any())
        verify(mockCapabilitiesConnector, times(1)).taxCodeList(anyString())(any(), any())
        verify(mockCapabilitiesConnector, times(1)).childBenefitList(anyString())(any(), any())
        verify(mockCapabilitiesConnector, times(1)).payeIncomeList(anyString())(any(), any())

        verify(mockCapabilityDetailsRules, times(7)).withinTaxYear(any())
        verify(mockCapabilityDetailsRules, times(7)).withinSixMonth(any())

        result mustBe orderedActivities
      }
    }

    "return ordered list of Activities if retrieved unorderedCapabilityDetails both withinTaxYear and withinSixMonth" in {

      when(mockCapabilitiesConnector.taxCalcList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedTaxCalcDetails))
      when(mockCapabilitiesConnector.taxCodeList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedTaxCodeChangeDetails))
      when(mockCapabilitiesConnector.childBenefitList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedChildBenefitDetails))
      when(mockCapabilitiesConnector.payeIncomeList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedPayeIncomeDetails))
      when(mockCapabilityDetailsRules.withinTaxYear(any())).thenReturn(true)
      when(mockCapabilityDetailsRules.withinSixMonth(any())).thenReturn(true)

      capabilityDetailsService.retrieveAllActivitiesData("nino-for-empty-list").map { result =>
        verify(mockCapabilitiesConnector, times(1)).taxCalcList(anyString())(any(), any())
        verify(mockCapabilitiesConnector, times(1)).taxCodeList(anyString())(any(), any())
        verify(mockCapabilitiesConnector, times(1)).childBenefitList(anyString())(any(), any())
        verify(mockCapabilitiesConnector, times(1)).payeIncomeList(anyString())(any(), any())

        verify(mockCapabilityDetailsRules, times(7)).withinTaxYear(any())
        verify(mockCapabilityDetailsRules, never()).withinSixMonth(any())

        result mustBe orderedActivities
      }
    }
  }

  "retrieveActionsData" must {

    "return empty Seq if capabilitiesConnector returns empty Seq" in {

      when(mockCapabilitiesConnector.actionTaxCalcList(anyString())(any(), any())).thenReturn(Future.successful(Seq.empty))

      capabilityDetailsService.retrieveActionsData("nino-for-empty-list").map { result =>
        verify(mockCapabilitiesConnector, times(1)).actionTaxCalcList(anyString())(any(), any())

        verify(mockCapabilityDetailsRules, never()).withinTaxYear(any())
        verify(mockCapabilityDetailsRules, never()).withinSixMonth(any())

        result mustBe Actions(Seq.empty)
      }
    }

    "return empty Seq if capabilityDetails does not pass the rules" in {

      when(mockCapabilitiesConnector.actionTaxCalcList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedOverPayment))
      when(mockCapabilityDetailsRules.withinTaxYear(any())).thenReturn(false)
      when(mockCapabilityDetailsRules.withinSixMonth(any())).thenReturn(false)

      capabilityDetailsService.retrieveActionsData("nino-for-empty-list").map { result =>
        verify(mockCapabilitiesConnector, times(1)).actionTaxCalcList(anyString())(any(), any())

        verify(mockCapabilityDetailsRules, times(2)).withinTaxYear(any())
        verify(mockCapabilityDetailsRules, times(2)).withinSixMonth(any())

        result mustBe Actions(Seq.empty)
      }
    }

    "return ordered Seq of capabilityDetails if retrieved unorderedCapabilityDetails withinTaxYear" in {

      when(mockCapabilitiesConnector.actionTaxCalcList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedOverPayment))
      when(mockCapabilityDetailsRules.withinTaxYear(any())).thenReturn(true)

      capabilityDetailsService.retrieveActionsData("nino-for-empty-list").map { result =>
        verify(mockCapabilitiesConnector, times(1)).actionTaxCalcList(anyString())(any(), any())

        verify(mockCapabilityDetailsRules, times(2)).withinTaxYear(any())
        verify(mockCapabilityDetailsRules, never()).withinSixMonth(any())

        result mustBe orderedActions
      }
    }

    "return ordered list of Activities if retrieved unorderedCapabilityDetails withinSixMonth" in {

      when(mockCapabilitiesConnector.actionTaxCalcList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedOverPayment))
      when(mockCapabilityDetailsRules.withinTaxYear(any())).thenReturn(false)
      when(mockCapabilityDetailsRules.withinSixMonth(any())).thenReturn(true)

      capabilityDetailsService.retrieveActionsData("nino-for-empty-list").map { result =>
        verify(mockCapabilitiesConnector, times(1)).actionTaxCalcList(anyString())(any(), any())

        verify(mockCapabilityDetailsRules, times(2)).withinTaxYear(any())
        verify(mockCapabilityDetailsRules, times(2)).withinSixMonth(any())

        result mustBe orderedActions
      }
    }

    "return ordered list of Activities if retrieved unorderedCapabilityDetails both withinTaxYear and withinSixMonth" in {

      when(mockCapabilitiesConnector.actionTaxCalcList(anyString())(any(), any())).thenReturn(Future.successful(unOrderedOverPayment))
      when(mockCapabilityDetailsRules.withinTaxYear(any())).thenReturn(true)
      when(mockCapabilityDetailsRules.withinSixMonth(any())).thenReturn(true)

      capabilityDetailsService.retrieveActionsData("nino-for-empty-list").map { result =>
        verify(mockCapabilitiesConnector, times(1)).actionTaxCalcList(anyString())(any(), any())

        verify(mockCapabilityDetailsRules, times(2)).withinTaxYear(any())
        verify(mockCapabilityDetailsRules, never()).withinSixMonth(any())

        result mustBe orderedActions
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
      url = "url-1",
      activityHeading = "activityHeading-1"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 4, 9),
      descriptionContent = "Desc-2",
      url = "url-2",
      activityHeading = "activityHeading-2"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 1, 19),
      descriptionContent = "Desc-3",
      url = "url-3",
      activityHeading = "activityHeading-3")
  )

  val orderedByDateCapabilityDetails: Seq[CapabilityDetails] = Seq(
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 4, 9),
      descriptionContent = "Desc-2",
      url = "url-2",
      activityHeading = "activityHeading-2"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 1, 19),
      descriptionContent = "Desc-3",
      url = "url-3",
      activityHeading = "activityHeading-3"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2022, 5, 19),
      descriptionContent = "Desc-1",
      url = "url-1",
      activityHeading = "activityHeading-1")
  )

  val unOrderedTaxCalcDetails: Seq[CapabilityDetails] = Seq(
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 3, 1),
      descriptionContent = "Your tax calculation for the 2022-2023 is now available",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Your tax calculation"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 4, 1),
      descriptionContent = "Your tax calculation for the 2022-2023 is now available",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Your tax calculation")
  )

  val orderedByDateTaxCalcDetails: Seq[CapabilityDetails] = Seq(
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 4, 1),
      descriptionContent = "Your tax calculation for the 2022-2023 is now available",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Your tax calculation"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 3, 1),
      descriptionContent = "Your tax calculation for the 2022-2023 is now available",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Your tax calculation")

  )

  val unOrderedTaxCodeChangeDetails: Seq[TaxCodeChangeObject] = Seq(
    TaxCodeChangeObject(
      data = TaxCodeChangeData(
        current = TaxCodeChangeDetails(
          taxCode = "830L", employerName = "Employer Name", operatedTaxCode = true, p2Issued = true, startDate = LocalDate.of(2023, 5, 21).toString, endDate = LocalDate.of(2023, 10, 21).toString, payrollNumber = "1", pensionIndicator = true, primary = true
        ),
        previous = TaxCodeChangeDetails(
          taxCode = "1150L", employerName = "Employer Name", operatedTaxCode = true, p2Issued = true, startDate = LocalDate.now.minusMonths(1).minusDays(2).toString, endDate = "2018-06-26", payrollNumber = "1", pensionIndicator = true, primary = true
        )
      ),
      links = Array.empty[String]
    )
  )

  val orderedByDateTaxCodeChangeDetails: Seq[CapabilityDetails] = Seq(
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 5, 21),
      descriptionContent = "Your tax code has changed - 1",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Latest Tax code change")
  )

  val unOrderedChildBenefitDetails: Seq[CapabilityDetails] = Seq(
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 5, 5),
      descriptionContent = "HMRC paid you Child Benefit",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Recent Child Benefit payments"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 6, 6),
      descriptionContent = "HMRC paid you Child Benefit",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Recent Child Benefit payments"),
  )

  val orderedByDateChildBenefitDetails: Seq[CapabilityDetails] = Seq(
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 6, 6),
      descriptionContent = "HMRC paid you Child Benefit",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Recent Child Benefit payments"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 5, 5),
      descriptionContent = "HMRC paid you Child Benefit",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Recent Child Benefit payments"),
  )

  val unOrderedPayeIncomeDetails: Seq[CapabilityDetails] = Seq(
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 4, 5),
      descriptionContent = "Central Perk Coffee Ltd paid you PAYE income",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Your PAYE income for the current tax year"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 6, 5),
      descriptionContent = "Central Perk Coffee Ltd paid you PAYE income",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Your PAYE income for the current tax year")
  )

  val orderedByDatePayeIncomeDetails: Seq[CapabilityDetails] = Seq(
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 6, 5),
      descriptionContent = "Central Perk Coffee Ltd paid you PAYE income",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Your PAYE income for the current tax year"),
    CapabilityDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 4, 5),
      descriptionContent = "Central Perk Coffee Ltd paid you PAYE income",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Your PAYE income for the current tax year")
  )

  private val unOrderedOverPayment = Seq(
    ActionDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 1, 10),
      descriptionContent = "You paid too much tax in the 2022 to 2023 tax year. HMRC owes you a £84.23 refund",
      actionDescription = "Claim your tax refund",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Things for you to do"),
    ActionDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 1, 15),
      descriptionContent = "You paid too much tax in the 2022 to 2023 tax year. HMRC owes you a £84.23 refund",
      actionDescription = "Claim your tax refund",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Things for you to do")
  )

  private val orderedOverPayment = Seq(
    ActionDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 1, 15),
      descriptionContent = "You paid too much tax in the 2022 to 2023 tax year. HMRC owes you a £84.23 refund",
      actionDescription = "Claim your tax refund",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Things for you to do"),
    ActionDetails(
      nino = Nino(true, Some("GG012345C")),
      date = LocalDate.of(2023, 1, 10),
      descriptionContent = "You paid too much tax in the 2022 to 2023 tax year. HMRC owes you a £84.23 refund",
      actionDescription = "Claim your tax refund",
      url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
      activityHeading = "Things for you to do")
  )

  val orderedActivities: Activities =
    Activities(orderedByDateTaxCalcDetails,orderedByDateTaxCodeChangeDetails,orderedByDateChildBenefitDetails,orderedByDatePayeIncomeDetails)

  val orderedActions =
    Actions(orderedOverPayment)

}