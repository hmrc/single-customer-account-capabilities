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

package uk.gov.hmrc.singlecustomeraccountcapabilities.controllers

import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.singlecustomeraccountcapabilities.controllers.CapabilityDetailsControllerSpec.{allActivityData, overPaymentData}
import uk.gov.hmrc.singlecustomeraccountcapabilities.models.{ActionDetails, Actions, Activities, CapabilityDetails}
import uk.gov.hmrc.singlecustomeraccountcapabilities.service.CapabilityDetailsService

import java.time.LocalDate
import scala.concurrent.Future

class CapabilityDetailsControllerSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val fakeRequest = FakeRequest("GET", "/")
  private val mockCapabilitiesService: CapabilityDetailsService = mock[CapabilityDetailsService]

  val modules: Seq[GuiceableModule] =
    Seq(
      inject.bind[CapabilityDetailsService].toInstance(mockCapabilitiesService)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false).
    overrides(modules: _*).build()

  private val controller = application.injector.instanceOf[CapabilityDetailsController]

  override protected def beforeEach(): Unit = {
    reset(mockCapabilitiesService)
    super.beforeEach()
  }

  "GET /activities" must {
    "return 200" in {

      when(mockCapabilitiesService.retrieveAllActivitiesData(anyString())(any(), any())).thenReturn(Future.successful(allActivityData))

      val result = controller.getAllActivitiesData("valid-nino")(fakeRequest)

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.obj(
            "taxCalc" ->
            Json.arr(
              Json.obj(
              "nino" -> Json.obj(
                "hasNino" -> true,
                "nino" -> "GG012345C"
              ),
              "date" -> "2023-02-05",
              "descriptionContent" -> "Your tax calculation for the 2022-2023 is now available",
              "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
              "activityHeading" -> "Your tax calculation"
              )
            ),
            "taxCode" ->
              Json.arr(
                Json.obj(
                  "nino" -> Json.obj(
                    "hasNino" -> true,
                    "nino" -> "GG012345C"
                  ),
                  "date" -> "2023-06-06",
                  "descriptionContent" -> "Your tax code has changed - 7",
                  "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
                  "activityHeading" -> "Latest Tax code change"
                ),
                Json.obj(
                  "nino" -> Json.obj(
                    "hasNino" -> true,
                    "nino" -> "GG012345C"
                  ),
                  "date" -> "2023-05-05",
                  "descriptionContent" -> "Your tax code has changed - 1",
                  "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
                  "activityHeading" -> "Latest Tax code change"
                ),
                Json.obj(
                  "nino" -> Json.obj(
                    "hasNino" -> true,
                    "nino" -> "GG012345C"
                  ),
                  "date" -> "2023-04-07",
                  "descriptionContent" -> "Your tax code has changed - 6",
                  "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
                  "activityHeading" -> "Latest Tax code change"
                ),
                Json.obj(
                  "nino" -> Json.obj(
                    "hasNino" -> true,
                    "nino" -> "GG012345C"
                  ),
                  "date" -> "2023-04-06",
                  "descriptionContent" -> "Your tax code has changed - 2",
                  "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
                  "activityHeading" -> "Latest Tax code change"
                ),
                Json.obj(
                  "nino" -> Json.obj(
                    "hasNino" -> true,
                    "nino" -> "GG012345C"
                  ),
                  "date" -> "2023-04-06",
                  "descriptionContent" -> "Your tax code has changed - 5",
                  "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
                  "activityHeading" -> "Latest Tax code change"
                ),
                Json.obj(
                  "nino" -> Json.obj(
                    "hasNino" -> true,
                    "nino" -> "GG012345C"
                  ),
                  "date" -> "2023-04-05",
                  "descriptionContent" -> "Your tax code has changed - 4",
                  "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
                  "activityHeading" -> "Latest Tax code change"
                ),
                Json.obj(
                  "nino" -> Json.obj(
                    "hasNino" -> true,
                    "nino" -> "GG012345C"
                  ),
                  "date" -> "2023-03-07",
                  "descriptionContent" -> "Your tax code has changed - 3",
                  "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
                  "activityHeading" -> "Latest Tax code change"
                )
          ),
          "childBenefit" ->
            Json.arr(
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
                "date" -> "2023-03-07",
                "descriptionContent" -> "HMRC paid you Child Benefit",
                "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
                "activityHeading" -> "Recent Child Benefit payments"
              )
          ),
          "payeIncome" ->
            Json.arr(
              Json.obj(
                "nino" -> Json.obj(
                  "hasNino" -> true,
                  "nino" -> "GG012345C"
                ),
                "date" -> "2023-04-05",
                "descriptionContent" -> "Central Perk Coffee Ltd paid you PAYE income",
                "url" -> "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
                "activityHeading" -> "Your PAYE income for the current tax year"
              )
          )
        )
      }

    }

    "return Empty List When capabilities not found with the nino" in {

      val emptyActivities = Activities(Seq.empty,Seq.empty,Seq.empty,Seq.empty)

      when(mockCapabilitiesService.retrieveAllActivitiesData(anyString())(any(), any())).thenReturn(Future.successful(emptyActivities))

      val result = controller.getAllActivitiesData("invalid-nino")(fakeRequest)
      val emptyResult = Json.obj("taxCalc" -> Json.arr(),"taxCode" -> Json.arr(),"childBenefit" -> Json.arr(),"payeIncome" -> Json.arr())

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsJson(result) mustBe emptyResult
      }
    }
  }

  "GET /actions" must {
    "return 200" in {

      when(mockCapabilitiesService.retrieveActionsData(anyString())(any(), any())).thenReturn(Future.successful(overPaymentData))

      val result = controller.getAllActionsData("valid-nino")(fakeRequest)

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.obj(
          "taxCalc" ->
            Json.arr(
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
              )
            )
        )
      }
    }

    "return Empty List When actions not found with the nino" in {

      val emptyActivities = Actions(Seq.empty)

      when(mockCapabilitiesService.retrieveActionsData(anyString())(any(), any())).thenReturn(Future.successful(emptyActivities))

      val result = controller.getAllActionsData("invalid-nino")(fakeRequest)
      val emptyResult = Json.obj("taxCalc" -> Json.arr())

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsJson(result) mustBe emptyResult
      }
    }
  }
}

