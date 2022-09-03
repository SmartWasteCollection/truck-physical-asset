package swc

import swc.view.TruckGUI
import java.awt.EventQueue

var CURRENT_TRUCK: String = ""

fun main(args: Array<String>) {
    if (args.size != 1) {
        return
    }
    CURRENT_TRUCK = args[0]
    EventQueue.invokeLater(TruckGUI::createAndShowGUI)
}
