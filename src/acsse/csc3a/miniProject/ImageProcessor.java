package acsse.csc3a.miniProject;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Handles image processing, graph construction, and analysis
 */
public class ImageProcessor {
    private final BufferedImage image;
    private Graph rag;  // Region Adjacency Graph
    private final List<Region> regions;
    private final HashMap<String, double[]> featureDatabase;  // For similarity search: Chapter 14

    public ImageProcessor(File imageFile) throws IOException {
        this.image = ImageIO.read(imageFile);
        this.regions = new ArrayList<>();
        this.featureDatabase = new HashMap<>();  // In real project: Load from disk
    }

    // Core Methods

    public void processImage() {
        // 1. Segment image into regions
        segmentImage();

        // 2. Build Region Adjacency Graph
        buildRAG();

        // 3. Extract graph features
//        extractFeatures(); Come back and fix this!!
    }

    public String classify() {
        // Simplified GNN or k-NN classification (expand later)
        // double[] features = getGraphFeatureVector(); Implement later after chapter 14
        return (regions.size() > 10) ? "Malignant" : "Benign";  // Placeholder (Mock logic: More regions -> Malignant)
    }

    public String findSimilarCases(int k) {
        // Simplified similarity search (implement GED later)
        double[] currentFeatures = getGraphFeatureVector();
        return "Similar case 1 (0.82)\nSimilar case 2 (0.75)";  // Placeholder
    }

    public Viewer getGraphVisualization() {
        // Create GraphStream visualization
        Graph displayGraph = new SingleGraph("RAG");

        // Add nodes
        for(Region region : regions) {
            Node n = displayGraph.addNode(region.id);
            n.setAttribute("ui.style", "fill-color: rgb(" +
                    region.avgColor[0] + "," +
                    region.avgColor[1] + "," +
                    region.avgColor[2] + ");");
        }

        // Add edges
        for(Region region : regions) {
            for(Region neighbor : region.neighbors) {
                if(displayGraph.getEdge(region.id + "-" + neighbor.id) == null) {
                    displayGraph.addEdge(region.id + "-" + neighbor.id,
                                    region.id, neighbor.id)
                            .setAttribute("ui.style", "fill-color: rgba(100,100,100,128);");
                }
            }
        }

        return displayGraph.display();
    }


    //---------- Helper Methods ----------

    private void segmentImage() {
        // Basic region-growing segmentation (simplified)
        int regionId = 0;
        boolean[][] visited = new boolean[image.getWidth()][image.getHeight()];

        for(int x = 0; x < image.getWidth(); x++) {
            for(int y = 0; y < image.getHeight(); y++) {
                if(!visited[x][y]) {
                    Region region = new Region(regionId++);
                    growRegion(x, y, visited, region);
                    regions.add(region);
                }
            }
        }
    }

    private void growRegion(int startX, int startY, boolean[][] visited, Region region) {
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startX, startY});
        int[] seedColor = getRGB(startX, startY);
        visited[startX][startY] = true;

        while (!queue.isEmpty()) {
            int[] pixel = queue.poll();
            int x = pixel[0], y = pixel[1];
            region.addPixel(x, y, getRGB(x, y));

            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] dir : directions) {
                int nx = x + dir[0];
                int ny = y + dir[1];

                if (nx >= 0 && nx < image.getWidth() &&
                        ny >= 0 && ny < image.getHeight() &&
                        !visited[nx][ny]) {

                    int[] neighborColor = getRGB(nx, ny);
                    if (colorDistance(seedColor, neighborColor) < 30) {
                        visited[nx][ny] = true;
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }
    }

    private void buildRAG() {
        // Detect adjacent regions and build edges
        for(int i = 0; i < regions.size(); i++) {
            Region r1 = regions.get(i);
            for(int j = i+1; j < regions.size(); j++) {
                Region r2 = regions.get(j);
                if(areAdjacent(r1, r2)) {
                    r1.addNeighbor(r2);
                    r2.addNeighbor(r1);
                }
            }
        }
    }

    private boolean areAdjacent(Region r1, Region r2) {
        // Check if regions share adjacent pixels (simplified)
        for(int[] p1 : r1.pixels) {
            for(int dx = -1; dx <= 1; dx++) {
                for(int dy = -1; dy <= 1; dy++) {
                    int x = p1[0] + dx;
                    int y = p1[1] + dy;
                    if(r2.containsPixel(x, y)) return true;
                }
            }
        }
        return false;
    }

    private double[] getGraphFeatureVector() {
        // Feature vector for classification/similarity
        return new double[] {
                regions.size(),                      // Number of regions
                calculateAvgEdgeWeight(),           // Average edge dissimilarity
                calculateDegreeVariance()            // Variance of node degrees
        };
    }

    // Utility Methods

    private int[] getRGB(int x, int y) {
        int rgb = image.getRGB(x, y);
        return new int[] {
                (rgb >> 16) & 0xFF,  // Red
                (rgb >> 8) & 0xFF,   // Green
                rgb & 0xFF           // Blue
        };
    }

    private double colorDistance(int[] c1, int[] c2) {
        return Math.sqrt(
                Math.pow(c1[0]-c2[0], 2) +
                        Math.pow(c1[1]-c2[1], 2) +
                        Math.pow(c1[2]-c2[2], 2)
        );
    }

    private double calculateAvgEdgeWeight() {
        return regions.stream()
                .flatMap(r -> r.neighbors.stream()
                        .map(n -> new AbstractMap.SimpleEntry<>(r, n))) // Pair regions with their neighbors
                .mapToDouble(entry -> colorDistance(
                        entry.getKey().avgColor,    // Original region (r)
                        entry.getValue().avgColor    // Neighbor (n)
                ))
                .average()
                .orElse(0.0);
    }

    private double calculateDegreeVariance() {
        // Statistical measure of graph structure
        double mean = regions.stream()
                .mapToInt(r -> r.neighbors.size())
                .average()
                .orElse(0.0);

        return regions.stream()
                .mapToDouble(r -> Math.pow(r.neighbors.size() - mean, 2))
                .average()
                .orElse(0.0);
    }

    // Nested Region Class
    private class Region {
        String id;
        List<int[]> pixels;
        int[] avgColor;
        List<Region> neighbors;

        Region(int id) {
            this.id = "R" + id;
            this.pixels = new ArrayList<>();
            this.avgColor = new int[3];
            this.neighbors = new ArrayList<>();
        }

        void addPixel(int x, int y, int[] rgb) {
            pixels.add(new int[]{x, y});
            // Update average color incrementally
            avgColor[0] = (avgColor[0] * (pixels.size()-1) + rgb[0]) / pixels.size();
            avgColor[1] = (avgColor[1] * (pixels.size()-1) + rgb[1]) / pixels.size();
            avgColor[2] = (avgColor[2] * (pixels.size()-1) + rgb[2]) / pixels.size();
        }

        boolean containsPixel(int x, int y) {
            return pixels.stream()
                    .anyMatch(p -> p[0] == x && p[1] == y);
        }

        void addNeighbor(Region neighbor) {
            if(!neighbors.contains(neighbor)) {
                neighbors.add(neighbor);
            }
        }
    }
}

