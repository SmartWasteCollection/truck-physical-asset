package swc.entities

import java.time.LocalDate

data class Mission<out T : Waste>(
    val missionId: String,
    var truckId: String? = null,
    val date: LocalDate = LocalDate.now(),
    val typeOfWaste: TypeOfWaste<T>,
    val typeOfMission: TypeOfMission,
    val missionSteps: List<MissionStep>
) {
    fun isCompleted(): Boolean = missionSteps.all { it.completed }
}

enum class TypeOfMission {
    ORDINARY, EXTRAORDINARY
}

data class MissionStep(val stepId: String, var completed: Boolean = false)

fun String.toTypeOfMission(): TypeOfMission = when (this) {
    "ORDINARY" -> TypeOfMission.ORDINARY
    else -> TypeOfMission.EXTRAORDINARY
}
