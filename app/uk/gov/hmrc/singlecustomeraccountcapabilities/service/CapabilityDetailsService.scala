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
import uk.gov.hmrc.singlecustomeraccountcapabilities.connectors.{ActivitiesConnector, CapabilitiesConnector}
import uk.gov.hmrc.singlecustomeraccountcapabilities.models.CapabilityDetails

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

  def retrieveActivitiesData(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[CapabilityDetails]] =

//    val activitiesDataList = Seq(capabilitiesConnector.taxCalcList(nino))

    capabilitiesConnector.list(nino).map { activities =>
      activities.filter(activities => withinValidTimeFrame(activities.date))
        .sortWith((x, y) => x.date.isAfter(y.date))
    }

  private def withinValidTimeFrame(taxCodeChangeDate: LocalDate): Boolean =
    capabilitiesRules.withinTaxYear(taxCodeChangeDate) || capabilitiesRules.withinSixMonth(taxCodeChangeDate)
}


/*
def listActivity(nino: String, endpoint: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[CapabilityDetails]] = {
  httpClientV2.get(url"${endpoint.format(nino)}")
    .execute[Option[Seq[CapabilityDetails]]]
    .map {
      case Some(capabilityDetails) => capabilityDetails
      case _ => Seq.empty
    }
}

def listActivities(nino: String): Unit = {
  endpointArray.map(endpoint => listActivity(nino, endpoint) )
}
 */