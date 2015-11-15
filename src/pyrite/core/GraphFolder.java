package pyrite.core;

import de.jg3d.Graph;
import de.jg3d.Node;
import java.util.List;

public class GraphFolder {

    public interface DecisionMaker {

        public boolean hasFiniteTraversal();

        public Node getNextNode(Graph g);

        public List<Node> getTraversing(Graph g);

        public double[] decisionMaker(double[] perception);

        public double[] generatePerception();

        public double performAction(double[] actionsScore);
    }

    public interface ConvexUniformHoneycomb {

        public interface Builder {

            public void drawNode();

            public void drawEdge();
        }

        public List<int[]> getNeighborhood(int[] point);

        public int[] getClosestNeighbor(double[] point);

        public void spawn(Builder builder, int width, int height, int depth);
    }

    public static double fold(Graph g, DecisionMaker d) {
        double score = 0;
        if (d.hasFiniteTraversal()) {
            List<Node> traversing = d.getTraversing(g);
            for (Node n : traversing) {
                score += d.performAction(d.decisionMaker(d.generatePerception()));
            }
        } else {
            Node n;
            while ((n = d.getNextNode(g)) != null) {
                score += d.performAction(d.decisionMaker(d.generatePerception()));
            }
        }
        return score + getScore(g);
    }

    public static double getScore(Graph g) {
        return 0;
    }

}
