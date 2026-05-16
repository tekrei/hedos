package hedos.graphics;

import hedos.ga.data.Point;
import hedos.utility.Settings;
import org.web3d.x3d.sai.*;
import org.web3d.x3d.sai.grouping.Transform;
import org.web3d.x3d.sai.rendering.Color;
import org.web3d.x3d.sai.rendering.Coordinate;
import org.web3d.x3d.sai.rendering.IndexedLineSet;
import org.web3d.x3d.sai.shape.Appearance;
import org.web3d.x3d.sai.shape.Material;
import org.web3d.x3d.sai.shape.Shape;
import org.web3d.x3d.sai.time.TimeSensor;
import org.xj3d.sai.external.node.interpolation.SAIPositionInterpolator;
import org.xj3d.ui.awt.browser.ogl.X3DBrowserJPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class X3DEngine {
    private static final Logger logger = LoggerFactory.getLogger(X3DEngine.class);
    private final X3DBrowserJPanel browserPanel;
    private final float[] targetColor = {1.0f, 1.0f, 0.0f};
    private final float[] lineColor = {1.0f, 0f, 0f};
    private X3DScene scene;

    public X3DEngine() {
        this.browserPanel = (X3DBrowserJPanel) BrowserFactory.createX3DComponent(null);
        if (this.browserPanel == null) {
            throw new RuntimeException("Failed to create X3D component. Check native libraries and JOGL/Xj3D classpath.");
        }
        initializeScene();
    }

    private void initializeScene() {
        ExternalBrowser browser = browserPanel.getBrowser();
        try {
            java.net.URL sceneUrl = getClass().getResource("/x3d/world.x3d");
            if (sceneUrl == null) throw new RuntimeException("X3D Scene file not found in resources!");
            
            // Use createX3DFromURL to allow relative path resolution for Protos and textures
            scene = browser.createX3DFromURL(new String[]{sceneUrl.toExternalForm()});
            browser.replaceWorld(scene);
        } catch (Exception e) {
            logger.error("Failed to initialize X3D scene: {}", e.getMessage(), e);
            throw new RuntimeException("X3D Scene initialization failed", e);
        }
    }

    public X3DBrowserJPanel getBrowserPanel() {
        return browserPanel;
    }

    public void addLineSet(float[] coords, int[] indexes, int[] colorIndices) {
        deleteNode("P_SOLUTION");
        Coordinate coord = (Coordinate) scene.createNode("Coordinate");
        coord.setPoint(coords);

        // Create a Color node with two entries: 0 for normal segments, 1 for sharp turns
        Color colorNode = (Color) scene.createNode("Color");
        colorNode.setColor(new float[]{
                lineColor[0], lineColor[1], lineColor[2],        // Normal color (Red)
                targetColor[0], targetColor[1], targetColor[2]   // Sharp turn color (Yellow)
        });

        IndexedLineSet ils = (IndexedLineSet) scene.createNode("IndexedLineSet");
        ils.setCoord(coord);
        ils.setCoordIndex(indexes);
        ils.setColor(colorNode);
        ils.setColorIndex(colorIndices);
        ils.setColorPerVertex(false); // Color per line segment

        Shape ilsShape = (Shape) scene.createNode("Shape");
        Appearance app = (Appearance) scene.createNode("Appearance");
        // Material emissiveColor is ignored when a Color node is present in the geometry
        ilsShape.setAppearance(app);
        ilsShape.setGeometry(ils);

        scene.updateNamedNode("P_SOLUTION", ilsShape);
        scene.addRootNode(ilsShape);
    }

    public void createTargetAt(Point point) {
        Transform transform = (Transform) scene.createNode("Transform");
        scene.updateNamedNode(point.name(), transform);
        transform.setTranslation(new float[]{point.x(), point.y(), point.z()});
        transform.realize();
        transform.addChildren(new X3DNode[]{createShapeWithColor(targetColor, (X3DGeometryNode) scene.createNode("Sphere"))});
        scene.addRootNode(transform);
    }

    public void deleteNode(String name) {
        try {
            X3DNode node = scene.getNamedNode(name);
            scene.removeRootNode(node);
        } catch (InvalidNodeException e) {
            // Silently ignore if the node doesn't exist yet (e.g. clearing P_SOLUTION)
        }
    }

    public void setRoute(List<Point> targets, Point startPoint) {
        SAIPositionInterpolator interpolator = (SAIPositionInterpolator) scene.getNamedNode("TourPath");
        int targetCount = targets.size();
        float increment = 1.0f / (targetCount + 1);
        float currentKey = 0f;

        float[] keys = new float[targetCount + 2];
        float[] values = new float[(targetCount + 2) * 3];

        keys[0] = currentKey;
        values[0] = startPoint.x();
        values[1] = startPoint.y();
        values[2] = startPoint.z();

        for (int i = 0; i < targetCount; i++) {
            currentKey += increment;
            keys[i + 1] = currentKey;
            Point p = targets.get(i);
            values[(i + 1) * 3] = p.x();
            values[(i + 1) * 3 + 1] = p.y();
            values[(i + 1) * 3 + 2] = p.z();
        }

        keys[targetCount + 1] = 1.0f;
        values[(targetCount + 1) * 3] = startPoint.x();
        values[(targetCount + 1) * 3 + 1] = startPoint.y();
        values[(targetCount + 1) * 3 + 2] = startPoint.z();

        interpolator.setKey(keys);
        interpolator.setKeyValue(values);
    }

    public void startTour(int targetCount) {
        TimeSensor timer = (TimeSensor) scene.getNamedNode("TourTimer");
        timer.setCycleInterval((float) (targetCount * 0.5));
        timer.setStartTime(System.currentTimeMillis() * 0.001);
    }

    private Shape createShapeWithColor(float[] color, X3DGeometryNode geometryNode) {
        Shape shape = (Shape) scene.createNode("Shape");
        Appearance app = (Appearance) scene.createNode("Appearance");
        Material mat = (Material) scene.createNode("Material");
        mat.setEmissiveColor(color);
        app.setMaterial(mat);
        shape.setAppearance(app);
        shape.setGeometry(geometryNode);
        return shape;
    }

    public void shutdown() {
        browserPanel.shutdown();
    }
}