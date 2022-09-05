package swc.drivers

import com.azure.core.credential.TokenCredential
import com.azure.digitaltwins.core.DigitalTwinsClient
import com.azure.digitaltwins.core.DigitalTwinsClientBuilder
import com.azure.identity.AzureCliCredentialBuilder
import swc.drivers.JsonDriver.parse
import swc.drivers.JsonDriver.toTruck
import swc.entities.Truck

object AzureDriver {
    object Constants {
        const val TRUCK_DT_MODEL_ID = "dtmi:swc:Truck;1"
        const val DT_SERVICE_ENDPOINT = "https://test-instance.api.wcus.digitaltwins.azure.net/"
    }

    object Authentication {
        val client = authenticate { AzureCliCredentialBuilder().build() }

        private fun authenticate(builder: () -> TokenCredential): DigitalTwinsClient = DigitalTwinsClientBuilder()
            .credential(builder())
            .endpoint(Constants.DT_SERVICE_ENDPOINT)
            .buildClient()
    }

    object DigitalTwins {
        fun getTruck(truckId: String): Truck =
            parse(Authentication.client.getDigitalTwin(truckId, String::class.java)).toTruck()
    }
}
