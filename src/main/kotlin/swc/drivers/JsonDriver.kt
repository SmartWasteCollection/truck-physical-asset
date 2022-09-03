package swc.drivers

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import swc.entities.Mission
import swc.entities.MissionStep
import swc.entities.TypeOfWaste
import swc.entities.Waste
import swc.entities.toTypeOfMission
import swc.entities.toWaste
import swc.entities.toWasteColor
import java.time.LocalDate

object JsonDriver {
    object Fields {
        const val MISSION_ID: String = "missionId"
        const val TRUCK_ID: String = "truckId"
        const val DATE: String = "date"
        const val TYPE_OF_WASTE: String = "typeOfWaste"
        const val TYPE_OF_MISSION: String = "typeOfMission"
        const val MISSION_STEPS: String = "missionSteps"
        const val WASTE_NAME: String = "wasteName"
        const val WASTE_COLOR: String = "wasteColor"
        const val STEP_ID: String = "stepId"
        const val COMPLETED: String = "completed"
    }

    fun parse(s: String): JsonObject = JsonParser.parseString(s).asJsonObject

    fun parseArray(s: String): JsonArray = JsonParser.parseString(s).asJsonArray

    fun JsonObject.toMission(): Mission<Waste> = Mission(
        this[Fields.MISSION_ID].asString,
        this[Fields.TRUCK_ID].asString.orNull(),
        LocalDate.parse(this[Fields.DATE].asString),
        this[Fields.TYPE_OF_WASTE].asJsonObject.toTypeOfWaste(),
        this[Fields.TYPE_OF_MISSION].asString.toTypeOfMission(),
        this[Fields.MISSION_STEPS].asJsonArray.map { it.asJsonObject.toMissionStep() }.toList()
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
