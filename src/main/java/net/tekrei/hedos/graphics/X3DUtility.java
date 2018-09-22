/*
 * tekrei tarafindan Feb 2, 2006 tarihinde yaratilmistir.
 */
package net.tekrei.hedos.graphics;

import net.tekrei.hedos.ga.utilities.GAParameters;
import net.tekrei.hedos.ga.utilities.Point;
import net.tekrei.hedos.utility.Settings;
import org.web3d.x3d.sai.*;
import org.web3d.x3d.sai.grouping.Transform;
import org.web3d.x3d.sai.rendering.Coordinate;
import org.web3d.x3d.sai.rendering.IndexedLineSet;
import org.web3d.x3d.sai.shape.Appearance;
import org.web3d.x3d.sai.shape.Material;
import org.web3d.x3d.sai.shape.Shape;
import org.web3d.x3d.sai.time.TimeSensor;
import org.xj3d.sai.external.node.interpolation.SAIPositionInterpolator;
import org.xj3d.ui.awt.browser.ogl.X3DBrowserJPanel;

import java.nio.file.Files;
import java.nio.file.Paths;

public class X3DUtility {

    private final static X3DBrowserJPanel x3dComp = (X3DBrowserJPanel) BrowserFactory.createX3DComponent(null);
    private static final float[] targetColor = {1.0f, 1.0f, 0.0f};
    private static final float[] lineColor = {1.0f, 0f, 0f};
    private static X3DScene scene;

    public static X3DBrowserJPanel createBrowser() {
        ExternalBrowser browser = x3dComp.getBrowser();
        try {
            scene = browser.createX3DFromString(new String(Files.readAllBytes(
                    Paths.get(ClassLoader.getSystemResource("x3d/DolasimDunyasi.x3d").toURI()))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        browser.replaceWorld(scene);
        return x3dComp;
    }

    private static Shape createShapeWithColor(float[] color, X3DGeometryNode geometryNode) {
        Shape shape = (Shape) scene.createNode("Shape");
        Appearance appearance = (Appearance) scene.createNode("Appearance");
        Material material = (Material) scene.createNode("Material");
        material.setEmissiveColor(color);
        appearance.setMaterial(material);
        shape.setAppearance(appearance);
        shape.setGeometry(geometryNode);
        return shape;
    }

    private static X3DNode createTarget() {
        return createShapeWithColor(targetColor, (X3DGeometryNode) scene.createNode("Sphere"));
    }

    public static void addLineSet(float[] coords, int[] indexes) {
        Coordinate coord = (Coordinate) scene.createNode("Coordinate");
        coord.setPoint(coords);
        IndexedLineSet ils = (IndexedLineSet) scene.createNode("IndexedLineSet");
        ils.setCoord(coord);
        ils.setCoordIndex(indexes);
        scene.addRootNode(createShapeWithColor(lineColor, ils));
    }

    public static void createTargetAt(Point nokta) {
        Transform transf = (Transform) scene.createNode("Transform");
        scene.updateNamedNode(nokta.getName(), transf);
        transf.setTranslation(new float[]{nokta.getX(), nokta.getY(), nokta.getZ()});
        transf.realize();
        transf.addChildren(new X3DNode[]{createTarget()});
        scene.addRootNode(transf);
    }

    public static void deleteNode(String isim) {
        scene.removeRootNode(scene.getNamedNode(isim));
    }

    public static void setRoute() {
        SAIPositionInterpolator interpolator = (SAIPositionInterpolator) scene.getNamedNode("DolasimRotasi");

        int hedefAdet = GAParameters.getInstance().getHedefSayi();

        float artis = (float) 1 / (hedefAdet + 1);
        float baslangic = 0f;

        Point baslangicNoktasi = Settings.getInstance().getBaslangic();
        float[] baslangicNoktasiKoordinatlari = new float[]{
                baslangicNoktasi.getX(), baslangicNoktasi.getY(),
                baslangicNoktasi.getZ()};

        float[] keyArray = new float[hedefAdet + 2];
        float[] keyValueArray = new float[(hedefAdet + 2) * 3];

        int valueIndex = 0;
        keyArray[0] = baslangic;
        keyValueArray[valueIndex] = baslangicNoktasiKoordinatlari[0];
        keyValueArray[++valueIndex] = baslangicNoktasiKoordinatlari[1];
        keyValueArray[++valueIndex] = baslangicNoktasiKoordinatlari[2];

        baslangic += artis;

        for (int i = 1; i < (hedefAdet + 1); i++) {
            try {
                keyArray[i] = baslangic;

                Point hedefNoktasi = GAParameters.getInstance().getHedef(
                        i - 1);
                keyValueArray[++valueIndex] = hedefNoktasi.getX();
                keyValueArray[++valueIndex] = hedefNoktasi.getY();
                keyValueArray[++valueIndex] = hedefNoktasi.getZ();
                baslangic += artis;
            } catch (Exception e) {
                System.err.println(i + ":" + e.toString());
            }
        }

        keyArray[hedefAdet + 1] = 1.0f;
        keyValueArray[++valueIndex] = baslangicNoktasiKoordinatlari[0];
        keyValueArray[++valueIndex] = baslangicNoktasiKoordinatlari[1];
        keyValueArray[++valueIndex] = baslangicNoktasiKoordinatlari[2];

        interpolator.setKey(keyArray);
        interpolator.setKeyValue(keyValueArray);
    }

    public static void shutdown() {
        x3dComp.shutdown();
    }

    public static void setTimer() {
        TimeSensor timeInterpolator = (TimeSensor) scene.getNamedNode("DolasimZamanlayici");
        timeInterpolator.setCycleInterval((float) (GAParameters.getInstance().getHedefSayi() * 0.5));
        timeInterpolator.setStartTime(System.currentTimeMillis() * 0.001);
    }
}
