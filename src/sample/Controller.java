package sample;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class Controller {

    @FXML
    private Label realRenderWidthLabel;
    @FXML
    private Label realRenderIterationsLabel;
    @FXML
    private Label realRenderPaletteLabel;
    @FXML
    private ChoiceBox<String> renderPaletteSelect;
    @FXML
    private Label realPaletteLabel;
    @FXML
    private ToolBar toolBar0;
    @FXML
    private ToolBar toolBar1;
    @FXML
    private ToolBar toolBar2;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Label realViewWidthLabel;
    @FXML
    private Label realViewIterationsLabel;
    @FXML
    private Label realNumThreadsLabel;
    @FXML
    private TextField numThreadsField;
    @FXML
    private TextField renderIterationsField;
    @FXML
    private TextField renderWidthField;
    @FXML
    private TextField viewIterationsField;
    @FXML
    private TextField viewWidthField;
    @FXML
    private ChoiceBox<String> viewPaletteSelect;
    @FXML
    private Rectangle zoomBox;
    @FXML
    private Pane mandelPane;
    private WritableImage mainMandelImage;
    @FXML
    private ImageView mandelView;


    private int mainMaxIterations;
    private int renderMaxIterations;

    private double zoomBoxScale;

    // todo total window dimension customization
    private double viewWidth;
    private double viewHeight;

    private double renderWidth;
    private double renderHeight;

    private final double minZoomScale = 0.03;
    private final double maxZoomScale = 0.4;

    private Color[] mainPalette;
    private Color[] renderPalette;

    private Stack<MandelZoom> mandelZooms;

    private boolean[] threadsRendering;
    private boolean isRendering;

    double numberOfThreads;

    @FXML
    private void initialize() {
        mandelZooms = new Stack<>();
        mandelZooms.push(new MandelZoom(-2.5, 1, -1, 1));

        viewPaletteSelect.getItems().addAll("palette1", "grayscale", "reverse_grayscale", "warm", "pastel");
        renderPaletteSelect.getItems().addAll("palette1", "grayscale", "reverse_grayscale", "warm", "pastel");
        viewPaletteSelect.setValue("palette1");
        renderPaletteSelect.setValue("palette1");

        viewWidthField.setText("1200");
        viewIterationsField.setText("1000");
        updateViewSettings();

        renderWidthField.setText("1920");
        renderIterationsField.setText("1000");
        updateRenderSettings(null);

        // todo allow customization of zoombox color
        zoomBoxScale = 1.0 / 10;
        zoomBox.setWidth(viewWidth * zoomBoxScale);
        zoomBox.setHeight(viewHeight * zoomBoxScale);
        zoomBox.toFront();

        numThreadsField.setText("8");
        handleApplyNumThreads(null);

        generateMandelbrot(mainMandelImage, mandelZooms.peek(), mainMaxIterations, mainPalette);
    }

    // todo add more palettes
    // todo add support for alpha values so we can get some transparency going on
    // todo also look at color distribution, the linear method makes the program look outdated
    // todo add some kind of small icon view of the palette to select from
    // todo may want to load all the palettes in the beginning of the program instead of when we set the palette




    // todo add a cancellation feature to the threads that calculate everything

    //todo keep track of total zoom, if we go past a certain point, maybe switch algorithms or use a
    // different structure instead of doubles that keep more digits.

    private void generateMandelbrot(WritableImage writableImage, MandelZoom mandelZoom, int maxIters, Color[] palette) {
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        // default range x: -2.5, 1
        // default range y: -1, 1

        double width = writableImage.getWidth();
        double height = writableImage.getHeight();

        double minx = mandelZoom.getMinX();
        double maxx = mandelZoom.getMaxX();
        double miny = mandelZoom.getMinY();
        double maxy = mandelZoom.getMaxY();

        double rangex = maxx - minx;
        double rangey = maxy - miny;

        isRendering = true;
        threadsRendering = new boolean[(int)numberOfThreads];
        Arrays.fill(threadsRendering, true);

        ArrayList<Task<Void>> tasks = new ArrayList<>();

        for (int t = 0; t < numberOfThreads; t++) {
            int finalT = t;
            tasks.add(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    for (int i = finalT * (int) (width / numberOfThreads); i < (finalT + 1) * width / numberOfThreads; i++) {
                        Color[] columnColors = new Color[(int) height];
                        for (int j = 0; j < height; j++) {
                            // todo declare these variables outside the loop for performance?
                            double x0 = i * (rangex / width) + minx;
                            double y0 = j * (rangey / height) + miny;
                            double x = 0.0;
                            double y = 0.0;
                            int iter = 0;
                            double xtemp;
                            while (x * x + y * y <= 2 * 2 && iter < maxIters) {
                                xtemp = x * x - y * y + x0;
                                y = 2 * x * y + y0;
                                x = xtemp;
                                iter++;
                            }
                            columnColors[j] = palette[iter - 1];
                        }
                        final int finalI = i;
                        // todo think of a possible better way of transferring this information (do it in larger chunks)
                        Color[] finalColumnColors = Arrays.copyOf(columnColors, columnColors.length);
                        Platform.runLater(() -> {
                            for (int j = 0; j < height; j++) {
                                pixelWriter.setColor(finalI, j, finalColumnColors[j]);
                            }
                        });
                    }

                    updateRenderingStatus(false, finalT);
                    return null;
                }
            });
        }

        for (Task<Void> task : tasks) {
            new Thread(task).start();
        }

        // not sure what this does, possibly does the cancelling or progress properties or something
        // todo might need to implement this for elegant cancellation ***
        //test.setDaemon(true);

    }

    @FXML
    private void handleMouseClick(MouseEvent mouseEvent) {
        // check first to see if the scene is still rendering
        // todo move this check somewhere else?
        if (!isRendering) {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {

                double width = mainMandelImage.getWidth();
                double height = mainMandelImage.getHeight();

                MandelZoom previousMandelZoom = mandelZooms.peek();
                double previousMinX = previousMandelZoom.getMinX();
                double previousMaxX = previousMandelZoom.getMaxX();
                double previousMinY = previousMandelZoom.getMinY();
                double previousMaxY = previousMandelZoom.getMaxY();
                // minx = tlx / width * (1 - -2.5) + -2.5
                double minx = mouseEvent.getX() / width * (previousMaxX - previousMinX) + previousMinX;
                double maxx = zoomBoxScale * (previousMaxX - previousMinX) + minx;

                // miny = tly / height * (1 - -1) + -1
                double miny = mouseEvent.getY() / height * (previousMaxY - previousMinY) + previousMinY;
                double maxy = zoomBoxScale * (previousMaxY - previousMinY) + miny;

                MandelZoom nextZoom = new MandelZoom(minx, maxx, miny, maxy);
                mandelZooms.push(nextZoom);
                generateMandelbrot(mainMandelImage, nextZoom, mainMaxIterations, mainPalette);
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                if (mandelZooms.size() > 1) {
                    MandelZoom poppedZoom = mandelZooms.pop();
                    generateMandelbrot(mainMandelImage, mandelZooms.peek(), mainMaxIterations, mainPalette);
                }
            }
        }
    }

    @FXML
    private void handleMouseMoved(MouseEvent mouseEvent) {
        zoomBox.setLayoutX(mouseEvent.getX());
        zoomBox.setLayoutY(mouseEvent.getY());
    }

    @FXML
    private void handleMouseExited(MouseEvent mouseEvent) {
        zoomBox.setVisible(false);
    }

    @FXML
    private void handleMouseEntered(MouseEvent mouseEvent) {
        zoomBox.setVisible(true);
    }

    @FXML
    public void resetToDefaultView(ActionEvent actionEvent) {
        // todo move this check somewhere else?
        if (!isRendering) {
            // clears zoom history
            mandelZooms.clear();
            // pushes default zoom to stack
            // default values: minX=-2.5, maxX=1.0, minY=-1.0, maxY=1.0
            // these provide a nice, whole view of the Mandelbrot set
            mandelZooms.push(new MandelZoom(-2.5, 1, -1, 1));
            generateMandelbrot(mainMandelImage, mandelZooms.peek(), mainMaxIterations, mainPalette);
        }
    }

    @FXML
    private void handleScroll(ScrollEvent scrollEvent) {
        double newScale = zoomBoxScale + scrollEvent.getDeltaY() / viewHeight;
        if (newScale > minZoomScale && newScale < maxZoomScale) {
            zoomBoxScale = newScale;
            zoomBox.setWidth(viewWidth * zoomBoxScale);
            zoomBox.setHeight(viewHeight * zoomBoxScale);
        }
    }

    /**
     * Takes in a boolean and an index, update the threadsRendering array, then check
     * if any are still true. Updates a global variable so that whenever we want to know if
     * it is still rendering, we just check that variable instead of checking the whole array each time.
     * @param threadIsRendering
     * @param index
     */
    private void updateRenderingStatus(boolean threadIsRendering, int index) {
        threadsRendering[index] = threadIsRendering;
        for (boolean b : threadsRendering) {
            if (b) return;
        }
        isRendering = false;
    }

    private void updateViewSettings() {
        int newMaxIterations = 0;
        int newWidth = 0;

        // todo use event filter to only allow numbers to be entered in these boxes (also have a min and max)

        try {
            newMaxIterations = Integer.parseInt(viewIterationsField.getText());
            // todo extend maxIterations range when cancellation is possible
            if (newMaxIterations < 1 || newMaxIterations > 20000) {
                newMaxIterations = 0;
                throw new IllegalArgumentException("Must be between 1 and 20,000 inclusive.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error parsing iteration number.");
        }

        try {
            newWidth = Integer.parseInt(viewWidthField.getText());
            // todo create better range for these numbers
            if (newWidth < 100 || newWidth > 4000) {
                newWidth = 0;
                throw new IllegalArgumentException("Must be between 100 and 4,000 inclusive.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error parsing width.");
        }

        if (newMaxIterations != 0) {
            mainMaxIterations = newMaxIterations;
            realViewIterationsLabel.setText(String.valueOf(mainMaxIterations));
        }

        if (newWidth != 0) {
            viewWidth = newWidth;
            viewHeight = 2 / 3.5 * viewWidth;
            realViewWidthLabel.setText(String.valueOf(viewWidth));
        }

        mainPalette = Palette.get(viewPaletteSelect.getValue(), mainMaxIterations);
        realPaletteLabel.setText(viewPaletteSelect.getValue());

        Main.getStage().setWidth(viewWidth);
        // FIXME heights of bars are null for some reason
        //Main.getStage().setHeight(viewHeight + menuBar.getHeight() + toolBar0.getHeight() + toolBar1.getHeight() + toolBar2.getHeight());
        Main.getStage().setHeight(viewHeight + 168.0);

        mainMandelImage = new WritableImage((int) viewWidth, (int) viewHeight);
        mandelView.setImage(mainMandelImage);
    }

    @FXML
    private void handleUpdateView() {
        updateViewSettings();
        generateMandelbrot(mainMandelImage, mandelZooms.peek(), mainMaxIterations, mainPalette);
    }

    @FXML
    private void handleApplyNumThreads(ActionEvent actionEvent) {
        int newThreads = 0;
        try {
            newThreads = Integer.parseInt(numThreadsField.getText());
            if (newThreads < 1 || newThreads > 32) {
                throw new IllegalArgumentException("Must be between 1 and 32 inclusively.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Incorrect formatting for number of threads. Must be an integer between 1 and 32 inclusively.");
        }
        if (newThreads != 0) {
            this.numberOfThreads = newThreads;
            realNumThreadsLabel.setText(String.valueOf((int)numberOfThreads));
        }
    }

    @FXML
    private void saveView(ActionEvent actionEvent) {
        // todo add option to render current view at a higher resolution (will have to
        //  generate the view again on a bigger canvas (create bigger canvas and pass that into the
        //  generate mandelbrot method with an appropriate MandelZoom object)), allow a different
        //  palette to be selected, larger MAX_ITERATIONS, and other features
        // todo add a dialogue that allows the user to input settings at the save request
        // todo have this run as as separate thread, will need to change the isRendering system to be dependent
        //  on what exactly it is rendering so that you can continue using the application as it is saving the file.
        FileChooser saver = new FileChooser();
        saver.setTitle("Save Image");
        saver.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        File file = saver.showSaveDialog(Main.getStage());

        if (file != null) {
            WritableImage newRender = new WritableImage((int)renderWidth, (int)renderHeight);
            generateMandelbrot(newRender, mandelZooms.peek(), renderMaxIterations, renderPalette);

            try {
                while (isRendering) {
                    System.out.println("Still rendering, waiting a little.");
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                // i don't know what to put here, this is probably not the best way to do this anyway.
            }

//            ImageView testView = new ImageView(newRender);
//            Stage testStage = new Stage();
//            testStage.setScene(new Scene(new Pane(testView)));
//            testStage.show();

            //  Solution: Queue the saving image in runLater() -- it preserves order
            //  Also: Rework how I handle Platform.runLater() -- might need a separate render method
            Platform.runLater(() -> {
                BufferedImage fromFX = SwingFXUtils.fromFXImage(newRender, null);
                try {
                    ImageIO.write(fromFX, "png", file);
                } catch (IOException e) {
                    System.out.println("Could not save image");
                }
            });

        }

    }
    @FXML
    private void updateRenderSettings(ActionEvent actionEvent) {
        int newRenderIterations = 0;
        int newWidth = 0;
        // todo use event filter to only allow numbers to be entered in these boxes (also have a min and max)
        try {
            newRenderIterations = Integer.parseInt(renderIterationsField.getText());
            // todo extend maxIterations range when cancellation is possible
            if (newRenderIterations < 1 || newRenderIterations > 20000) {
                newRenderIterations = 0;
                throw new IllegalArgumentException("Must be between 1 and 20,000 inclusive.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error parsing iteration number.");
        }

        try {
            newWidth = Integer.parseInt(renderWidthField.getText());
            // todo create better range for these numbers
            if (newWidth < 100 || newWidth > 4000) {
                newWidth = 0;
                throw new IllegalArgumentException("Must be between 100 and 4,000 inclusive.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error parsing width.");
        }

        if (newRenderIterations != 0) {
            renderMaxIterations = newRenderIterations;
            realRenderIterationsLabel.setText(String.valueOf(renderMaxIterations));
        }

        if (newWidth != 0) {
            renderWidth = newWidth;
            renderHeight = 2 / 3.5 * renderWidth;
            realRenderWidthLabel.setText(String.valueOf(renderWidth));
        }

        renderPalette = Palette.get(renderPaletteSelect.getValue(), renderMaxIterations);
        realRenderPaletteLabel.setText(renderPaletteSelect.getValue());
    }
}
