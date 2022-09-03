package swc.drivers

import swc.CURRENT_TRUCK
import swc.drivers.JsonDriver.parseArray
import swc.drivers.JsonDriver.toMission
import swc.entities.Mission
import swc.entities.Waste
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object HttpDriver {
    private val client: HttpClient = HttpClient.newHttpClient()
    private const val missionURL: String = "https://localhost:3000/missions"

    fun getMission(): Mission<Waste>? {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(missionURL))
            .header("accept", "application/json")
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return parseArray(response.body())
            .map { it.asJsonObject.toMission() }
            .filter { !it.isCompleted() }
            .find { it.truckId == CURRENT_TRUCK }
    }
}
