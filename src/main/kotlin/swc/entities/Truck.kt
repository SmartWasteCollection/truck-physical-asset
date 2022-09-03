package swc.entities

data class Truck(
    val truckId: String,
    val position: Position = Position(0.0, 0.0),
    val occupiedVolume: Volume = Volume(0.0),
    val capacity: Double = 1000.0,
    val isInMission: Boolean = false
)

data class Position(val latitude: Double, val longitude: Double)

data class Volume(val value: Double) {
    fun getOccupiedPercentage(capacity: Double): Double = value / capacity
}
