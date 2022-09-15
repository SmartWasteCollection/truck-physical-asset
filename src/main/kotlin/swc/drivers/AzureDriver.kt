package swc.drivers

import com.azure.core.credential.TokenCredential
import com.azure.core.models.JsonPatchDocument
import com.azure.digitaltwins.core.DigitalTwinsClient
import com.azure.digitaltwins.core.DigitalTwinsClientBuilder
import com.azure.identity.AzureCliCredentialBuilder
import com.azure.messaging.servicebus.ServiceBusClientBuilder
import com.azure.messaging.servicebus.ServiceBusProcessorClient
import swc.drivers.JsonDriver.parse
import swc.drivers.JsonDriver.toTruck
import swc.entities.Truck
import swc.view.TruckGUI

object AzureDriver {
    object Constants {
        const val TRUCK_DT_MODEL_ID = "dtmi:swc:Truck;1"
        const val DT_SERVICE_ENDPOINT = "https://test-instance.api.wcus.digitaltwins.azure.net/"
        const val CONNECTION_STRING = "Endpoint=sb://swc-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=2CC1DN11vk6jggICwJFojsVNxAE4yohQFpdt9+VwTK8="
    }

    object Authentication {
        val client = authenticate { AzureCliCredentialBuilder().build() }
        var eventsClient: ServiceBusProcessorClient? = null

        private fun authenticate(builder: () -> TokenCredential): DigitalTwinsClient = DigitalTwinsClientBuilder()
            .credential(builder())
            .endpoint(Constants.DT_SERVICE_ENDPOINT)
            .buildClient()

        fun authenticateEvents(panel: TruckGUI.TruckPanel): ServiceBusProcessorClient = ServiceBusClientBuilder()
            .connectionString(Constants.CONNECTION_STRING)
            .processor()
            .topicName("trucks-topic")
            .subscriptionName("trucks-subscription")
            .processMessage(panel.messageProcessor)
            .processError { println("ERROR: ${it.exception.cause}") }
            .buildProcessorClient()
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
        fun listenToEvents(panel: TruckGUI.TruckPanel) {
            Authentication.authenticateEvents(panel).also { Authentication.eventsClient = it }
            Authentication.eventsClient?.start()
        }
    }
}
