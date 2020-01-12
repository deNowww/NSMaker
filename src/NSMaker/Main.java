package NSMaker;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Main extends Application {
    public static Stage stage;
    private static FXMLLoader loader;
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        loader = new FXMLLoader();
        Parent root;
        if (stringContainsItemFromList(System.getProperty("os.name").toLowerCase(), Arrays.asList("osx", "os x", "macos"))) {
            root = loader.load(getClass().getResource("Main-MacOS.fxml").openStream());
        } else {
            root = loader.load(getClass().getResource("Main-WinLinux.fxml").openStream());
        }
        Document.create();
        stage.setTitle("Untitled - NSMaker");
        Scene scene = new Scene(root, 1200, 750);
        scene.getStylesheets().addAll(this.getClass().getResource("Main.css").toExternalForm());
        stage.setScene(scene);
        stage.setOnCloseRequest(areYouSureHandler);
        stage.show();
    }
    
    private static boolean stringContainsItemFromList(String inputStr, List<String> items) {
        return items.parallelStream().anyMatch(inputStr::contains);
    }
    
    private EventHandler<WindowEvent> areYouSureHandler = event -> {
        if (Document.isSaved()) {
            Platform.exit();
            return;
        }
        Alert closeConfirmation = new Alert(Alert.AlertType.CONFIRMATION, "Any unsaved work will be lost.");
        closeConfirmation.setHeaderText("Are you sure you want to exit?");
        closeConfirmation.initModality(Modality.APPLICATION_MODAL);
        closeConfirmation.initOwner(stage);
        Button exitButton = (Button) closeConfirmation.getDialogPane().lookupButton(ButtonType.OK);
        exitButton.setText("Exit");
    
        Optional<ButtonType> closeResponse = closeConfirmation.showAndWait();
        if (ButtonType.OK.equals(closeResponse.get())) {
            Platform.exit(); // for some reason the application wouldn't quit properly without this
        } else {
            event.consume();
        }
    };
    
    public static Controller getController() {
        return loader.getController();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
