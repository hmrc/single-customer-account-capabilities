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

import uk.gov.hmrc.singlecustomeraccountcapabilities.utils.DateHelper
import uk.gov.hmrc.time.CurrentTaxYear

import java.time.LocalDate
import javax.inject.Singleton

@Singleton()
class CapabilityDetailsRules extends CurrentTaxYear {

  private val MONTH_6 = 6

  override def now: () => LocalDate = () => DateHelper.today

  def withinTaxYear(taxCodeChangeDate: LocalDate): Boolean = {
    (taxCodeChangeDate.isEqual(current.starts) || taxCodeChangeDate.isAfter(current.starts)) &&
      (taxCodeChangeDate.isBefore(current.finishes) || taxCodeChangeDate.isEqual(current.finishes))
  }

  def withinSixMonth(taxCodeChangeDate: LocalDate): Boolean = {
    val sixMonthsOneDayAgo = today.minusMonths(MONTH_6).minusDays(1)
    sixMonthsOneDayAgo.isBefore(taxCodeChangeDate)
  }
}