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

import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.singlecustomeraccountcapabilities.config.AppConfig
import uk.gov.hmrc.singlecustomeraccountcapabilities.models.CapabilityDetails

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CapabilitiesConnector @Inject()(appConfig: AppConfig, httpClientV2: HttpClientV2) {

  private val endpoint = s"${appConfig.capabilitiesDataBaseUrl}/individuals/details/NINO/%s"

  def list(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[CapabilityDetails]] = {
    httpClientV2.get(url"${endpoint.format(nino)}")
      .execute[Option[Seq[CapabilityDetails]]]
      .map {
        case Some(capabilityDetails) => capabilityDetails
        case _ => Seq.empty
      }
  }
}
