package spaceindexer;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Demonstration of quad tree insert and find, with visual feedback on subdivisions and
 * bound selections.
 *
 * @author Jean-Luc Thumm
 */
public class Main extends Application {

    // Constants
    private static final int POINT_RAD = 2; // Radius of points graphed

    private Group root;             // groups canvas and highlighted nodes
    private Canvas canvas;          // where graphing will occur
    private GraphicsContext gc;     // gc of canvas
    private QuadTree tree;          // contains points for logn access times
    private Point2D selecAnchor;    // stores anchor of each selection rectangle
    private Rectangle selecRec;     // actual selection rectangle
    private ArrayList<Node> highlightedPoints;  // keeps track of all highlighted points

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Set up hierarchy
        primaryStage.setTitle("Graph");
        root = new Group();
        Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        // Set up canvas
        canvas = new Canvas();
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::canvasClicked);
        canvas.heightProperty().bind(scene.heightProperty());
        canvas.widthProperty().bind(scene.widthProperty());
        root.getChildren().add(canvas);

        // Set up root event handlers
        root.setOnMousePressed(this::captureSelectionAnchor);
        root.setOnMouseDragged(this::dragSelection);
        root.setOnMouseReleased(this::endSelection);

        // Construct tree
        tree = new QuadTree(new Rectangle2D(0, 0, canvas.getWidth(), canvas.getHeight()));

        highlightedPoints = new ArrayList<>();

        // Display to user
        primaryStage.show();
    }

    /** Handle clicks on canvas **/
    private void canvasClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            // Graph a point and add to tree
            gc.setFill(Color.BLACK);
            gc.fillOval(event.getX() - POINT_RAD, event.getY() - POINT_RAD, 2 * POINT_RAD, 2 * POINT_RAD);
            tree.add(new Point2D(event.getX(), event.getY()));
            tree.graphBoundaries(gc);
            event.consume(); // prevent selection from happening;
        }
    }

    /** Captures root point for selection rectangle */
    private void captureSelectionAnchor(MouseEvent event) {
        // Create selection rectangle and prep for dragging
        if (event.getButton() != MouseButton.PRIMARY) return;
        selecAnchor = new Point2D(event.getSceneX(), event.getSceneY());
        selecRec = new Rectangle(selecAnchor.getX(), selecAnchor.getY(), 0, 0);
        selecRec.setStroke(Color.BLACK);
        selecRec.setFill(Color.TRANSPARENT);
        root.getChildren().add(selecRec);

        // Get rid of previously highlighted points
        root.getChildren().removeAll(highlightedPoints); // TODO get rid of this
        highlightedPoints.clear();
    }

    /** Resizes selection rectangle */
    private void dragSelection(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) return;
        double newWidth = event.getSceneX() - selecAnchor.getX();
        double newHeight = event.getSceneY() - selecAnchor.getY();

        // Handle rectangles growing in all direction from their anchor point
        if (newWidth >= 0) {
            selecRec.setWidth(newWidth);
            selecRec.setX(selecAnchor.getX());
        } else {
            selecRec.setWidth(-newWidth);
            selecRec.setX(selecAnchor.getX() + newWidth);
        }
        if (newHeight >= 0) {
            selecRec.setHeight(newHeight);
            selecRec.setY(selecAnchor.getY());
        } else {
            selecRec.setHeight(-newHeight);
            selecRec.setY(selecAnchor.getY() + newHeight);
        }
    }

    /** Removes selection triangle and delegates to tree to find points */
    private void endSelection(MouseEvent event) {
        // Convert to canvas coordinates
        if (event.getButton() != MouseButton.PRIMARY) return;
        Point2D canvasPt = canvas.parentToLocal(selecRec.getX(), selecRec.getY());
        Rectangle2D selection = new Rectangle2D(canvasPt.getX(), canvasPt.getY(),
                selecRec.getWidth(), selecRec.getHeight());

        // Highlight selected points
        LinkedList<Point2D> points = tree.getPointsInBound(selection);
        highlightPoints(points); // TODO get rid of this

        // Get rid of selection rectangle
        root.getChildren().remove(selecRec);
        selecRec = null;
    }

    /** Highlights given points by overlaying nodes over the positions */
    private void highlightPoints(LinkedList<Point2D> points) {
        // Overlay points with actual red nodes
        highlightedPoints = new ArrayList<>(points.size());
        for (Point2D point : points) {
            Circle circle = new Circle(point.getX(), point.getY(), POINT_RAD);
            circle.setFill(Color.RED);
            root.getChildren().add(circle);
            highlightedPoints.add(circle);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
