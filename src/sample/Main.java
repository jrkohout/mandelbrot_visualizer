package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage mainStage;
    @Override
    public void start(Stage primaryStage) throws Exception{
        mainStage = primaryStage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = fxmlLoader.load();
        mainStage.setTitle("Mandelbrot");
        mainStage.setScene(new Scene(root));
        mainStage.show();
    }

    public static Stage getStage() {
        return mainStage;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
