package connectors

import akka.stream.TLSRole.server
import play.api.libs.ws.WSClient
import play.api.test.Helpers.baseApplicationBuilder.injector
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.singlecustomeraccountcapabilities.connectors.CapabilitiesConnector

class CapabilitiesConnectorSpec {

  lazy val mockAuthConnector: AuthConnector = mock[AuthConnector]
  lazy val authAction = new AuthActionImpl(mockAuthConnector, frontendAppConfigInstance, bodyParserInstance)
  lazy val connector: CapabilitiesConnector = new CapabilitiesConnector(injector.instanceOf[WSClient], frontendAppConfigInstance)
  server.start()


}
