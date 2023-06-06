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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.singlecustomeraccountcapabilities.connectors.CapabilitiesConnector
import uk.gov.hmrc.singlecustomeraccountcapabilities.models.{Activities, CapabilityDetails}

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class CapabilityDetailsService @Inject()(capabilitiesConnector: CapabilitiesConnector, capabilitiesRules: CapabilityDetailsRules) {


  def retrieveCapabilitiesData(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[CapabilityDetails]] =
    capabilitiesConnector.list(nino).map { capabilityDetails =>
      capabilityDetails.filter(capabilityDetail => withinValidTimeFrame(capabilityDetail.date))
        .sortWith((x, y) => x.date.isAfter(y.date))
    }

  def retrieveActivitiesData(nino: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[CapabilityDetails]] =
    capabilitiesConnector.list(nino).map { activities =>
      activities.filter(activities => withinValidTimeFrame(activities.date))
        .sortWith((x, y) => x.date.isAfter(y.date))
    }

  def retrieveAllActivitiesData(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Activities] ={

    def sortFilter(activities: Seq[CapabilityDetails]): Seq[CapabilityDetails] ={
      activities.filter(activities => withinValidTimeFrame(activities.date))
        .sortWith((x, y) => x.date.isAfter(y.date))}

    for{
      taxCalc <- capabilitiesConnector.taxCalcList(nino)
      taxCode <- capabilitiesConnector.taxCodeList(nino)
      childBenefit <- capabilitiesConnector.childBenefitList(nino)
      payeIncome <- capabilitiesConnector.payeIncomeList(nino)
    }
    yield{
      Activities(sortFilter(taxCalc), sortFilter(taxCode), sortFilter(childBenefit), sortFilter(payeIncome))
    }
  }

  private def withinValidTimeFrame(taxCodeChangeDate: LocalDate): Boolean =
    capabilitiesRules.withinTaxYear(taxCodeChangeDate) || capabilitiesRules.withinSixMonth(taxCodeChangeDate)
}

