/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pyrite.core;

import de.jg3d.Edge;
import de.jg3d.Graph;
import java.util.List;

/**
 *
 * @author andy
 */
public interface FoldingStrategy {

    public boolean hasStaticTraversal();

    public Edge getNextEdge(Graph g);

    public List<Edge> getTraversing(Graph g);

    public double[] decisionMaker(double[] perception);

    public double[] generatePerception(Graph g, Edge e);

    public double performAction(double[] actionsScore, Graph g, Edge e);
}
