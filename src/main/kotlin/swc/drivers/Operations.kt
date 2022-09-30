package swc.drivers

import kotlinx.coroutines.delay
import swc.drivers.AzureDriver.DigitalTwins.updateLatitude
import swc.drivers.AzureDriver.DigitalTwins.updateLongitude
import swc.drivers.AzureDriver.DigitalTwins.updateVolume
import swc.drivers.HttpDriver.completeMissionStep
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

    fun simulateStep(mission: Mission<Waste>?, index: Int, positions: List<Position>, truck: Truck) {
        positions.forEach {
            mission?.truckId?.let { t ->
                updateLatitude(t, it.latitude)
                updateLongitude(t, it.longitude)
            }
            println("Moving to ${it.latitude}:${it.longitude} ...")
            Thread.sleep(1000)
        }
        println("Raggiunto il Collection Point ${mission?.missionSteps?.get(index)?.stepId}")
        mission?.missionSteps
            ?.get(index)
            ?.stepId
            ?.let { getDumpstersInCollectionPoint(it) }
            ?.filter { it.dumpsterType.typeOfOrdinaryWaste.wasteName == mission.typeOfWaste.wasteName }
            ?.forEach { d ->
                mission.truckId?.let { t -> updateVolume(t, truck.occupiedVolume.value + d.occupiedVolume.value) }
                emptyDumpster(d.id)
                println("Svuotato il Dumpster ${d.id}")
            }
        completeMissionStep(mission!!.missionSteps[index].stepId)
        Thread.sleep(1000)
    }
}
