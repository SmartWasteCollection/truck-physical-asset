package swc.drivers

import com.azure.core.credential.TokenCredential
import com.azure.core.models.JsonPatchDocument
import com.azure.digitaltwins.core.DigitalTwinsClient
import com.azure.digitaltwins.core.DigitalTwinsClientBuilder
import com.azure.identity.AzureCliCredentialBuilder
import com.azure.messaging.servicebus.ServiceBusClientBuilder
import com.azure.messaging.servicebus.ServiceBusProcessorClient
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext
import io.github.cdimascio.dotenv.dotenv
import swc.drivers.JsonDriver.parse
import swc.drivers.JsonDriver.toTruck
import swc.entities.Truck
import swc.view.TruckGUI

object AzureDriver {
    object Constants {
        const val DT_SERVICE_ENDPOINT: String = "https://test-instance.api.wcus.digitaltwins.azure.net/"
    }

    object Authentication {
        val client = authenticate { AzureCliCredentialBuilder().build() }
        lateinit var eventsClient: ServiceBusProcessorClient

        private fun authenticate(builder: () -> TokenCredential): DigitalTwinsClient = DigitalTwinsClientBuilder()
            .credential(builder())
            .endpoint(Constants.DT_SERVICE_ENDPOINT)
            .buildClient()

        fun authenticateEvents(processMethod: (context: ServiceBusReceivedMessageContext) -> Unit): ServiceBusProcessorClient = ServiceBusClientBuilder()
            .connectionString(dotenv()["SERVICE_BUS_CONNECTION_STRING"])
            .processor()
            .topicName("trucks-topic")
            .subscriptionName("trucks-subscription")
            .processMessage { processMethod(it) }
            .processError { println("ERROR: ${it.exception.cause}") }
            .buildProcessorClient()
            .also { eventsClient = it }
    }

    object DigitalTwins {
        fun getTruck(truckId: String): Truck =
            parse(Authentication.client.getDigitalTwin(truckId, String::class.java)).toTruck()

        fun updateLatitude(id: String, latitude: Double) {
            Authentication.client.updateDigitalTwin(id, JsonPatchDocument().appendAdd("/position/latitude", latitude))
        }

        fun updateLongitude(id: String, longitude: Double) {
            Authentication.client.updateDigitalTwin(id, JsonPatchDocument().appendAdd("/position/longitude", longitude))
        }

        fun updateVolume(id: String, volume: Double) {
            Authentication.client.updateDigitalTwin(id, JsonPatchDocument().appendAdd("/occupiedVolume/value", volume))
        }
    }

    object Events {
        fun listenToEvents(processMethod: (context: ServiceBusReceivedMessageContext) -> Unit) {
            Authentication.authenticateEvents(processMethod)
            Authentication.eventsClient.start()
        }
    }
}
