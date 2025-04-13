package acsse.csc3a.miniProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the dataset of images from the specified directory.
 * The dataset is expected to be organized into subdirectories
 * for each class (e.g., "benign" and "malignant").
 */
public class DatasetLoader {

    /**
     * Loads the dataset from the specified path.
     *
     * @param datasetPath the path to the dataset directory
     * @return a list of ImageData objects representing the images and their labels
     */
    public static List<ImageData> loadDataset(String datasetPath) {
        List<ImageData> dataset = new ArrayList<>();

        File datasetDir = new File(datasetPath);
        File[] classDirs = datasetDir.listFiles();

        if (classDirs == null) return dataset;

        for (File classDir : classDirs) {
            int label = classDir.getName().equalsIgnoreCase("benign") ? 0 : 1;
            File[] images = classDir.listFiles();

            if (images == null) continue;

            for (File imageFile : images) {
                dataset.add(new ImageData(imageFile, label));
            }
        }

        return dataset;
    }
}

/*
    * The DatasetLoader class is responsible for loading the dataset of images. The following is how to use it:
    * List<ImageData> trainSet = DatasetLoader.loadDataset("dataset/train");
    * List<ImageData> testSet = DatasetLoader.loadDataset("dataset/test");
 */

/*
 * The DatasetLoader class is responsible for loading the dataset of images. The following is how to use it:
 * List<ImageData> trainSet = DatasetLoader.loadDataset("dataset/train");
 * List<ImageData> testSet = DatasetLoader.loadDataset("dataset/test");
 */