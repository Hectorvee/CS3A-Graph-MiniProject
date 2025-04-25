package acsse.csc3a.miniProject;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.embed.swing.SwingNode;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class MainUI extends BorderPane {

    // Core Components
    private ImageView imageView;
    private Button btnUpload;
    private Button btnAnalyze;
    private Label lblResult;
    private Label lblSimilarCases;
    private ImageProcessor processor;

    // Graph Visualization
    private SwingNode graphSwingNode;
    private StackPane graphContainer;

    /**
     * Constructor for MainUI
     * Initializes the GUI components and layout.
     * This is the main entry point for the application.
     */
    public MainUI() {
        initGUI();
    }

    private void initGUI() {
        // Top Section: Title
        Label title = new Label("Skin Lesion Analysis System");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(20));

        // Center Section: Image & Graph
        SplitPane centerSplit = new SplitPane();

        // Left: Image + Upload
        VBox imageContainer = new VBox(10);
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(400);

        btnUpload = new Button("Upload Image");
        btnUpload.setOnAction(e -> {
            try {
                handleUpload();
            } catch (IOException ex) {
                showAlert("Error", "Failed to load image: " + ex.getMessage());
            }
        });

        imageContainer.getChildren().addAll(btnUpload, imageView);
        imageContainer.setPadding(new Insets(10));

        // Right: Graph Visualization
        graphSwingNode = new SwingNode();
        graphContainer = new StackPane();
        graphContainer.setStyle("-fx-background-color: #f0f0f0;");
        graphContainer.getChildren().add(graphSwingNode);
        centerSplit.getItems().addAll(imageContainer, graphContainer);

        centerSplit.setDividerPosition(0, 0.4); // 40% width for image

        // Bottom Section: Results
        VBox resultsBox = new VBox(15);
        lblResult = new Label("Classification: -");
        lblSimilarCases = new Label("Similar Cases: -");
        btnAnalyze = new Button("Analyze Lesion");
        btnAnalyze.setOnAction(e -> handleAnalysis());

        resultsBox.getChildren().addAll(
                new Separator(),
                lblResult,
                lblSimilarCases,
                btnAnalyze
        );
        resultsBox.setPadding(new Insets(20));

        // Assemble Layout
        this.setTop(titleBox);
        this.setCenter(centerSplit);
        this.setBottom(resultsBox);

        // Theme: Create a function for this
        theme(title, titleBox, resultsBox, imageContainer);

    }

    private void handleUpload() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            Image image = new Image(file.toURI().toString(), 400, 400, true, true);
            imageView.setImage(image);
            processor = new ImageProcessor(file);

            // Reset previous results
            lblResult.setText("Classification: -");
            lblSimilarCases.setText("Similar Cases: -");
            graphSwingNode.setContent(null);
        }
    }

    private void handleAnalysis() {
        if (processor == null) {
            showAlert("Error", "No image loaded. Please upload an image first.");
            return;
        }

        // Show loading indicator
        ProgressIndicator progress = new ProgressIndicator();
        StackPane loadingPane = new StackPane(progress);
        loadingPane.setStyle("-fx-background-color: #f0f0f0;");
        graphContainer.getChildren().set(0, loadingPane);

        lblResult.setText("Analyzing...");
        lblSimilarCases.setText("");

        // Process in background thread
        new Thread(() -> {
            try {
                processor.processImage();
                String classification = processor.classify();
                String similarCases = processor.findSimilarCases(3);
                Viewer viewer = processor.getGraphVisualization();

                Platform.runLater(() -> {
                    lblResult.setText("Classification: " + classification);
                    lblSimilarCases.setText("Similar Cases:\n" + similarCases);

                    // Cast to JComponent and update UI
                    JComponent graphView = (JComponent) viewer.addDefaultView(false);
                    graphSwingNode.setContent(graphView);
                    graphContainer.getChildren().set(0, graphSwingNode);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error", "Analysis failed: " + e.getMessage());
                    lblResult.setText("Classification: Error");
                    graphContainer.getChildren().set(0, new Label("Graph unavailable"));
                });
            }
        }).start();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void theme(Label title, HBox titleBox, VBox resultsBox, VBox imageContainer) {
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #EFB036;");
        titleBox.setStyle("-fx-background-color: #23486A;");
        btnUpload.setStyle("-fx-background-color: #4C7B8B; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAnalyze.setStyle("-fx-background-color: #3B6790; -fx-text-fill: white; -fx-font-weight: bold;");
        lblResult.setStyle("-fx-font-size: 16px; -fx-text-fill: #23486A;");
        lblSimilarCases.setStyle("-fx-font-size: 14px; -fx-text-fill: #3B6790;");
        resultsBox.setStyle("-fx-background-color: #F4F9FB;");
        imageContainer.setStyle("-fx-background-color: #F4F9FB;");
        graphContainer.setStyle("-fx-background-color: #F4F9FB; -fx-border-color: #23486A; -fx-border-width: 2;");
    }
}