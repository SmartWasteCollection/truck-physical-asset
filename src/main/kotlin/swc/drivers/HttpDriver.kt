package swc.drivers

import swc.drivers.JsonDriver.parseArray
import swc.drivers.JsonDriver.toCollectionPoint
import swc.drivers.JsonDriver.toMission
import swc.entities.CollectionPoint
import swc.entities.Mission
import swc.entities.Position
import swc.entities.Waste
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object HttpDriver {
    private val client: HttpClient = HttpClient.newHttpClient()
    private const val missionURL: String = "https://localhost:3000/missions"
    private const val collectionPointURL: String = "https://localhost:3000/collectionpoints"
    private val routeURL: ((CollectionPoint, CollectionPoint) -> String) = { point1: CollectionPoint, point2: CollectionPoint ->
        "https://graphhopper.com/api/1/route?point="+ point1.position.latitude+","+point1.position.longitude+
                "&point="+ point2.position.latitude+","+point2.position.longitude+
                "&vehicle=car&debug=true&key=e21b15b7-4caa-4a56-9239-73d3753b93d2&type=json&points_encoded=false"
    }

    fun getMission(truckId: String): Mission<Waste>? {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(missionURL))
            .header("accept", "application/json")
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return parseArray(response.body())
            .map { it.asJsonObject.toMission() }
            .filter { !it.isCompleted() }
            .find { it.truckId == truckId }
    }

    fun getCollectionPoints(): List<CollectionPoint> {
        val request = HttpRequest.newBuilder()
                .uri(URI.create(collectionPointURL))
                .header("accept", "application/json")
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return parseArray(response.body()).map { it.asJsonObject.toCollectionPoint() }
    }

    fun getRoute(point1: CollectionPoint, point2: CollectionPoint): List<Position> {
        val request = HttpRequest.newBuilder()
                .uri(URI.create(routeURL(point1, point2)))
                .header("accept", "application/json")
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return arrayListOf()
    }
}
