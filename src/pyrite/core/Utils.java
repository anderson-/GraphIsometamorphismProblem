package pyrite.core;

import de.jg3d.Edge;
import de.jg3d.Graph;
import de.jg3d.Node;
import de.jg3d.Vector;
import pyrite.core.GraphFolder.ConvexUniformHoneycomb;

public class Utils {

    public static double getDistance(Node n1, Node n2) {
        return n1.getPos().distance(n2.getPos());
    }

    public static double getVolume(Graph g) {
        double x0, y0, z0, x, y, z, x1, y1, z1;
        x0 = y0 = z0 = Double.MAX_VALUE;
        x1 = y1 = z1 = Double.MIN_VALUE;
        for (Node n : g.getNodes()) {
            if (n.isFixed()) {
                Vector v = n.getPos();
                x = v.getX();
                y = v.getY();
                z = v.getZ();
                x0 = (x < x0) ? x : x0;
                y0 = (y < y0) ? y : y0;
                z0 = (z < z0) ? z : z0;
                x1 = (x > x1) ? x : x1;
                y1 = (y > y1) ? y : y1;
                z1 = (z > z1) ? z : z1;
            }
        }
        return (x1 - x0) * (y1 - y0) * (z1 - z0);
    }

    public static double scoreDistribution(Graph g, Vector center, int maxOffset) {
        double x0, y0, z0, x, y, z, x1, y1, z1;
        x0 = y0 = z0 = Double.MAX_VALUE;
        x1 = y1 = z1 = Double.MIN_VALUE;
        for (Node n : g.getNodes()) {
            if (n.isFixed()) {
                Vector v = n.getPos();
                x = v.getX();
                y = v.getY();
                z = v.getZ();
                x0 = (x < x0) ? x : x0;
                y0 = (y < y0) ? y : y0;
                z0 = (z < z0) ? z : z0;
                x1 = (x > x1) ? x : x1;
                y1 = (y > y1) ? y : y1;
                z1 = (z > z1) ? z : z1;
            }
        }
        x = center.getX();
        y = center.getY();
        z = center.getZ();
        double score = 0;
        score += Math.abs((x - x0) - (x1 - x));
        score += Math.abs((y - y0) - (y1 - y));
        score += Math.abs((z - z0) - (z1 - z));
        //normalize
        score = (maxOffset - score) / maxOffset;
        return score < 0 ? 0 : score;
    }

    public static int countAddedNodes(Graph g) {
        int an = 0;
        for (Node n : g.getNodes()) {
            an += n.getType() == GraphFolder.NODE_TYPE_NEW_EXTENSION ? 1 : 0;
        }
        return an;
    }

    public static int countPlacedNodes(Graph g) {
        int pn = 0;
        for (Node n : g.getNodes()) {
            pn += n.isFixed() ? 1 : 0;
        }
        return pn;
    }

    public static int countUnplacedNodes(Graph g) {
        return g.getNodes().size() - countPlacedNodes(g);
    }

    public static int countUnsolvedConnections(Graph g, ConvexUniformHoneycomb cuh) {
        return g.getEdges().size() - countSatisfiedConnections(g, cuh);
    }

    public static int countSatisfiedConnections(Graph g, ConvexUniformHoneycomb cuh) {
        int sc = 0;
        for (Edge e : g.getEdges()) {
            sc += cuh.isSatisfied(e) ? 1 : 0;
        }
        return sc;
    }

    public static int getMaximumSatisfiedConnectionCountByAction() {
        return 0;
    }

    public static int getMaximumUnsolvedNeighbourConnectionCountByAction() {
        return 0;
    }

}
