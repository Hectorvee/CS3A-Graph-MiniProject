package acsse.csc3a.miniProject;


import java.io.File;

/**
 * Represents an image and its associated label (benign or malignant).
 */
public class ImageData {
    private File imageFile;
    private int label; // 0 = benign, 1 = malignant

    /**
     * Constructor to initialize ImageData with a file and its label.
     *
     * @param imageFile the image file
     * @param label     the label (0 for benign, 1 for malignant)
     */
    public ImageData(File imageFile, int label) {
        this.imageFile = imageFile;
        this.label = label;
    }

    /**
     * Gets the image file.
     *
     * @return the image file
     */
    public File getImageFile() {
        return imageFile;
    }

    /**
     * Gets the label of the image.
     *
     * @return the label (0 for benign, 1 for malignant)
     */
    public int getLabel() {
        return label;
    }
}

