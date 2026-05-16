package hedos.utility;

import hedos.ga.data.Point;
import java.util.List;

/**
 * Utility class for path and coordinate calculations.
 */
public class PathUtils {

    public static float[] getPathCoordinates(int[] path, List<Point> targets) {
        float[] coordinates = new float[path.length * 3];
        for (int i = 0; i < path.length; i++) {
            Point target = targets.get(path[i]);
            coordinates[i * 3] = target.x();
            coordinates[i * 3 + 1] = target.y();
            coordinates[i * 3 + 2] = target.z();
        }
        return coordinates;
    }

    public static int[] getLineIndices(int pathLength) {
        if (pathLength < 2) return new int[0];
        int[] index = new int[(pathLength - 1) * 3];
        for (int i = 0; i < pathLength - 1; i++) {
            index[i * 3] = i;
            index[i * 3 + 1] = i + 1;
            index[i * 3 + 2] = -1; // End of segment
        }
        return index;
    }

    public static int[] detectSharpTurns(int[] path, List<Point> targets) {
        if (path.length < 2) return new int[0];
        int[] colorIndex = new int[path.length - 1];
        
        // Detect sharp turns (angle > 90 degrees) to highlight segments
        for (int i = 0; i < path.length - 2; i++) {
            Point p1 = targets.get(path[i]);
            Point p2 = targets.get(path[i + 1]);
            Point p3 = targets.get(path[i + 2]);

            float v1x = p2.x() - p1.x();
            float v1y = p2.y() - p1.y();
            float v1z = p2.z() - p1.z();
            float v2x = p3.x() - p2.x();
            float v2y = p3.y() - p2.y();
            float v2z = p3.z() - p2.z();

            // Dot product < 0 means angle between heading vectors is > 90 degrees
            if (v1x * v2x + v1y * v2y + v1z * v2z < 0) {
                colorIndex[i] = 1;     // Segment before turn
                colorIndex[i + 1] = 1; // Segment after turn
            }
        }
        return colorIndex;
    }
}