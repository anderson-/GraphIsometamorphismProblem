package pyrite.core;

import de.jg3d.Graph;
import de.jg3d.Node;
import java.util.List;

public class GreedyDecisionMaker implements GraphFolder.DecisionMaker {

    @Override
    public boolean hasFiniteTraversal() {
        return true;
    }

    @Override
    public Node getNextNode(Graph g) {
        return null;
    }

    @Override
    public List<Node> getTraversing(Graph g) {
        return null;
    }

    @Override
    public double[] decisionMaker(double[] perception) {
        double[] actionsScore = new double[]{};
        return actionsScore;
    }

    @Override
    public double[] generatePerception(Node n) {
        return null;
    }

    @Override
    public double performAction(double[] actionsScore) {
        return 0;
    }
}
