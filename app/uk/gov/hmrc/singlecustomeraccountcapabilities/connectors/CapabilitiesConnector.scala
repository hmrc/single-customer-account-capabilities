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



import play.api.Logging
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.singlecustomeraccountcapabilities.config.AppConfig
import uk.gov.hmrc.singlecustomeraccountcapabilities.models.{ActionDetails, CapabilityDetails, TaxCodeChangeObject}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CapabilitiesConnector @Inject()(appConfig: AppConfig, httpClientV2: HttpClientV2) extends Logging {

  private val capabilitiesEndpoint = s"${appConfig.capabilitiesDataBaseUrl}/individuals/details/NINO/%s"
  private val taxCalcEndpoint = s"${appConfig.capabilitiesDataBaseUrl}/individuals/activities/tax-calc/NINO/%s"
  private val taxCodeEndpoint = s"${appConfig.capabilitiesDataBaseUrl}/individuals/activities/tax-code-change/NINO/%s"
  private val childBenefitEndpoint = s"${appConfig.capabilitiesDataBaseUrl}/individuals/activities/child-benefit/NINO/%s"
  private val payeIncomeEndpoint = s"${appConfig.capabilitiesDataBaseUrl}/individuals/activities/paye-income/NINO/%s"
  private val actionTaxCalcEndpoint = s"${appConfig.capabilitiesDataBaseUrl}/individuals/actions/tax-calc/NINO/%s"


  def list(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[CapabilityDetails]] = {
    httpClientV2.get(url"${capabilitiesEndpoint.format(nino)}")
      .execute[Option[Seq[CapabilityDetails]]]
      .map {
        case Some(capabilityDetails) => capabilityDetails
        case _ => Seq.empty
      }.recover {
      case ex: Exception =>
        logger.error(s"[CapabilityConnector][list] exception: ${ex.getMessage}")
        Seq.empty
    }
  }
  def taxCalcList(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[CapabilityDetails]] = {
    httpClientV2.get(url"${taxCalcEndpoint.format(nino)}")
      .execute[Option[Seq[CapabilityDetails]]]
      .map {
        case Some(capabilityDetails) => capabilityDetails
        case _ => Seq.empty
      }.recover {
      case ex: Exception =>
        logger.error(s"[CapabilityConnector][taxCalcList] exception: ${ex.getMessage}")
        Seq.empty
    }
  }

  def taxCodeChange(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[TaxCodeChangeObject] = {
    httpClientV2.get(url"${taxCodeEndpoint.format(nino)}")
      .execute[Option[TaxCodeChangeObject]]
      .map {
        case Some(taxCodedata) =>
          println(taxCodedata)
          taxCodedata
        case _ =>
          println("secondCase ran")
          null
      }.recover {
      case ex: Exception =>
        logger.error(s"[CapabilityConnector][taxCodeChange] exception: ${ex.getMessage}")
        null
    }
  }
  def childBenefitList(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[CapabilityDetails]] = {
    httpClientV2.get(url"${childBenefitEndpoint.format(nino)}")
      .execute[Option[Seq[CapabilityDetails]]]
      .map {
        case Some(capabilityDetails) => capabilityDetails
        case _ => Seq.empty
      }.recover {
      case ex: Exception =>
        logger.error(s"[CapabilityConnector][childBenefitList] exception: ${ex.getMessage}")
        Seq.empty
    }
  }
  def payeIncomeList(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[CapabilityDetails]] = {
    httpClientV2.get(url"${payeIncomeEndpoint.format(nino)}")
      .execute[Option[Seq[CapabilityDetails]]]
      .map {
        case Some(capabilityDetails) => capabilityDetails
        case _ => Seq.empty
      }.recover {
      case ex: Exception =>
        logger.error(s"[CapabilityConnector][payeIncomeList] exception: ${ex.getMessage}")
        Seq.empty
    }
  }

  def actionTaxCalcList(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[ActionDetails]] = {
    httpClientV2.get(url"${actionTaxCalcEndpoint.format(nino)}")
      .execute[Option[Seq[ActionDetails]]]
      .map {
        case Some(capabilityDetails) => capabilityDetails
        case _ => Seq.empty
      }.recover {
      case ex: Exception =>
        logger.error(s"[CapabilityConnector][actionTaxCalcList] exception: ${ex.getMessage}")
        Seq.empty
    }
  }
}
