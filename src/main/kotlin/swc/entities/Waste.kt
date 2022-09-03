package swc.entities

sealed interface Waste

enum class OrdinaryWaste : Waste {
    UNSORTED, PLASTICS_ALUMINIUM, ORGANIC, GLASS, PAPER
}

enum class ExtraordinaryWaste : Waste {
    TWIGS, WASTE_OIL, IRON, ELECTRONICS, CLOTHES, OTHER
}

data class TypeOfWaste<out T : Waste>(val wasteName: T, val wasteColor: WasteColor)

enum class WasteColor {
    GREEN, YELLOW, BLUE, BROWN, GREY, NONE
}

fun String.toWaste(): Waste = when (this) {
    "PAPER", "PLASTICS/ALUMINIUM", "ORGANIC", "GLASS", "UNSORTED" -> this.toOrdinaryWaste()
    else -> this.toExtraordinaryWaste()
}

fun String.toOrdinaryWaste(): OrdinaryWaste = when (this.uppercase()) {
    "PAPER" -> OrdinaryWaste.PAPER
    "PLASTICS/ALUMINIUM" -> OrdinaryWaste.PLASTICS_ALUMINIUM
    "ORGANIC" -> OrdinaryWaste.ORGANIC
    "GLASS" -> OrdinaryWaste.GLASS
    else -> OrdinaryWaste.UNSORTED
}

fun String.toExtraordinaryWaste(): ExtraordinaryWaste = when (this.uppercase()) {
    "ELECTRONICS" -> ExtraordinaryWaste.ELECTRONICS
    "IRON" -> ExtraordinaryWaste.IRON
    "CLOTHES" -> ExtraordinaryWaste.CLOTHES
    "TWIGS" -> ExtraordinaryWaste.TWIGS
    "WASTE_OIL" -> ExtraordinaryWaste.WASTE_OIL
    else -> ExtraordinaryWaste.OTHER
}

fun String.toWasteColor(): WasteColor = when (this) {
    "PAPER" -> WasteColor.BLUE
    "UNSORTED" -> WasteColor.GREY
    "ORGANIC" -> WasteColor.BROWN
    "GLASS" -> WasteColor.GREEN
    "PLASTICS_ALUMINIUM" -> WasteColor.YELLOW
    else -> WasteColor.NONE
}
