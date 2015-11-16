package pyrite.core;

import de.jg3d.Edge;
import de.jg3d.Graph;
import de.jg3d.Node;
import de.jg3d.Vector;
import java.util.List;

public class GraphFolder {

    static final int NODE_TYPE_NEW_EXTENSION = 0;
    static final int NODE_TYPE_EXTENSION = 1;
    static final int NODE_TYPE_COMPONENT = 2;

    public interface DecisionMaker {

        public boolean hasFiniteTraversal();

        public Node getNextNode(Graph g);

        public List<Node> getTraversing(Graph g);

        public double[] decisionMaker(double[] perception);

        public double[] generatePerception(Node n);

        public double performAction(double[] actionsScore);
    }

    public interface ConvexUniformHoneycomb {

        public interface Builder {

            public void drawNode();

            public void drawEdge();
        }

        public boolean isSatisfied(Edge e);

        public double getDensity(Graph g);

        public int getMaxNeighborCount();

        public List<Vector> getNeighborhood(Vector point);

        public Vector getClosestNeighbor(Vector point);

        public void spawn(Builder builder, int width, int height, int depth);
    }

    public static double fold(Graph g, DecisionMaker d) {
        double score = 0;
        if (d.hasFiniteTraversal()) {
            List<Node> traversing = d.getTraversing(g);
            for (Node n : traversing) {
                score += d.performAction(d.decisionMaker(d.generatePerception(n)));
            }
        } else {
            Node n;
            while ((n = d.getNextNode(g)) != null) {
                score += d.performAction(d.decisionMaker(d.generatePerception(n)));
            }
        }
        return score + getScore(g);
    }

    private static double getScore(Graph g) {
        return 0;
    }

}
