import javafx.application.Application
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.jvm.optionals.getOrNull
import kotlin.system.exitProcess


class Main : Application() {

    private val homeRoot = File("${System.getProperty("user.dir")}/test/")
    private var nowDirectory = File("")
    private var dirFiles: Array<out File> = arrayOf()
    private var chosenFile = File("")
    private var listItems = FXCollections.observableArrayList<String>()
    private var statusLabel = Label()
    private var imagelist = ListView<String>()

    override fun start(primaryStage: Stage?) {

        // Create menu items
        val menubar = MenuBar().apply {
            prefWidth = 700.0

        }

        //widget

        val vBox = VBox()
        val fileMenu = Menu("File")
        val fileNew = MenuItem("New")
        val fileOpen = MenuItem("Open")
        val fileQuit = MenuItem("Quit")
        fileMenu.items.addAll(fileNew, fileOpen, fileQuit)
        //actions
        val actionMenu = Menu("Actions")
        val actRename = MenuItem("Rename")
        val actMove = MenuItem("Move")
        val actDelete = MenuItem("Delete")
        actionMenu.items.addAll(actRename, actMove, actDelete)
        //view and options
        val viewMenu = Menu("View")
        val viewEnter = MenuItem("Enter")
        val viewPrev = MenuItem("Prev")
        val viewHome = Menu("Home")
        viewMenu.items.addAll(viewEnter,viewPrev,viewHome)
        val optionMenu = Menu("Options")
        // Map accelerator keys to menu items
        fileNew.accelerator = KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN)
        fileOpen.accelerator = KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN)
        fileQuit.accelerator = KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN)
        actRename.accelerator = KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN)
        actMove.accelerator = KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN)
        actDelete.accelerator = KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN)
        viewEnter.accelerator = KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN)
        viewPrev.accelerator = KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN)
        viewHome.accelerator = KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN)
        //put menu together
        menubar.menus.addAll(fileMenu, actionMenu, viewMenu, optionMenu)

        // Setup handlers
        fileNew.setOnAction { println("File-New") }
        fileOpen.setOnAction { println("File-Open") }
        fileQuit.setOnAction {
            println("File-Quit")
            exitProcess(0)
        }
        actRename.setOnAction { renameFile(primaryStage) }
        actMove.setOnAction { moveTo(primaryStage) }
        viewHome.setOnAction { returnHome() }
        viewPrev.setOnAction { prevButtonClicked() }
        actDelete.setOnAction { deleteFile() }

        //initialize var
        statusLabel.prefWidthProperty().bind(vBox.widthProperty())
        initialize()

        //////////////////////////////////////////////////////////////////////////////////////////
        //create image list
        //widget size
        imagelist.prefWidth = 150.0
        imagelist.prefHeight = 330.0
        // update the imageList
        updateListView(imagelist, listItems)
        // Select the first item; you don't want an empty selection
        imagelist.selectionModel.selectIndices(0)

        //set vBox layout
        vBox.children.add(statusLabel)
        vBox.children.add(imagelist)