object CapabilityDetailsControllerSpec {
  val allActivityData: Activities = {
    Activities(
      taxCalc = Seq(
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023, 2, 5),
          descriptionContent = "Your tax calculation for the 2022-2023 is now available",
          url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          activityHeading = "Your tax calculation")
      ),
      taxCode = Seq(
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023, 6, 6),
          descriptionContent = "Your tax code has changed - 7",
          url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          activityHeading = "Latest Tax code change"),
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023,5 , 5),
          descriptionContent = "Your tax code has changed - 1",
          url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          activityHeading = "Latest Tax code change"),
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023, 4, 7),
          descriptionContent = "Your tax code has changed - 6",
          url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          activityHeading = "Latest Tax code change"),
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023, 4, 6),
          descriptionContent = "Your tax code has changed - 2",
          url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          activityHeading = "Latest Tax code change"),
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023, 4, 6),
          descriptionContent = "Your tax code has changed - 5",
          url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          activityHeading = "Latest Tax code change"),
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023, 4, 5),
          descriptionContent = "Your tax code has changed - 4",
          url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          activityHeading = "Latest Tax code change"),
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023, 3, 7),
          descriptionContent = "Your tax code has changed - 3",
          url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          activityHeading = "Latest Tax code change"),
      ),
      childBenefit = Seq(
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023, 5, 5),
          descriptionContent = "HMRC paid you Child Benefit",
          url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          activityHeading = "Recent Child Benefit payments"),
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023, 4, 6),
          descriptionContent = "HMRC paid you Child Benefit",
          url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          activityHeading = "Recent Child Benefit payments"),
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023, 4, 6),
          descriptionContent = "HMRC paid you Child Benefit",
          url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          activityHeading = "Recent Child Benefit payments"),
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023, 4, 5),
          descriptionContent = "HMRC paid you Child Benefit",
          url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          activityHeading = "Recent Child Benefit payments"),
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023, 3, 7),
          descriptionContent = "HMRC paid you Child Benefit",
          url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          activityHeading = "Recent Child Benefit payments"),
      ),
      payeIncome = Seq(
        CapabilityDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023, 4, 5),
          descriptionContent = "Central Perk Coffee Ltd paid you PAYE income",
          url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          activityHeading = "Your PAYE income for the current tax year")
      )
    )
  }

  val overPaymentData =
    Actions(
      taxCalc = Seq(
        ActionDetails(
          nino = Nino(true, Some("GG012345C")),
          date = LocalDate.of(2023,1,10),
          descriptionContent = "You paid too much tax in the 2022 to 2023 tax year. HMRC owes you a £84.23 refund",
          actionDescription = "Claim your tax refund",
          url = "www.tax.service.gov.uk/check-income-tax/tax-code-change/tax-code-comparison",
          activityHeading = "Things for you to do")
      )
    )
}


