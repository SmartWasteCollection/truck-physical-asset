package swc.view

import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel

object TruckGUI {
    class TruckFrame(title: String) : JFrame() {
        init {
            this.title = title
            this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            this.size = Dimension(800, 600)
            this.setLocationRelativeTo(null)
            this.add(TruckPanel(this))
        }
    }

    class TruckPanel(frame: JFrame) : JPanel() {
        init {
            this.layout = BorderLayout()
            this.add(JLabel("Truck Panel"), BorderLayout.NORTH)
        }
    }

    fun createAndShowGUI() = TruckFrame("Truck Manager Window").also { it.isVisible = true }
}
