package swc.drivers

import kotlinx.coroutines.delay
import swc.drivers.AzureDriver.DigitalTwins.updateLatitude
import swc.drivers.AzureDriver.DigitalTwins.updateLongitude
import swc.drivers.AzureDriver.DigitalTwins.updateVolume
import swc.drivers.HttpDriver.emptyDumpster
import swc.drivers.HttpDriver.getDumpstersInCollectionPoint
import swc.entities.Mission
import swc.entities.Position
import swc.entities.Truck
import swc.entities.Waste
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

    suspend fun simulateStep(mission: Mission<Waste>?, index: Int, positions: List<Position>) {
        positions.forEach {
            mission?.truckId?.let { t ->
                updateLatitude(t, it.latitude)
                updateLongitude(t, it.longitude)
            }
            delay(2000)
        }
        val dumpsters = mission?.missionSteps
            ?.get(index)
            ?.stepId
            ?.let { getDumpstersInCollectionPoint(it) }
        dumpsters?.filter { it.dumpsterType.typeOfOrdinaryWaste.wasteName == mission.typeOfWaste.wasteName }
            ?.forEach { d ->
                mission.truckId?.let { t -> updateVolume(t, d.occupiedVolume.value) }
                emptyDumpster(d.id)
            }
        delay(2000)
    }
}
