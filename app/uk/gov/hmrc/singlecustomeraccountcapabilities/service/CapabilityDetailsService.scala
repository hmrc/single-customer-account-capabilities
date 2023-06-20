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

import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.singlecustomeraccountcapabilities.connectors.CapabilitiesConnector
import uk.gov.hmrc.singlecustomeraccountcapabilities.models.{ActionDetails, Actions, Activities, CapabilityDetails}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class CapabilityDetailsService @Inject()(capabilitiesConnector: CapabilitiesConnector, capabilitiesRules: CapabilityDetailsRules) {


  def retrieveCapabilitiesData(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[CapabilityDetails]] =
    capabilitiesConnector.list(nino).map { capabilityDetails =>
      capabilityDetails.filter(capabilityDetail => withinValidTimeFrame(capabilityDetail.date))
        .sortWith((x, y) => x.date.isAfter(y.date))
    }

  def retrieveAllActivitiesData(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Activities] = {

    def sortFilter(activities: Seq[CapabilityDetails]): Seq[CapabilityDetails] = {
      activities.filter(activities => withinValidTimeFrame(activities.date))
        .sortWith((x, y) => x.date.isAfter(y.date))
    }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    for {
      taxCalc <- capabilitiesConnector.taxCalcList(nino)
      taxDetails <- capabilitiesConnector.taxCodeList(nino)
      childBenefit <- capabilitiesConnector.childBenefitList(nino)
      payeIncome <- capabilitiesConnector.payeIncomeList(nino)
    }
    yield {
      Activities(sortFilter(taxCalc), sortFilter(Seq(CapabilityDetails(nino = Nino(true, Some("GG012345C")),
        date = LocalDate.parse(taxDetails.head.data.current.startDate, formatter),
        descriptionContent = "Your tax code has changed - 1",
        url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
        activityHeading = "Latest Tax code change"))),
        sortFilter(childBenefit), sortFilter(payeIncome))
    }
  }

  def retrieveActionsData(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Actions] = {

    def sortFilter(actions: Seq[ActionDetails]): Seq[ActionDetails] = {
      actions.filter(actions => withinValidTimeFrame(actions.date))
        .sortWith((x, y) => x.date.isAfter(y.date))
    }

    for {
      taxCalc <- capabilitiesConnector.actionTaxCalcList(nino)
    }
    yield {
      Actions(sortFilter(taxCalc))
    }
  }

  private def withinValidTimeFrame(taxCodeChangeDate: LocalDate): Boolean =
    capabilitiesRules.withinTaxYear(taxCodeChangeDate) || capabilitiesRules.withinSixMonth(taxCodeChangeDate)
}

