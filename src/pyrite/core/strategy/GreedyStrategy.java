package pyrite.core.strategy;

import de.jg3d.Edge;
import de.jg3d.Graph;
import de.jg3d.Node;
import de.jg3d.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import pyrite.core.ConvexUniformHoneycomb;
import pyrite.core.FoldingStrategy;
import pyrite.core.GraphFolder;
import pyrite.core.GraphUtils;

public class GreedyStrategy implements FoldingStrategy {

    private final ConvexUniformHoneycomb honeycomb;
    private final double[] inputValueWeights;
    private Node kernel = null;

    public GreedyStrategy(ConvexUniformHoneycomb h) {
        this(h, new double[]{1, 1, 1, 1});
    }

    public GreedyStrategy(ConvexUniformHoneycomb h, double[] inputValueWeights) {
        this.honeycomb = h;
        this.inputValueWeights = inputValueWeights;
    }

    @Override
    public boolean hasStaticTraversal() {
        return false;
    }
    int k = 0;

    @Override
    public Edge getNextEdge(Graph g) {
        k++;
        if (kernel == null) {
            kernel = GraphUtils.setKernel(g, honeycomb);
        }
        if (k > g.getEdges().size() * 2) {
            return null;
        }

        double d = Double.MAX_VALUE;
        Edge sel = null;
        for (Edge e : g.getEdges()) {
            if (e.getSource().isFixed() && !e.getDestination().isFixed()) {
                double dk = 0;
//                dk = e.getSource().getPos().distance(e.getDestination().getPos());
                dk = kernel.getPos().distance(e.getSource().getPos().midpoint(e.getDestination().getPos()));
//                dk = e.getSource().getAdjacencies().size() + e.getDestination().getAdjacencies().size();
                if (sel == null || dk < d) {
                    d = dk;
                    sel = e;
                }
            }
        }
//        d = Double.MAX_VALUE;
//        if (sel == null) {
//            for (Edge e : g.getEdges()) {
//                if (e.getSource().isFixed() && e.getDestination().isFixed() && !honeycomb.isSatisfied(e)) {
//                    double dk = 0;
////                dk = e.getSource().getPos().distance(e.getDestination().getPos());
//                    dk = kernel.getPos().distance(e.getSource().getPos().midpoint(e.getDestination().getPos()));
//                    if (sel == null || dk < d) {
//                        d = dk;
//                        sel = e;
//                    }
//                }
//            }
//        }

        return sel;
    }

    @Override
    public List<Edge> getTraversing(Graph g) {
        kernel = GraphUtils.setKernel(g, honeycomb);

        if (kernel == null) {
            return new ArrayList<>();
        }

        List<Edge> elist = new ArrayList<>(g.getEdges());

        Collections.sort(elist, (Edge e1, Edge e2) -> {
            double e1m = kernel.getPos().distance(e1.getSource().getPos().midpoint(e1.getDestination().getPos()));
            double e2m = kernel.getPos().distance(e2.getSource().getPos().midpoint(e2.getDestination().getPos()));
            return e1m == e2m ? 0 : (e1m < e2m ? -1 : 1);
        });

        for (Edge e : elist) {
            System.out.println(kernel.getPos().distance(e.getSource().getPos().midpoint(e.getDestination().getPos())));
        }
        return elist;
    }

    @Override
    public double[] decisionMaker(double[] perception) {
        int nbCount = honeycomb.getMaxNeighborCount();
        return java.util.Arrays.copyOf(perception, nbCount * 2);
    }

    @Override
    public double[] generatePerception(Graph g, Edge e) {
        Node fixed = e.getDestination().isFixed() ? e.getDestination() : e.getSource();
        if (!e.getSource().isFixed() && !e.getDestination().isFixed()) {
            if (e.getSource().getPos().distance(kernel.getPos()) < e.getDestination().getPos().distance(kernel.getPos())) {
                fixed = e.getSource();
            } else {
                fixed = e.getDestination();
            }
        }
        List<Vector> nb = honeycomb.getNeighborhood(fixed.getPos());

        int size = nb.size();
        double[] perception = new double[size * 2 + 1];

        /*
         [!] The value of a perceptor is directly proportional to the amount of
         satisfied connections to be satisfied, and vary inversaly to the
         distance from the graph kernel and the sum of non-satisfied
         neighbor connections.
         */
        int i = 0;
        for (Vector v : nb) {
            if (honeycomb.getNode(v) != null) {
                perception[i] = 0;
                perception[i + size] = 0;
            } else if (e.getSource().isFixed() && e.getDestination().isFixed()) {
                perception[i] = 0;
                perception[i + size] = 0;//e.getLength() / (v.distance(fixed.getPos()) + v.distance(e.getOther(fixed).getPos()));

            } else {

                double data[] = new double[5];
                data[0] = honeycomb.getSatisfiedConnectionCount(e, v) / honeycomb.getMaximumSatisfiedConnectionCount();
                data[1] = honeycomb.getUnsolvedNeighborConnectionCount(e, v) / honeycomb.getMaximumUnsolvedNeighborConnectionCount();
                data[2] = GraphUtils.getVolume(g, honeycomb) / GraphUtils.getNewVolumeWithPoint(g, honeycomb, null, v)[0];
                data[3] = e.getLength() / (v.distance(fixed.getPos()) + v.distance(e.getOther(fixed).getPos()));
                data[4] = 1 / kernel.getPos().distance(v);

                perception[i] = computeInputValue(data, inputValueWeights);
                perception[i + size] = 0;
            }

            i++;
        }

        return perception;
    }

    public double computeInputValue(double[] data, double[] weights) {
        double value = 0;
        for (int i = 0; i < data.length; i++) {
            value += GraphFolder.check(data[i], i) * weights[i];
        }
//        System.out.println(Arrays.toString(data));
//        System.out.println(Arrays.toString(weights));
//        System.out.println("V: " + value);
        return value;
    }

    @Override
    public double performAction(double[] actionsScore, Graph g, Edge e) {
        int id = -1;
        double maxVal = 0;
        for (int i = 0; i < actionsScore.length; i++) {
            double b = actionsScore[i];
            if (b > maxVal) {
                maxVal = b;
                id = i;
            }
        }

        if (id < 0) {
//            if (honeycomb.isSatisfied(e)) {
//                System.err.println("Already solved");
//            } else {
//                System.err.println("Impossible move =(");
//            }
            throw new RuntimeException("impossible Move");
//            return -1;
        }

        int nbCount = honeycomb.getMaxNeighborCount();
        double actionScore = 0;

        Node fixed = e.getDestination().isFixed() ? e.getDestination() : e.getSource();
        Vector fPos = fixed.getPos();
        if (!fixed.isFixed()) {
            fPos = honeycomb.getClosestNeighbor(fPos);
        }

        Vector pos;
        if (id >= nbCount) {
            //addNode
            pos = honeycomb.getNeighborhood(fPos).get(id - nbCount);
            Node newNode = GraphUtils.addNodeBetween(g, fixed, e.getOther(fixed), new Node());
            newNode.setFixed(true);
            newNode.setPos(pos);
            honeycomb.setNodePlaced(pos, newNode);
        } else {
            //fixNode
            pos = honeycomb.getNeighborhood(fPos).get(id);
            e.getOther(fixed).setFixed(true);
            e.getOther(fixed).setPos(pos);
            honeycomb.setNodePlaced(pos, e.getOther(fixed));
        }
        return actionScore;
    }
}
