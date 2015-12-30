package pyrite.core;

import de.jg3d.Edge;
import de.jg3d.Graph;
import de.jg3d.Node;
import de.jg3d.Vector;

public class GraphUtils {

    public static double getDistance(Node n1, Node n2) {
        return n1.getPos().distance(n2.getPos());
    }

    public static double getVolume(Graph g, ConvexUniformHoneycomb cuh) {
        double x0, y0, z0, x, y, z, x1, y1, z1;
        x0 = y0 = z0 = Double.POSITIVE_INFINITY;
        x1 = y1 = z1 = Double.NEGATIVE_INFINITY;
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
        return (int) (((x1 - x0 + 1) * (y1 - y0 + 1) * (z1 - z0 + 1)) / cuh.getCellVolume()) - 1;
    }

    public static double[] getNewVolumeWithPoint(Graph g, ConvexUniformHoneycomb cuh, double[] limits, Vector p) {
        double x0, y0, z0, x, y, z, x1, y1, z1;
        if (limits == null) {
            limits = new double[7];
            x0 = y0 = z0 = Double.POSITIVE_INFINITY;
            x1 = y1 = z1 = Double.NEGATIVE_INFINITY;
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
            limits[1] = x0;
            limits[2] = x1;
            limits[3] = y0;
            limits[4] = y1;
            limits[5] = z0;
            limits[6] = z1;
        } else {
            x0 = limits[1];
            x1 = limits[2];
            y0 = limits[3];
            y1 = limits[4];
            z0 = limits[5];
            z1 = limits[6];
        }
        x = p.getX();
        y = p.getY();
        z = p.getZ();
        x0 = (x < x0) ? x : x0;
        y0 = (y < y0) ? y : y0;
        z0 = (z < z0) ? z : z0;
        x1 = (x > x1) ? x : x1;
        y1 = (y > y1) ? y : y1;
        z1 = (z > z1) ? z : z1;
        limits[0] = ((x1 - x0 + 1) * (y1 - y0 + 1) * (z1 - z0 + 1)) / cuh.getCellVolume();
        return limits;
    }

    public static double scoreDistribution(Graph g, ConvexUniformHoneycomb cuh, Vector center, int maxOffset) {
        double x0, y0, z0, x, y, z, x1, y1, z1;
        x0 = y0 = z0 = Double.POSITIVE_INFINITY;
        x1 = y1 = z1 = Double.NEGATIVE_INFINITY;
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
        score += Math.abs((x - x0) - (x1 - x)) / cuh.getShortestDistance();
        score += Math.abs((y - y0) - (y1 - y)) / cuh.getShortestDistance();
        score += Math.abs((z - z0) - (z1 - z)) / cuh.getShortestDistance();
        //normalize
        score = (maxOffset - score) / maxOffset;
        return score < 0 ? 0 : score;
    }

    public static Vector getDefaultKernelPos() {
        return new Vector();
    }

    public static Node setKernel(Graph g, ConvexUniformHoneycomb cuh) {
        Node kernel = null;
        for (Node n : g.getNodes()) {
            if (kernel == null || n.getAdjacencies().size() > kernel.getAdjacencies().size()) {
                kernel = n;
            }
        }
        if (kernel == null) {
            return null;
        } else {
            kernel.setFixed(true);
            kernel.setPos(getDefaultKernelPos());
            cuh.setNodePlaced(kernel.getPos(), kernel);
            GraphFolder.delay();
            return kernel;
        }
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

    public static Node addNodeBetween(Graph graph, Node n1, Node n2, Node newNode) {
        graph.disconnect(n1, n2);
        newNode.setPos(n1.getPos().midpoint(n2.getPos()));
        graph.addNode(newNode);
        graph.connect(n1, newNode, 100);
        graph.connect(n2, newNode, 100);
        return newNode;
    }
}