//        imagelist.selectionModel.selectedItemProperty().addListener(itemsListener())

        //////////////////////////////////////////////////////////////////////////////////////////
        //create tool bar
        val toggles = ToggleGroup().apply {

        }
        val homeButton = ToggleButton("Home")
        val prevButton = ToggleButton("Prev")
        val nextButton = ToggleButton("Next")
        val deleteButton = ToggleButton("delete")
        val renameButton = ToggleButton("Rename")
        val moveButton = ToggleButton("Move")
        toggles.toggles.addAll(homeButton, prevButton, nextButton, deleteButton, renameButton, moveButton)
        // setup the layout
        val tilePane = TilePane()
        tilePane.children.add(homeButton)
        tilePane.children.add(prevButton)
        tilePane.children.add(nextButton)
        tilePane.children.add(deleteButton)
        tilePane.children.add(renameButton)
        tilePane.children.add(moveButton)
        //set button event
        homeButton.setOnMouseClicked { returnHome() }
        prevButton.setOnMouseClicked { prevButtonClicked() }
        moveButton.setOnMouseClicked {  moveTo(primaryStage)}
        renameButton.setOnMouseClicked { renameFile(primaryStage) }
        deleteButton.setOnMouseClicked { deleteFile() }
        //////////////////////////////////////////////////////////////////////////////////////////
        // create panels
        val leftPane = Pane().apply {
            prefWidth = 150.0
            background = Background(BackgroundFill(Color.valueOf("#ff00ff"), null, null))
            setOnMouseClicked { println("left pane clicked") }
            children.add(vBox)
        }

        val topPane = Pane().apply {
            prefHeight = 55.0
            //background = Background(BackgroundFill(Color.valueOf("#00ff00"), null, null))
            setOnMouseClicked { println("top pane clicked") }
            val topBars = VBox(menubar, tilePane).apply {
                alignment = Pos.CENTER

            }
            children.add(topBars)
        }

        val centrePane = Pane().apply {
            prefWidth = 100.0
            background = Background(BackgroundFill(Color.valueOf("#F0FFFF"), null, null))
            setOnMouseClicked { println("centre pane clicked") }
        }

        nextButton.setOnMouseClicked { nextButtonClicked(centrePane) }
        viewEnter.setOnAction { nextButtonClicked(centrePane) }
        // put the panels side-by-side in a container
        val root = BorderPane().apply {
            left = leftPane
            center = centrePane
            top = topPane

        }

        //set
        imagelist.setOnMouseClicked { handleListViewItemClick(it, imagelist, centrePane) }
        imagelist.setOnKeyPressed { handleListViewKeyPress(it, imagelist, centrePane) }

        // create the scene and show the stage
        with(primaryStage!!) {
            scene = Scene(root, 600.0, 400.0)
            title = "A1"
            show()
        }
    }

    private fun itemsListener() {
        val selectIndex = imagelist.selectionModel.selectedIndex
        chosenFile = dirFiles[selectIndex]
    }

    // move file
    private fun moveTo(primaryStage: Stage?) {
        if (chosenFile.exists()) {
            val dirChooser = DirectoryChooser()
            dirChooser.title = "Move to"
            val selectedDirectory = dirChooser.showDialog(primaryStage)
            if (selectedDirectory != null) {
                println("move to: ${selectedDirectory.absolutePath}")
                chosenFile.renameTo(File(selectedDirectory.absolutePath, chosenFile.name))

            } else {
                println("no directory chosen")
            }
        } else {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Error"
            alert.headerText = null
            alert.contentText = "Source file doesn't exist"
            alert.showAndWait()
        }

    }


    private fun updateListView(view: ListView<String>, items: ObservableList<String>) {
        items.clear()
        dirFiles = nowDirectory.listFiles()!!
        val filePaths: MutableList<String> = mutableListOf()
        for (file in dirFiles) {
            filePaths.add(file.name)
        }
        listItems.addAll(filePaths)
        view.items = listItems
        view.refresh()
        print(filePaths)
    }

    //initialize the primaryStage
    private fun initialize() {
        nowDirectory = homeRoot
        dirFiles = homeRoot.listFiles()!!
//        val relativePath = nowDirectory.relativeTo(homeRoot).path
        statusLabel.text = "./"
        statusLabel.background =
            Background(BackgroundFill(Color.WHITE, CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY))
    }

    private fun updateStatusLabel() {
        val relativePath = chosenFile.relativeTo(this.homeRoot).path.replace('\\', '/')
        if (chosenFile.isDirectory) {
            if(chosenFile == homeRoot)
                statusLabel.text = "./"
            else
                statusLabel.text = "./$relativePath/"
        } else {
            statusLabel.text = "./$relativePath"
        }
    }

    private fun handleListViewItemClick(event: MouseEvent, listView: ListView<String>, pane: Pane) {
        val selectedIndex = listView.selectionModel.selectedIndex
        chosenFile = dirFiles[selectedIndex]
        updateStatusLabel()
        if ((event.button == MouseButton.PRIMARY) && ((event.clickCount == 2))) {
            displayFile(chosenFile, pane)
        }
    }

    private fun handleListViewKeyPress(event: KeyEvent, listView: ListView<String>, pane: Pane) {
        println(event.code)
        if (event.code == KeyCode.UP || event.code == KeyCode.DOWN) {
            val selectedIndex = listView.selectionModel.selectedIndex
            chosenFile = dirFiles[selectedIndex]
            updateStatusLabel()
        }
        if (event.code == KeyCode.ENTER) {
            println(222)
            displayFile(chosenFile, pane)
        }
        if(event.code == KeyCode.BACK_SPACE){
            println(111)
            prevButtonClicked();
        }
    }

    //open and display file
    private fun displayFile(file: File, pane: Pane) {
        val extension = file.extension.lowercase()
        if (extension.endsWith("jpg") || extension.endsWith("png") || extension.endsWith("bmp")) {
            println(extension)
            //val image = Image(file.absolutePath)
            val stream: InputStream = FileInputStream(file.absolutePath)
            val image = Image(stream)
            val imageView = ImageView(image)

            imageView.fitWidthProperty().bind(pane.widthProperty())
            imageView.fitHeightProperty().bind(pane.heightProperty())
            //set pane's children
            pane.children.clear()
            pane.children.add(imageView)
        } else if (extension.endsWith("txt") || extension.endsWith("md")) {
            val textArea = TextArea()
            val textContent = file.readText()
            textArea.text = textContent
            textArea.isEditable = false
            textArea.prefWidthProperty().bind(pane.widthProperty())
            textArea.prefHeightProperty().bind(pane.heightProperty())

            //set pane's children
            pane.children.clear()
            pane.children.add(textArea)
            println(extension)
        } else if (file.isDirectory) {

            nowDirectory = file
            updateStatusLabel()
            updateListView(imagelist, listItems)
            pane.children.clear()
            println(file.name)
        } else {
            println("unsupported type")
        }
    }

    //return to home root
    private fun returnHome() {
        nowDirectory = homeRoot
        chosenFile = homeRoot
        updateStatusLabel()
        updateListView(imagelist, listItems)
    }

    //prev button event
    private fun prevButtonClicked() {
        if(nowDirectory != homeRoot){
            nowDirectory = nowDirectory.parentFile
            chosenFile = nowDirectory
            updateStatusLabel()
            updateListView(imagelist, listItems)
        }
    }
    private fun renameFile(primaryStage: Stage?){
            val newNameDialog = TextInputDialog()
            newNameDialog.headerText = "Input new file name"
            newNameDialog.contentText = "file name: "
            val result = newNameDialog.showAndWait()
            println(result.get().trim())
            val newfileName = result.get()
            if(chosenFile.extension != newfileName.split('.')[1] ||
                newfileName.contains('/') || newfileName.contains('\\' )
                || newfileName.contains(':') ||newfileName.contains('*')
                ||newfileName.contains('?') || newfileName.contains('"')
                || newfileName.contains('<') || newfileName.contains('>')||
                newfileName.contains('|')){
                println("invalid rename ")
            }   else {
                chosenFile.renameTo(File(nowDirectory, result.get()))
                updateStatusLabel()
                updateListView(imagelist, listItems)
            }

    }
    private fun deleteFile(){
        val confirmation = Alert(AlertType.CONFIRMATION)
        confirmation.title = "Confirmation dialog"
        confirmation.contentText = "Are you sure you want to delete it?"
        val result1 = confirmation.showAndWait()
        if (result1.isPresent) {
            when (result1.get()) {
                ButtonType.OK -> {
                    chosenFile.deleteRecursively()
                    updateStatusLabel()
                    updateListView(imagelist, listItems)
                }
                ButtonType.CANCEL -> println("CANCEL")
            }
        }
    }
    private fun nextButtonClicked(pane: Pane){
        displayFile(chosenFile, pane)
    }
}