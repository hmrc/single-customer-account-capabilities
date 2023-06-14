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

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.singlecustomeraccountcapabilities.service.CapabilityDetailsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext


@Singleton()
class CapabilityDetailsController @Inject()(capabilitiesService: CapabilityDetailsService,
                                            cc: ControllerComponents)
                                           (implicit ec: ExecutionContext)
  extends BackendController(cc) with Logging {

  def getCapabilitiesData(nino: String): Action[AnyContent] = Action.async { implicit request =>
    capabilitiesService.retrieveCapabilitiesData(nino).map { capabilityDetail =>
      logger.info(s"[CapabilityDetailsController][getCapabilitiesData] capabilities data fetched")
      Ok(Json.toJson(capabilityDetail))
    }
  }
  def getAllActivitiesData(nino: String): Action[AnyContent] = Action.async { implicit request =>
    capabilitiesService.retrieveAllActivitiesData(nino).map { activities =>
      logger.info(s"[CapabilityDetailsController][getAllActivitiesData] activities data fetched")
      Ok(Json.toJson(activities))
    }
  }

  def getAllActionsData(nino: String): Action[AnyContent] = Action.async { implicit request =>
    capabilitiesService.retrieveActionsData(nino).map { actions =>
      logger.info(s"[CapabilityDetailsController][getAllActionsData] actions data fetched")
      Ok(Json.toJson(actions))
    }
  }

}
