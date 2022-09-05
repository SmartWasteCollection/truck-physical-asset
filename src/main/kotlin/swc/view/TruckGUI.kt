package swc.view

import swc.drivers.Operations
import swc.entities.Truck
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingUtilities

object TruckGUI {
    class TruckFrame(title: String) : JFrame() {
        init {
            this.title = title
            this.defaultCloseOperation = EXIT_ON_CLOSE
            this.size = Dimension(800, 600)
            this.setLocationRelativeTo(null)
            this.add(SelectTruckPanel(this))
        }
    }

    class SelectTruckPanel(frame: JFrame) : JPanel() {
        private val truckId: JTextField = JTextField(20)
        private val selectTruck: JButton = JButton("Select Truck")

        init {
            this.layout = FlowLayout()
            this.add(JLabel("Truck ID:"))
            this.add(truckId)
            this.selectTruck.addActionListener {
                Operations.getTruck(this.truckId.text).thenApply {
                    SwingUtilities.invokeLater {
                        println("Truck: $it")
                        frame.remove(this)
                        TruckPanel(frame, it)
                    }
                }
            }
            this.add(selectTruck)
            frame.add(this)
        }
    }

    class TruckPanel(frame: JFrame, truck: Truck) : JPanel() {
        init {
            this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
            this.add(JLabel("Id: ${truck.truckId}"))
            this.add(JLabel("Position: (${truck.position.latitude}, ${truck.position.longitude})"))
            this.add(JLabel("Occupied volume: ${truck.occupiedVolume.value}L"))
            this.add(JLabel("Capacity: ${truck.capacity}L"))
            this.add(JLabel("In mission: ${truck.isInMission}"))
            frame.add(this)
            frame.isVisible = true
        }
    }

    fun createAndShowGUI() = TruckFrame("Truck Manager Window").also { it.isVisible = true }
}
