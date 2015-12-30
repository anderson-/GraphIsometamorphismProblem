package pyrite.core;

import de.jg3d.Edge;
import de.jg3d.Graph;
import java.util.Arrays;
import java.util.List;
import pyrite.Main;

public class GraphFolder {

    static final int NODE_TYPE_NEW_EXTENSION = 0;
    static final int NODE_TYPE_EXTENSION = 1;
    static final int NODE_TYPE_COMPONENT = 2;

    public static double fold(Graph g, FoldingStrategy d, ConvexUniformHoneycomb h) {
        long t = System.currentTimeMillis();
        int startingNodeCount = g.getNodes().size();
        int startingEdgeCount = g.getEdges().size();
        double score = 0;
        if (d.hasStaticTraversal()) {
            List<Edge> traversing = d.getTraversing(g);
            for (Edge e : traversing) {
                if (!h.isSatisfied(e)) {
                    synchronized (g) {
                        score += d.performAction(d.decisionMaker(d.generatePerception(g, e)), g, e);
                    }
                    delay();
                    int k = 0;
                    for (Edge w : g.getEdges()) {
                        k += h.isSatisfied(w) ? 1 : 0;
                    }
//                    System.out.printf("k = %d %%, added %d, satisfied = %d, unsatisfied = %d\n", 100 * k / g.getEdges().size(), g.getNodes().size() - startingNodeCount, k, g.getEdges().size() - k);
                }
            }
        } else {
            Edge e;
            while ((e = d.getNextEdge(g)) != null) {
                if (!h.isSatisfied(e)) {
                    score += d.performAction(d.decisionMaker(d.generatePerception(g, e)), g, e);
                    int k = 0;
                    for (Edge w : g.getEdges()) {
                        k += h.isSatisfied(w) ? 1 : 0;
                    }
                    Main.print("{clear}");
                    Main.print(String.format("Starting Nodes: %d\nAdded: %d\nSatisfied: %d (%.2f %%)\nUnsatisfied: %d\nVolume: %f\n", startingNodeCount, g.getNodes().size() - startingNodeCount, k, 100f * k / g.getEdges().size(), g.getEdges().size() - k, GraphUtils.getVolume(g, h)));
//                    System.out.printf("k = %d %%, added %d, satisfied = %d, unsatisfied = %d\n", 100 * k / g.getEdges().size(), g.getNodes().size() - startingNodeCount, k, g.getEdges().size() - k);
                    waitForEqui(g);
                    delay(5);
                }
            }
        }
        t = System.currentTimeMillis() - t;
        double finalScore = getScore(g, h, startingNodeCount, startingEdgeCount);
        Main.print("{clear}");
        Main.print(String.format("Completed in %.2f s, Score: %.4f", t / 1000f, finalScore * 100));
        int k = 0;
        for (Edge w : g.getEdges()) {
            k += h.isSatisfied(w) ? 1 : 0;
        }
        Main.print(String.format("Starting Nodes: %d\nAdded: %d\nSatisfied: %d (%.2f %%)\nUnsatisfied: %d\nVolume: %f\n", startingNodeCount, g.getNodes().size() - startingNodeCount, k / 2, 100f * k / g.getEdges().size(), (g.getEdges().size() - k) / 2, GraphUtils.getVolume(g, h)));
        return finalScore;
    }

    public static void waitForEqui(Graph g) {
//        for (Edge e : g.getEdges()) {
//            e.setWeight(e.getWeight() + .1);
//        }
        int i = 0;
        do {
            i++;
            delay(5);
        } while (g.getKE() > 50);
//        System.out.println(g.getKE() + " " + i);
    }

    public static void proximateDelay(Graph g) {
        delay((int) (Math.log10(g.getKE()) / Math.tan(0.08266)));
    }

    public static void delay() {
        delay(80);
    }

    public static void delay(int t) {
        try {
            Thread.sleep(t);
        } catch (Exception e) {
        }
    }

    private static double getScore(Graph g, ConvexUniformHoneycomb h, int startingNodeCount, int startingEdgeCount) {
        double[] normalizedSubScore = new double[]{0, 0, 0, 0, 0, 0, 0};
        double[] weights = new double[]{
            .03, //score center-based distribution
            .20, //score density
            .07, //score added nodes
            .10, //score fixed nodes
            .10, //score lose nodes
            .50, //score unsatisfied connections
            0 //step scores
        };

        double finalNodeCount = g.getNodes().size();
        double finalEdgeCount = g.getEdges().size();

        //score center-based distribution
        normalizedSubScore[0] = GraphUtils.scoreDistribution(g, h, GraphUtils.getDefaultKernelPos(), 30);

        //score density
        normalizedSubScore[1] = finalNodeCount / (8 * GraphUtils.getVolume(g, h));

        //score added nodes
        normalizedSubScore[2] = (2. * startingNodeCount - finalNodeCount) / startingNodeCount;

        //score fixed nodes
        normalizedSubScore[3] = (2. * startingNodeCount - GraphUtils.countPlacedNodes(g)) / startingNodeCount;

        //score lose nodes
        normalizedSubScore[4] = GraphUtils.countPlacedNodes(g) / finalNodeCount;

        //score unsatisfied connections
        normalizedSubScore[5] = GraphUtils.countSatisfiedConnections(g, h) / finalEdgeCount;

        //step scores
        normalizedSubScore[6] = 0;

//        System.out.println(Arrays.toString(normalizedSubScore));
        double finalSocre = 0;
        for (int i = 0; i < normalizedSubScore.length; i++) {
            finalSocre += check(normalizedSubScore[i] * weights[i], i);
        }
        return finalSocre;
    }

    public static double check(double v, int id) {
        if (v > 1 || v < 0) {
//            throw new RuntimeException("Invalid Value: " + v + "[" + id + "]");
        }
        return v;
    }
}
