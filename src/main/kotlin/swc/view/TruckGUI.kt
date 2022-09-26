package swc.view

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import swc.drivers.AzureDriver
import swc.drivers.HttpDriver.getCollectionPoints
import swc.drivers.HttpDriver.getMission
import swc.drivers.HttpDriver.getRoute
import swc.drivers.Operations
import swc.drivers.Operations.simulateStep
import swc.entities.Truck
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSpinner
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
                        frame.remove(this)
                        TruckPanel(frame, it)
                    }
                }
            }
            this.add(selectTruck)
            frame.add(this)
        }
    }

    class CustomDimension : Dimension(120, 20)

    class CustomFormElement(title: String, vararg components: JComponent) : JPanel() {
        init {
            this.layout = BoxLayout(this, BoxLayout.X_AXIS)
            this.add(JLabel(title))
            components.forEach { this.add(it) }
        }
    }

    class TruckPanel(frame: JFrame, truck: Truck) : JPanel() {
        val messageProcessor: (ServiceBusReceivedMessageContext) -> Unit = {
            SwingUtilities.invokeLater {
                val value = Gson().fromJson(it.message.body.toString(), JsonObject::class.java)["patch"]
                    .asJsonArray.first().asJsonObject
                when (value["path"].asString) {
                    "/occupiedVolume/value" -> this.occupiedVolume.value = value["value"].asDouble
                    "/position/longitude" -> this.longitude.value = value["value"].asDouble
                    "/position/latitude" -> this.latitude.value = value["value"].asDouble
                    "/inMission" -> {
                        this.inMission.text = "In mission: ${value["value"].asBoolean}"
                        this.startMission.isEnabled = value["value"].asBoolean
                    }
                    else -> println("Unknown path: ${value["path"].asString}")
                }
            }
        }

        private val id = JLabel("Id: ${truck.truckId}")
        private val position = JLabel("Position:")
        private val latitude = JSpinner().also {
            it.value = truck.position.latitude
            it.preferredSize = CustomDimension()
            it.maximumSize = CustomDimension()
            it.minimumSize = CustomDimension()
            it.addChangeListener { e ->
                AzureDriver.DigitalTwins.updateLatitude(
                    truck.truckId,
                    when ((e.source as JSpinner).value) {
                        is Int -> ((e.source as JSpinner).value as Int).toDouble()
                        else -> (e.source as JSpinner).value as Double
                    }
                )
            }
        }
        private val longitude = JSpinner().also {
            it.value = truck.position.longitude
            it.preferredSize = CustomDimension()
            it.maximumSize = CustomDimension()
            it.minimumSize = CustomDimension()
            it.addChangeListener { e ->
                AzureDriver.DigitalTwins.updateLongitude(
                    truck.truckId,
                    when ((e.source as JSpinner).value) {
                        is Int -> ((e.source as JSpinner).value as Int).toDouble()
                        else -> (e.source as JSpinner).value as Double
                    }
                )
            }
        }
        private val volume = JLabel("Occupied volume (L):")
        private val occupiedVolume = JSpinner().also {
            it.value = truck.occupiedVolume.value
            it.preferredSize = CustomDimension()
            it.maximumSize = CustomDimension()
            it.minimumSize = CustomDimension()
            it.addChangeListener { e ->
                AzureDriver.DigitalTwins.updateVolume(
                    truck.truckId,
                    when ((e.source as JSpinner).value) {
                        is Int -> ((e.source as JSpinner).value as Int).toDouble()
                        else -> (e.source as JSpinner).value as Double
                    }
                )
            }
        }
        private val capacity = JLabel("Capacity (L): ${truck.capacity}")
        private val inMission = JLabel("In mission: ${truck.isInMission}")
        private val startMission = JButton("Start Mission").also {
            it.isEnabled = false
            it.addChangeListener { e ->
                (e.source as JButton).isEnabled = false
                val collectionPoints = getCollectionPoints()
                val mission = getMission(truck.truckId)
                val steps = mission?.missionSteps
                        ?.map { collectionPoints.first { cp -> cp.id == it.stepId }.position }
                        ?.toMutableList().also { pos -> pos?.add(0, truck.position) }
                        ?.zipWithNext()
                        ?.map { p -> getRoute(p.first, p.second) }
                suspend {
                    coroutineScope {
                        launch {
                            steps?.forEachIndexed { i, s -> simulateStep(mission, i, s) }
                        }
                    }
                }
            }
        }

        init {
            this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
            this.add(this.id)
            this.add(CustomFormElement("Position:", this.latitude, this.longitude))
            this.add(CustomFormElement("Occupied volume (L):", this.occupiedVolume))
            this.add(this.capacity)
            this.add(this.inMission)
            this.add(this.startMission)
            frame.add(this)
            frame.isVisible = true
            AzureDriver.Events.listenToEvents(this)
        }
    }

    fun createAndShowGUI() = TruckFrame("Truck Manager Window").also { it.isVisible = true }
}
