//Overall Main class: 30 marks ***********************************************
//Correctness (10 marks) ********************************
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class Main {
    /**
     * Extracts 8x8 grayscale patches from an image.
     * @param image Input BufferedImage.
     * @return PositionList of 8x8 patches.
     */
    public static PositionList<Patch> extractPatches(BufferedImage image) {
        PositionList<Patch> patches = new PositionList<>();
        for (int y = 0; y <= image.getHeight() - 8; y += 8) {
            for (int x = 0; x <= image.getWidth() - 8; x += 8) {
                double[][] patchData = new double[8][8];
                for (int dy = 0; dy < 8; dy++) {
                    for (int dx = 0; dx < 8; dx++) {
                        int rgb = image.getRGB(x + dx, y + dy);
                        int gray = (((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff)) / 3;
                        patchData[dy][dx] = gray;
                    }
                }
                patches.addLast(new Patch(patchData, x, y));
            }
        }
        return patches;
    }

    /**
     * Reconstructs an image from a list of patches.
     * @param patches PositionList of selected patches.
     * @param width Width of the final image.
     * @param height Height of the final image.
     * @return Reconstructed BufferedImage.
     * 10 marks ***********************************************
     */
    public static BufferedImage renderScene(PositionList<Patch> patches, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (Position<Patch> pos = patches.first(); pos != null; pos = patches.next(pos)) {
            Patch patch = pos.element();
            if (patch == null) continue;  // Skip null patches

            for (int dy = 0; dy < 8; dy++) {
                for (int dx = 0; dx < 8; dx++) {
                    int x = patch.getX() + dx;
                    int y = patch.getY() + dy;
                    if (x < width && y < height) {
                        int gray = (int) patch.getData()[dy][dx];
                        int rgb = (gray << 16) | (gray << 8) | gray;
                        image.setRGB(x, y, rgb);
                    }
                }
            }
        }
        return image;
    }

    public static void main(String[] args) throws IOException {
        // Load multiple images from the scene
        System.out.println("Loading images and extracting patches...");
        File[] imageFiles = new File("scenes/").listFiles((dir, name) -> name.endsWith(".jpg"));
        if (imageFiles == null || imageFiles.length == 0) {
            System.err.println("No JPG images found in data directory");
            return;
        }

        PositionList<Patch> allPatches = new PositionList<>();
        for (File file : imageFiles) {
            BufferedImage img = ImageIO.read(file);
            if (img != null) {
                PositionList<Patch> patches = extractPatches(img);
                for (Position<Patch> pos = patches.first(); pos != null; pos = patches.next(pos)) {
                    allPatches.addLast(pos.element());
                }
            }
        }

        // Heap insertion - Use the custom Heap class with key = Hamming distance and value = Patch
        //5 marks ***********************************************
        // Insert all patches into the heap with their Hamming distance as the key
        System.out.println("Calculating hashes and inserting into heap...");
        Heap<Integer, Patch> heap = new Heap<>();
        for (Position<Patch> pos = allPatches.first(); pos != null; pos = allPatches.next(pos)) {
            Patch patch = pos.element();
            if (patch == null) continue;

            // Calculate average hash
            double total = 0;
            double[][] data = patch.getData();
            for (double[] row : data) {
                for (double val : row) {
                    total += val;
                }
            }
            double mean = total / (data.length * data[0].length);

            int hash = 0;
            int bit = 1;
            for (double[] row : data) {
                for (double val : row) {
                    if (val > mean) {
                        hash |= bit;
                    }
                    bit <<= 1;
                    if (bit == 0) bit = 1; // Prevent integer overflow
                }
            }

            // Only insert if we have a valid hash and patch
            if (patch != null) {
                heap.insert(hash, patch);
            }
        }

        // Select top patches
        //5 marks ***********************************************
        System.out.println("Selecting best patches...");
        PositionList<Patch> bestPatches = new PositionList<>();
        int patchCount = Math.min(100, heap.size());
        for (int i = 0; i < patchCount; i++) {
            try {
                Entry<Integer, Patch> entry = heap.removeMin();
                if (entry != null && entry.getValue() != null) {
                    bestPatches.addLast(entry.getValue());
                }
            } catch (Exception e) {
                System.err.println("Error removing patch from heap: " + e.getMessage());
                break;
            }
        }

        System.out.println("Rendering completed scene...");
        // Reconstruct and save image
        BufferedImage result = renderScene(bestPatches, 800, 800); // assume 800x800 output
        ImageIO.write(result, "png", new File("completed_scene.png"));

        System.out.println("Done!");
    }
}