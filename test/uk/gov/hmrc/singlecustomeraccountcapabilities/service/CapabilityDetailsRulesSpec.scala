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

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.singlecustomeraccountcapabilities.utils.DateHelper

import java.time.LocalDate

class CapabilityDetailsRulesSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    super.beforeEach()
  }

  "withinTaxYear" must {
    DateHelper.setDate(Some(LocalDate.of(2023, 7, 1)))
    val capabilityDetailsRules = new CapabilityDetailsRules()

    "return false if taxCodeChange is before TaxYear" in {
      capabilityDetailsRules.withinTaxYear(LocalDate.of(2023, 4, 5)) mustBe false
    }

    "return false if taxCodeChange is after TaxYear" in {
      capabilityDetailsRules.withinTaxYear(LocalDate.of(2024, 4, 6)) mustBe false
    }

    "return true if taxCodeChange is happens at first day of TaxYear" in {
      capabilityDetailsRules.withinTaxYear(LocalDate.of(2023, 4, 6)) mustBe true
    }

    "return true if taxCodeChange is happens at last day of TaxYear" in {
      capabilityDetailsRules.withinTaxYear(LocalDate.of(2024, 4, 5)) mustBe true
    }

  }

  "withinSixMonth" must {

    DateHelper.setDate(Some(LocalDate.of(2023, 7, 1)))
    val capabilityDetailsRules = new CapabilityDetailsRules()

    "return false if taxCodeChange is before 6 month" in {
      capabilityDetailsRules.withinSixMonth(LocalDate.of(2022, 12, 31)) mustBe false
    }

    "return true if taxCodeChange is equald 6 month" in {
      capabilityDetailsRules.withinSixMonth(LocalDate.of(2023, 1, 1)) mustBe true
    }

    "return true if taxCodeChange is after 6 month" in {
      capabilityDetailsRules.withinSixMonth(LocalDate.of(2023, 1, 2)) mustBe true
    }

  }
}