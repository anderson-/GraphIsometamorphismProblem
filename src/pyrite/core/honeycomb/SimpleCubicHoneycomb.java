/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pyrite.core.honeycomb;

import de.jg3d.Edge;
import de.jg3d.Node;
import de.jg3d.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import util.intervaltree.HDIntervalTree;
import pyrite.core.ConvexUniformHoneycomb;

/**
 *
 * @author anderson
 */
public class SimpleCubicHoneycomb implements ConvexUniformHoneycomb {

    private final boolean allowAxis;
    private final boolean allowFaceDiagonal;
    private final boolean allowSpaceDiagonal;
    private final double edgeLength;
    private final double faceDiagonalLength;
    private final double spaceDiagonalLength;
    private final double cellVolume;
    private final boolean useOctree;
    private final HashMap<Vector, Node> hashmap;
    private final HDIntervalTree<Node> octree;

    public SimpleCubicHoneycomb(boolean allowAxis, boolean allowFaceDiagonal, boolean allowSpaceDiagonal, double edgeLength, boolean useOctree) {
        this.allowAxis = allowAxis;
        this.allowFaceDiagonal = allowFaceDiagonal;
        this.allowSpaceDiagonal = allowSpaceDiagonal;
        this.edgeLength = edgeLength;
        faceDiagonalLength = Math.sqrt(2) * edgeLength;
        spaceDiagonalLength = Math.sqrt(3) * edgeLength;
        cellVolume = edgeLength * edgeLength * edgeLength;
        this.useOctree = useOctree;
        if (useOctree) {
            octree = new HDIntervalTree<>(3);
            hashmap = null;
        } else {
            octree = null;
            hashmap = new HashMap<>();
        }
    }

    public SimpleCubicHoneycomb(double edgeLength, boolean useOctree) {
        this(true, true, true, edgeLength, useOctree);
    }

    @Override
    public double getCellVolume() {
        return cellVolume;
    }

    @Override
    public double getShortestDistance() {
        return edgeLength;
    }

    @Override
    public boolean isSatisfied(Edge e) {
        double distance = e.getLength();
        return e.getSource().isFixed()
                && e.getDestination().isFixed()
                && distance > 0 && distance <= spaceDiagonalLength
                && (allowAxis == false ? distance != edgeLength : true)
                && (allowFaceDiagonal == false ? distance != faceDiagonalLength : true)
                && (allowSpaceDiagonal == false ? distance != spaceDiagonalLength : true)
                && ((allowFaceDiagonal == false && allowSpaceDiagonal == false) ? distance == edgeLength : true);
    }

    @Override
    public boolean isSatisfiedOnPosition(Edge e, Vector pos) {
        Node fixed = e.getSource().isFixed() ? e.getSource() : e.getDestination();
        double distance = fixed.getPos().distance(pos);
        return fixed.isFixed()
                && distance > 0 && distance <= spaceDiagonalLength
                && (allowAxis == false ? distance != edgeLength : true)
                && (allowFaceDiagonal == false ? distance != faceDiagonalLength : true)
                && (allowSpaceDiagonal == false ? distance != spaceDiagonalLength : true)
                && ((allowFaceDiagonal == false && allowSpaceDiagonal == false) ? distance == edgeLength : true);
    }

    @Override
    public Node getNode(Vector point) {
        if (useOctree) {
            List<Node> result = octree.get(point.getX(), point.getY(), point.getZ());
            if (result != null && !result.isEmpty()) {
//                if (result.size() > 1) {
//                    System.err.println("Inconsistent result size: " + result.size());
//                }
                return result.get(0);
            }
        } else {
            return hashmap.get(point);
        }
        return null;
    }

    @Override
    public void setNodePlaced(Vector point, Node node) {
        node.setFixed(true);
        if (useOctree) {
            octree.addPoint(node, point.getX(), point.getY(), point.getZ());
        } else {
            hashmap.put(point, node);
        }
    }

