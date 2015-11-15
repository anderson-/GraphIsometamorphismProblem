package pyrite;

import de.jg3d.Graph;
import static de.jg3d.Main.createAndRun;
import de.jg3d.util.Importer;

public class Main {

    public static void main(String[] args) {
        Graph g = new Graph();
        Importer.importfile(g, "cir.txt");
    }

}
