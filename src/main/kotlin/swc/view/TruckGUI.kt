package swc.view

import com.azure.digitaltwins.core.implementation.models.ErrorResponseException
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext
import com.google.gson.Gson
import com.google.gson.JsonObject
import swc.drivers.AzureDriver
import swc.drivers.HttpDriver.getCollectionPoints
import swc.drivers.HttpDriver.getMission
import swc.drivers.HttpDriver.getRoute
import swc.drivers.Operations
import swc.drivers.Operations.simulateStep
import swc.entities.Position
import swc.entities.Truck
import java.awt.*
import java.util.concurrent.Executors
import javax.swing.*

object TruckGUI {
    class TruckFrame(title: String) : JFrame() {
        init {
            this.title = title
            this.defaultCloseOperation = EXIT_ON_CLOSE
            this.size = Dimension(800, 300)
            this.setLocationRelativeTo(null)
            //this.add(SelectTruckPanel(this))
            this.add(TruckPanel(this))
        }
    }

    class CustomTextArea(content: String): JTextPane() {
        init {
            text = content
            isOpaque = true
            isEditable = false
            isFocusable = false
            font = Font("Courier", Font.BOLD, 20)
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
                        TruckPanel(frame)
                    }
                }
            }
            this.add(selectTruck)
            frame.add(this)
        }
    }

    class CustomDimension : Dimension(300, 30)

    class CustomFormElement(title: String, vararg components: JComponent) : JPanel() {
        init {
            this.layout = BoxLayout(this, BoxLayout.X_AXIS)
            this.add(CustomTextArea(title))
            components.forEach { this.add(it) }
        }
    }

    class TruckPanel(frame: JFrame) : JPanel() {
        private lateinit var truck: Truck
        private val messageProcessor: (ServiceBusReceivedMessageContext) -> Unit = {
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
        private lateinit var latitude: JSpinner
        private lateinit var longitude: JSpinner
        private lateinit var occupiedVolume: JSpinner
        private lateinit var inMission: CustomTextArea
        private lateinit var startMission: JButton
        private val disposalPointPosition = Position(latitude = 44.147413, longitude = 12.187121)
        private lateinit var contentPanel: JComponent

        init {
            layout = BorderLayout()
            val truckId = JOptionPane.showInputDialog(frame,"Insert the Truck's ID","Truck ID", JOptionPane.PLAIN_MESSAGE) as String
            try {
                truck = AzureDriver.DigitalTwins.getTruck(truckId)
                showDigitalTwin()
                frame.isVisible = true
                AzureDriver.Events.listenToEvents(this.messageProcessor)
            } catch (_: ErrorResponseException) {
                contentPanel = JTextArea("Truck Not Found")
            }
            add(contentPanel, BorderLayout.CENTER)
        }

        private fun showDigitalTwin() {
            val dtPanel = JPanel()
            val vbox = Box.createVerticalBox()
            val title = CustomTextArea("Truck Physical Asset")
            title.componentOrientation = ComponentOrientation.RIGHT_TO_LEFT
            vbox.add(title)
            vbox.add(CustomTextArea("Id: ${truck.truckId}"))

            latitude = JSpinner().also {
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

            longitude = JSpinner().also {
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
            vbox.add(CustomFormElement("Position: ", latitude, longitude))

            occupiedVolume = JSpinner().also {
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
            vbox.add(CustomFormElement("Occupied volume (L):", occupiedVolume))

            vbox.add(CustomTextArea("Capacity (L): ${truck.capacity}"))
            inMission = CustomTextArea("In mission: ${truck.isInMission}")
            vbox.add(inMission)

            startMission = JButton("Start Mission").also {
                it.isEnabled = truck.isInMission
                it.preferredSize = CustomDimension()
                it.minimumSize = CustomDimension()
                it.maximumSize = CustomDimension()
                it.font = Font("Courier", Font.BOLD, 20)
                it.addActionListener { e ->
                    SwingUtilities.invokeLater {
                        (e.source as JButton).isEnabled = false
                        val collectionPoints = getCollectionPoints()
                        val mission = getMission(truck.truckId)
                        val steps = mission?.missionSteps
                            ?.map { collectionPoints.first { cp -> cp.id == it.stepId }.position }
                            ?.toMutableList().also { pos -> pos?.add(0, disposalPointPosition) }
                            ?.zipWithNext()
                            ?.map { p -> getRoute(p.first, p.second) }
                        Executors.newSingleThreadExecutor().execute {
                            steps?.forEachIndexed { i, s -> simulateStep(mission, i, s, truck) }
                        }
                    }
                }
            }
            vbox.add(startMission)

            dtPanel.layout = BoxLayout(dtPanel, BoxLayout.Y_AXIS)
            dtPanel.add(vbox)
            contentPanel = dtPanel

        }
    }

    fun createAndShowGUI() = TruckFrame("Truck Manager Window").also { it.isVisible = true }
}
