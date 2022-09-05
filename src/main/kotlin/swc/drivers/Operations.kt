package swc.drivers

import swc.entities.Truck
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Operations {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun getTruck(truckId: String): CompletableFuture<Truck> = CompletableFuture<Truck>().also {
        executor.submit {
            it.complete(AzureDriver.DigitalTwins.getTruck(truckId))
        }
    }
}