    @Override
    public List<Vector> getNeighborhood(Vector point) {
        List<Vector> n = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    int a = Math.abs(i) + Math.abs(j) + Math.abs(k);
                    if (a != 0) {
                        if ((a == 1 && allowAxis)
                                || (a == 2 && allowFaceDiagonal)
                                || (a == 3 && allowSpaceDiagonal)) {
                            n.add(new Vector(
                                    point.getX() + i * edgeLength,
                                    point.getY() + j * edgeLength,
                                    point.getZ() + k * edgeLength
                            ));
                        }
                    }
                }
            }
        }
//        System.out.println(java.util.Arrays.deepToString(n.toArray()));
        return n;
    }

    @Override
    public Vector getClosestNeighbor(Vector point) {
        int x = (int) point.getX();
        int y = (int) point.getY();
        int z = (int) point.getZ();
        int l = (int) edgeLength;
        x = (x / l) * l + (Math.abs(x) % l > l / 2 ? l * Math.abs(x) / x : 0);
        y = (y / l) * l + (Math.abs(y) % l > l / 2 ? l * Math.abs(y) / y : 0);
        z = (z / l) * l + (Math.abs(z) % l > l / 2 ? l * Math.abs(z) / z : 0);
        return new Vector(x, y, z);
    }

    @Override
    public double normalizeDensity(int nodeCount, double volume) {
        if (volume <= 0) {
            return 0;
        }
        double density = nodeCount / volume;
        double maxDensity = 4 * (volume + edgeLength);
        return density / maxDensity / edgeLength;
    }

    @Override
    public int getMaxNeighborCount() {
        return (allowAxis ? 6 : 0) + (allowFaceDiagonal ? 12 : 0) + (allowSpaceDiagonal ? 8 : 0);
    }

    @Override
    public int getSatisfiedConnectionCount(Edge e, Vector pos) {
        Node lose = !e.getSource().isFixed() ? e.getSource() : e.getDestination();
        int satisfiedConnCount = 0;
        for (Edge adj : lose.getAdjacencies()) {
            satisfiedConnCount += isSatisfiedOnPosition(adj, pos) ? 1 : 0;
        }
        return satisfiedConnCount;
    }

    @Override
    public int getUnsolvedNeighborConnectionCount(Edge e, Vector pos) {
        Node lose = !e.getSource().isFixed() ? e.getSource() : e.getDestination();
        int usatisfiedEdgeCount = 0;
        for (Edge adj : lose.getAdjacencies()) {
            Node neighbour = lose != adj.getSource() ? adj.getSource() : adj.getDestination();
            for (Edge nbAdj : neighbour.getAdjacencies()) {
                usatisfiedEdgeCount += !isSatisfied(nbAdj) ? 1 : 0;
            }
        }
        return usatisfiedEdgeCount - getSatisfiedConnectionCount(e, pos);
    }

    @Override
    public int getMaximumSatisfiedConnectionCount() {
        return getMaxNeighborCount();
    }

    @Override
    public int getMaximumUnsolvedNeighborConnectionCount() {
        return getMaxNeighborCount() * getMaxNeighborCount();
    }

    @Override
    public void spawn(Builder builder, int width, int height, int depth) {
        Vector point = new Vector();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < depth; k++) {
                    int a = Math.abs(i) + Math.abs(j) + Math.abs(k);
                    if (a != 0) {
                        if ((a == 1 && allowAxis)
                                || (a == 2 && allowFaceDiagonal)
                                || (a == 3 && allowSpaceDiagonal)) {
                            Vector p = new Vector(
                                    point.getX() + i * edgeLength,
                                    point.getY() + j * edgeLength,
                                    point.getZ() + k * edgeLength);
                            builder.drawNode(p);
                            for (Vector v : getNeighborhood(p)) {
                                builder.drawEdge(p, v);
                            }
                        }
                    }
                }
            }
        }
    }

}
