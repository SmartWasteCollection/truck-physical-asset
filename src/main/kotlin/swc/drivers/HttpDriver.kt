package swc.drivers

import swc.drivers.JsonDriver.parse
import swc.drivers.JsonDriver.parseArray
import swc.drivers.JsonDriver.toCollectionPoint
import swc.drivers.JsonDriver.toDumpster
import swc.drivers.JsonDriver.toGraphHopperPositions
import swc.drivers.JsonDriver.toMission
import swc.entities.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpResponse

object HttpDriver {
    private val client: HttpClient = HttpClient.newHttpClient()
    private const val domain: String = "https://localhost:3000"
    private const val missionURL: String = "$domain/missions"
    private const val collectionPointURL: String = "$domain/collectionpoints"
    private val dumpstersInCollectionPointURL: (String) -> String = { collectionPointId: String ->
        "$domain/collectionpoints/$collectionPointId/dumpsters"
    }
    private val emptyDumpsterURL: (String) -> String = { dumpsterId: String ->
        "$domain/dumpsters/volume/$dumpsterId"
    }
    private val routeURL: (Position, Position) -> String = { point1: Position, point2: Position ->
        "https://graphhopper.com/api/1/route?point=${point1.latitude},${point1.longitude}"+
                "&point=${point2.latitude},${point2.longitude}"+
                "&vehicle=car&debug=true&key=e21b15b7-4caa-4a56-9239-73d3753b93d2&type=json&points_encoded=false"
    }

    fun getMission(truckId: String): Mission<Waste>? {
        val response = sendRequest(missionURL)
        return parseArray(response.body())
            .map { it.asJsonObject.toMission() }
            .filter { !it.isCompleted() }
            .find { it.truckId == truckId }
    }

    fun getCollectionPoints(): List<CollectionPoint> {
        val response = sendRequest(collectionPointURL)
        return parseArray(response.body())
                .map { it.asJsonObject.toCollectionPoint() }
    }

    fun getRoute(point1: Position, point2: Position): List<Position> {
        val response = sendRequest(routeURL(point1, point2))
        return parse(response.body()).toGraphHopperPositions()
    }

    fun getDumpstersInCollectionPoint(collectionPointId: String): List<Dumpster> {
        val response = sendRequest(dumpstersInCollectionPointURL(collectionPointId))
        return parseArray(response.body()).map { it.asJsonObject.toDumpster() }
    }

    fun emptyDumpster(dumpsterId: String) {
        val request = HttpRequest.newBuilder()
                .uri(URI.create(emptyDumpsterURL(dumpsterId)))
                .PUT(HttpRequest.BodyPublishers.ofString("{\"volume\": 0}"))
                .header("accept", "application/json")
                .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun sendRequest(uri: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("accept", "application/json")
                .build()

        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }

}
