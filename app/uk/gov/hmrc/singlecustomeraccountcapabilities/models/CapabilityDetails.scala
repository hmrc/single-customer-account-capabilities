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

package uk.gov.hmrc.singlecustomeraccountcapabilities.models

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.auth.core.Nino

import java.time.LocalDate


case class CapabilityDetails(nino: Nino, date: LocalDate, descriptionContent: String, url: String, activityHeading: String)

object CapabilityDetails {

  implicit val format: Format[CapabilityDetails] = Json.format[CapabilityDetails]

}

case class ActionDetails(
                          nino: Nino,
                          date: LocalDate,
                          descriptionContent: String,
                          actionDescription: String,
                          url: String,
                          activityHeading: String
                        )

object ActionDetails {

  implicit val format: Format[ActionDetails] = Json.format[ActionDetails]

}

case class TaxCodeChangeDetails(
                                 taxCode: String,
                                 employerName: String,
                                 operatedTaxCode: Boolean,
                                 p2Issued: Boolean,
                                 startDate: String,
                                 endDate: String,
                                 payrollNumber: String,
                                 pensionIndicator: Boolean,
                                 primary: Boolean
                               )

object TaxCodeChangeDetails {

  implicit val format: Format[TaxCodeChangeDetails] = Json.format[TaxCodeChangeDetails]

}

case class TaxCodeChangeData (
                               current: TaxCodeChangeDetails,
                               previous: TaxCodeChangeDetails
                             )

object TaxCodeChangeData {

  implicit val format: Format[TaxCodeChangeData] = Json.format[TaxCodeChangeData]

}

case class TaxCodeChangeObject (
                                 data: TaxCodeChangeData,
                                 links: Array[String]
                               )

object TaxCodeChangeObject {

  implicit val format: Format[TaxCodeChangeObject] = Json.format[TaxCodeChangeObject]

}

