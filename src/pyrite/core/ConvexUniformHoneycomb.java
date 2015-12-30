/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pyrite.core;

import de.jg3d.Edge;
import de.jg3d.Node;
import de.jg3d.Vector;
import java.util.List;

/**
 *
 * @author andy
 */
public interface ConvexUniformHoneycomb {

    public interface Builder {

        public void drawNode(Vector p);

        public void drawEdge(Vector p1, Vector p2);
    }

    public double getCellVolume();
    
    public double getShortestDistance();
    
    public boolean isSatisfied(Edge e);

    public boolean isSatisfiedOnPosition(Edge e, Vector pos);

    public double normalizeDensity(int nodeCount, double volume);

    public int getMaxNeighborCount();

    public Node getNode(Vector point);

    public void setNodePlaced(Vector point, Node node);

    public List<Vector> getNeighborhood(Vector point);

    public Vector getClosestNeighbor(Vector point);

    public int getSatisfiedConnectionCount(Edge e, Vector pos);

    public int getUnsolvedNeighborConnectionCount(Edge e, Vector pos);

    public int getMaximumSatisfiedConnectionCount();

    public int getMaximumUnsolvedNeighborConnectionCount();

    public void spawn(Builder builder, int width, int height, int depth);
}
