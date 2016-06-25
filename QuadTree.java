package spaceindexer;


import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.LinkedList;

/**
 * Indexes 2D space using a tree data structure. Supports 2D points insertion
 * and look up (individually and within a region)
 *
 * @author Jean-Luc
 */
class QuadTree {

    private Node root;
    private long size;

    /**
     * Construct new QuadTree with the given bounds
     *
     * @param bounds only points within this bound will be added to tree
     */
    QuadTree(Rectangle2D bounds) {
        root = new Node(null, bounds); // root node has no data
        size = 0;
    }

    /**
     * Gets the number of points stored in this tree
     *
     * @return SE
     */
    @SuppressWarnings("unused")
    public long getSize() {

        return size;
    }

    /**
     * Checks if this tree contains no points
     *
     * @return SE
     */
    @SuppressWarnings("unused")
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Insertion operation
     *
     * @param point point to be inserted into the tree
     * @return {@code true} if the insertion was succesful,
     * {@code false} otherwise
     */
    boolean add(Point2D point) {
        System.out.println(point); // DEBUG
        return point != null && add(root, point);
    }

    /**
     * Recursive helper for {@link #add(Point2D)}
     */
    private boolean add(Node node, Point2D point) {
        // Leaf node -> subdivide and add
        if (node.isLeaf()) {
            // Child is empty -> just add to it
            if (node.data == null) {
                node.data = point;
                size++;
                return true;
            }

            // Child is not empty -> subdivide for more space
            if (point.equals(node.data)) return false; // no duplicates
            if (!node.subdivide()) return false; // subdivide

            // Add to appropriate child
            for (Node quad : node.quads) {
                if (quad.contains(point)) {
                    quad.data = point;
                    size++;
                    return true;
                }
            }
            return false;
        }

        // Parent node -> recur
        for (Node quad : node.quads) {
            // Let child figure out if it needs to subdivide
            if (quad.contains(point))
                add(quad, point);
        }
        return false;
    }

    /**
     * Verifies if a point is stored in the tree
     *
     * @param point point to verify
     * @return {@code true} if found, {@code false} otherwise
     */
    @SuppressWarnings("unused")
    boolean find(Point2D point) {
        return root != null && point != null && find(root, point);
    }

    /**
     * Recursive helper for {@link #find(Point2D)}
     */
    private boolean find(Node node, Point2D point) {
        if (node == null) return false;
        if (point.equals(node.data)) return true;

        // Recur
        for (Node quad : node.quads) {
            if (find(quad, point)) return true;
        }
        return false;
    }

    /**
     * Displays the boundaries of each node
     *
     * @param gc GraphicsContext to display bounds on
     */
    void graphBoundaries(GraphicsContext gc) {
        if (root == null) return;
        gc.setFill(Color.BLUE);
        double width = gc.getLineWidth();
        gc.setLineWidth(0.5);
        graphBoundaries(root, gc);
        gc.setLineWidth(width);
    }

    /**
     * Recursive helper for {@link #graphBoundaries(GraphicsContext)}
     */
    private void graphBoundaries(Node node, GraphicsContext gc) {
        // Print boundary
        Rectangle2D bounds = node.bounds;
        gc.strokeRect(bounds.getMinX(), bounds.getMinY(),
                bounds.getWidth(), bounds.getHeight());

        // Traverse
        if (node.isLeaf()) return;
        for (Node quad : node.quads) {
            graphBoundaries(quad, gc);
        }
    }

    /**
     * Gathers all the points stored in this tree in a given region
     *
     * @param bound defines region to search for points in
     * @return a list of points contained in the region
     */
    LinkedList<Point2D> getPointsInBound(Rectangle2D bound) {
        LinkedList<Point2D> list = new LinkedList<>();
        if (root == null) return list;
        getPointsInBound(root, list, bound);
        return list;
    }

    /**
     * Recursive helper for {@link #getPointsInBound(Rectangle2D)}
     */
    private void getPointsInBound(Node node, LinkedList<Point2D> list, Rectangle2D bound) {
        if (bound.contains(node.data))
            list.add(node.data);

        // Check children
        if (node.isLeaf()) return;
        for (Node quad : node.quads) {
            if (quad.bounds.intersects(bound))
                getPointsInBound(quad, list, bound);
        }
    }

    /**
     * Underlying element of QuadTree. Has 4 children representing the 4
     * quadrants of space in 2D
     */
    private class Node {
        Point2D data;
        Rectangle2D bounds;
        Node[] quads;

        /**
         * Creates a new node with the given parameters
         *
         * @param data data for node to hold
         * @param ne   pointer to node representing northeast quadrant
         * @param nw   pointer to node representing northwest quadrant
         * @param sw   pointer to node representing southwest quadrant
         * @param se   pointer to node representing southeast quadrant
         */
        Node(Point2D data, Rectangle2D bounds, Node ne, Node nw, Node sw, Node se) {
            this.bounds = bounds;
            this.data = data;
            quads = new Node[4];
            quads[0] = ne;
            quads[1] = nw;
            quads[2] = sw;
            quads[3] = se;
        }

        /**
         * Creates a new leaf node with the given data
         *
         * @param data data to place in leaf node
         */
        Node(Point2D data, Rectangle2D bounds) {
            this(data, bounds, null, null, null, null);
        }

        /**
         * Checks if this node is a leaf
         *
         * @return true if this node is a leaf, false otherwise
         */
        boolean isLeaf() {
            return quads[0] == null; // all children are created at once
        }

        /**
         * Checks if point is contained in this node's region
         *
         * @param point point to check for
         * @return self explanatory
         */
        boolean contains(Point2D point) {
            return bounds.contains(point);
        }

        /**
         * Create children for the 4 regions in 2D space
         *
         * @return true if operation was successful, false otherwise
         */
        boolean subdivide() {
            if (!isLeaf()) return false;

            // Extract dimensions
            double minx = bounds.getMinX();
            double miny = bounds.getMinY();
            double widthSmall = bounds.getWidth() / 2;
            double heightSmall = bounds.getHeight() / 2;

            // Subdivide
            quads[0] = new Node(null, new Rectangle2D(minx + widthSmall, miny, widthSmall, heightSmall));
            quads[1] = new Node(null, new Rectangle2D(minx, miny, widthSmall, heightSmall));
            quads[2] = new Node(null, new Rectangle2D(minx, miny + heightSmall, widthSmall, heightSmall));
            quads[3] = new Node(null, new Rectangle2D(minx + widthSmall, miny + heightSmall, widthSmall, heightSmall));

            return true;
        }
    }
}
