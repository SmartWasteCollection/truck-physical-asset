package swc.entities

private const val MAX_VOLUME_THRESHOLD: Double = 95.0

data class Dumpster(
        val id: String,
        val dumpsterType: TypeOfDumpster,
        var open: Boolean = false,
        val occupiedVolume: Volume,
        val working: Boolean = true,
) {
    fun available(): Boolean =
            this.working && this.occupiedVolume
                    .getOccupiedPercentage(this.dumpsterType.size.capacity) < MAX_VOLUME_THRESHOLD
}

data class TypeOfDumpster(
        val size: Size,
        val typeOfOrdinaryWaste: TypeOfWaste<Waste>
)

data class Size(
        val dimension: Dimension,
        val capacity: Double,
)

enum class Dimension {
    SMALL, LARGE
}
