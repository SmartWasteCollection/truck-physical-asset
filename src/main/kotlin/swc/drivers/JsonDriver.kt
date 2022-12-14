package swc.drivers

import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import swc.drivers.JsonDriver.Fields.COORDINATES
import swc.drivers.JsonDriver.Fields.PATHS
import swc.drivers.JsonDriver.Fields.POINTS
import swc.drivers.JsonDriver.Fields.SMALL
import swc.drivers.JsonDriver.toTruck
import swc.entities.*
import java.time.LocalDate
import kotlin.text.Typography.dollar

object JsonDriver {
    object Fields {
        const val MISSION_ID: String = "missionId"
        const val COLLECTION_POINT_ID: String = "id"
        const val TRUCK_ID: String = "truckId"
        const val DUMPSTER_ID: String = "id"
        const val DUMPSTER_TYPE: String = "dumpsterType"
        const val SMALL: String = "SMALL"
        const val OPEN: String = "open"
        const val WORKING: String = "working"
        const val DIMENSION: String = "dimension"
        const val DATE: String = "date"
        const val PATHS: String = "paths"
        const val POINTS: String = "points"
        const val COORDINATES: String = "coordinates"
        const val TYPE_OF_WASTE: String = "typeOfWaste"
        const val TYPE_OF_MISSION: String = "typeOfMission"
        const val MISSION_STEPS: String = "missionSteps"
        const val WASTE_NAME: String = "wasteName"
        const val WASTE_COLOR: String = "wasteColor"
        const val STEP_ID: String = "stepId"
        const val COMPLETED: String = "completed"
        const val POSITION: String = "position"
        const val LATITUDE: String = "latitude"
        const val LONGITUDE: String = "longitude"
        const val OCCUPIED_VOLUME: String = "occupiedVolume"
        const val VALUE: String = "value"
        const val CAPACITY: String = "capacity"
        const val IS_IN_MISSION: String = "inMission"
        const val TRUCK_DT_ID: String = "${dollar}dtId"
    }

    fun parse(s: String): JsonObject = JsonParser.parseString(s).asJsonObject

    fun parseArray(s: String): JsonArray = JsonParser.parseString(s).asJsonArray

    fun JsonObject.toMission(): Mission<Waste> = Mission(
        this[Fields.MISSION_ID].asString,
        if (this[Fields.TRUCK_ID].isJsonNull) null else this[Fields.TRUCK_ID].asString,
        LocalDate.parse(this[Fields.DATE].asString),
        this[Fields.TYPE_OF_WASTE].asJsonObject.toTypeOfWaste(),
        this[Fields.TYPE_OF_MISSION].asString.toTypeOfMission(),
        this[Fields.MISSION_STEPS].asJsonArray.map { it.asJsonObject.toMissionStep() }.toList()
    )

    fun JsonObject.toCollectionPoint(): CollectionPoint = CollectionPoint(
        this[Fields.COLLECTION_POINT_ID].asString,
        this[Fields.POSITION].asJsonObject.toPosition()
    )

    fun JsonObject.toTruck(): Truck = Truck(
        this[Fields.TRUCK_DT_ID].asString,
        this[Fields.POSITION].asJsonObject.toPosition(),
        this[Fields.OCCUPIED_VOLUME].asJsonObject.toVolume(),
        this[Fields.CAPACITY].asDouble,
        this[Fields.IS_IN_MISSION].asBoolean
    )

    fun JsonObject.toDumpster(): Dumpster = Dumpster(
        this[Fields.DUMPSTER_ID].asString,
        TypeOfDumpster(
            Size(
                this[Fields.DUMPSTER_TYPE].asJsonObject["size"].asJsonObject[Fields.DIMENSION].asString.toDimension(),
                this[Fields.DUMPSTER_TYPE].asJsonObject["size"].asJsonObject[Fields.CAPACITY].asDouble
            ),
            this[Fields.DUMPSTER_TYPE].asJsonObject["typeOfOrdinaryWaste"].asJsonObject.toTypeOfWaste()
        ),
        this[Fields.OPEN].asBoolean,
        this[Fields.OCCUPIED_VOLUME].asJsonObject.toVolume(),
        this[Fields.WORKING].asBoolean
    )

    fun JsonObject.toGraphHopperPositions(): List<Position> =
            this.getAsJsonArray(PATHS).get(0).asJsonObject
                    .getAsJsonObject(POINTS)
                    .getAsJsonArray(COORDINATES)
                    .map { Position(it.asJsonArray[1].asDouble, it.asJsonArray[0].asDouble) }

    private fun String.toDimension(): Dimension = when (this) {
        SMALL -> Dimension.SMALL
        else -> Dimension.LARGE
    }

    private fun JsonObject.toVolume(): Volume = Volume(this[Fields.VALUE].asDouble)

    private fun JsonObject.toPosition(): Position = Position(
        this[Fields.LATITUDE].asDouble,
        this[Fields.LONGITUDE].asDouble
    )

    private fun JsonObject.toTypeOfWaste(): TypeOfWaste<Waste> = TypeOfWaste(
        this[Fields.WASTE_NAME].asString.toWaste(),
        this[Fields.WASTE_COLOR].asString.toWasteColor()
    )

    private fun JsonObject.toMissionStep(): MissionStep = MissionStep(
        this[Fields.STEP_ID].asString,
        this[Fields.COMPLETED].asBoolean
    )

    private fun String.orNull(): String? = when (this) {
        "null" -> null
        else -> this
    }
}
