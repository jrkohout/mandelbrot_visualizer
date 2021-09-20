package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class PaletteTester extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        int numColors = 1000;
        WritableImage paletteStrip = new WritableImage(numColors, 50);

        // Change name here to test different palettes
        String paletteName = "pastel";

        Color[] palette = Palette.get(paletteName, numColors);
        PixelWriter pixelWriter = paletteStrip.getPixelWriter();

        for (int i = 0; i < paletteStrip.getWidth(); i++) {
            for (int j = 0; j < paletteStrip.getHeight(); j++) {
                pixelWriter.setColor(i, j, palette[i]);
            }
        }

        ImageView paletteView = new ImageView(paletteStrip);
        Pane pane = new Pane(paletteView);
        primaryStage.setScene(new Scene(pane));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
