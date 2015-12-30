/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pyrite.display;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;
import de.jg3d.Edge;
import de.jg3d.Graph;
import de.jg3d.GraphMonitor;
import de.jg3d.Node;
import de.jg3d.Vector;
import java.awt.Color;
import java.util.Arrays;
import lwjglTest.MiniMain;
import pyrite.core.ConvexUniformHoneycomb;
import quick3d.DrawingPanel3D;
import quick3d.graph.Graph3D;
import quick3d.simplegraphics.Axis;
import quick3d.simplegraphics.Console;

/**
 *
 * @author anderson
 */
public class Quick3DGraphMonitor implements GraphMonitor {

    private DrawingPanel3D p3d;
    private final ConvexUniformHoneycomb honeycomb;
    private final Console console;

    public Quick3DGraphMonitor(ConvexUniformHoneycomb honeycomb) {
        this.honeycomb = honeycomb;
        console = new Console(12, 10, 15, Color.LIGHT_GRAY);
    }

    public Console getConsole() {
        return console;
    }

    @Override
    public void show(final Graph g) {
        new Thread() {
            @Override
            public void run() {
                MiniMain sim = new MiniMain(g);
                sim.runSimulation();
                p3d = new DrawingPanel3D();
                p3d.createFrame("OpenGL Graph Monitor", false);
                p3d.append(console);
                p3d.append(new Graph3D() {
                    @Override
                    public void draw(GL2 gl, GLUT glut, boolean colorPicking) {
                        gl.glPushMatrix();
                        gl.glScalef(.1f, .1f, .1f);
                        Vector p, p2;
                        int i = 1;
                        for (Node n : new java.util.ArrayList<>(g.getNodes())) {
                            p = n.getPos();

                            if (n.isFixed()) {
//                                drawCube(gl, (float) p.getX(), (float) p.getY(), (float) p.getZ(), 1f, colorPicking ? i : n.hashCode());
                                if (n.getType() == 2) {
                                    gl.glPushMatrix();
                                    DrawingPanel3D.rotateAndGoToMidPoint(gl, new float[]{(float) p.getX(), (float) p.getY(), (float) p.getZ()}, new float[]{(float) p.getX(), (float) p.getY(), (float) p.getZ()});
                                    DrawingPanel3D.drawT(gl, .4f);
                                    gl.glPopMatrix();
                                } else {
                                    gl.glPushMatrix();
                                    gl.glTranslatef((float) p.getX(), (float) p.getY(), (float) p.getZ());
                                    switch (n.getLabel()) {
                                        case "GroundElm":
                                            gl.glColor3f(0.1f, 0.1f, 0.9f);
                                            break;
                                        case "RailElm":
                                            gl.glColor3f(0.9f, 0.1f, 0.1f);
                                            break;
                                        default:
                                            gl.glColor3f(0.5f, 0.5f, 0.5f);
                                    }
                                    glut.glutSolidSphere(.6f, 6, 6);
                                    gl.glPopMatrix();
                                }
                            } else {
                                drawCube(gl, (float) p.getX(), (float) p.getY(), (float) p.getZ(), .3f, colorPicking ? i : n.hashCode());
                            }
                            i++;
                        }

                        for (Edge e : new java.util.ArrayList<>(g.getEdges())) {
                            p = e.getSource().getPos();
                            p2 = e.getDestination().getPos();

                            gl.glLineWidth(2f);
                            if (honeycomb.isSatisfied(e)) {
                                if (e.getDestination().hashCode() > e.getSource().hashCode()) {
                                    gl.glPushMatrix();
                                    DrawingPanel3D.rotateAndGoToMidPoint(gl, new float[]{(float) p.getX(), (float) p.getY(), (float) p.getZ()}, new float[]{(float) p2.getX(), (float) p2.getY(), (float) p2.getZ()});
                                    if (e.getLabel() != null && e.getLabel().startsWith("d")) {
                                        DrawingPanel3D.drawD(gl, .2f);
                                    } else if (e.getLabel() != null && e.getLabel().startsWith("r")) {
                                        String[] val = e.getLabel().split(" ");
//                                        System.out.println(val[val.length - 1] + " > " + Arrays.toString(val[val.length - 1].split("\\.")));
//                                        System.out.println(Arrays.toString(val[val.length - 1].toCharArray()));
//                                        System.out.println(val[val.length - 1].contains("."));
//                                        System.exit(0);
                                        Color[] c = resistorColors(Integer.parseInt(val[val.length - 1].split("\\.")[0]));
                                        DrawingPanel3D.drawR(gl, .2f, Color.cyan, c[0], c[1], c[2], c[3]);
                                    } else if (e.getLabel() != null && e.getLabel().startsWith("s")) {
                                        gl.glColor3f(0, .5f, 0);
                                        glut.glutSolidSphere(1f, 6, 6);
                                    } else if (e.getLabel() != null && e.getLabel().startsWith("162")) {
                                        gl.glColor3f(.5f, 0, 0);
                                        glut.glutSolidSphere(1f, 6, 6);
                                    }

                                    gl.glPopMatrix();
                                }
                                //gl.glColor3f(0.0f, 1.0f, 0.2f);
                                gl.glColor3f(0.5f, 0.5f, 0.5f);

                            } else {
                                gl.glColor3f(1.0f, 0.0f, 0.2f);
                            }

                            gl.glBegin(gl.GL_LINE_STRIP);
                            gl.glVertex3f((float) p.getX(), (float) p.getY(), (float) p.getZ());
                            gl.glVertex3f((float) p2.getX(), (float) p2.getY(), (float) p2.getZ());
                            gl.glEnd();
                        }

                        //draw box
                        {
                            double minX, minY, minZ, maxX, maxY, maxZ;
                            minX = minY = minZ = Double.POSITIVE_INFINITY;
                            maxX = maxY = maxZ = Double.NEGATIVE_INFINITY;
                            for (Node n : new java.util.ArrayList<>(g.getNodes())) {
                                if (n.isFixed()) {
                                    p = n.getPos();
                                    if (p.getX() < minX) {
                                        minX = p.getX();
                                    }
                                    if (p.getY() < minY) {
                                        minY = p.getY();
                                    }
                                    if (p.getZ() < minZ) {
                                        minZ = p.getZ();
                                    }
                                    if (p.getX() > maxX) {
                                        maxX = p.getX();
                                    }
                                    if (p.getY() > maxY) {
                                        maxY = p.getY();
                                    }
                                    if (p.getZ() > maxZ) {
                                        maxZ = p.getZ();
                                    }
                                }
                            }

                            minX--;
                            minY--;
                            minZ--;
                            maxX++;
                            maxY++;
                            maxZ++;

                            gl.glColor3f(1.0f, 0.3f, 0.8f);
                            gl.glLineWidth(4f);

                            gl.glBegin(gl.GL_LINES);
                            gl.glVertex3d(minX, minY, maxZ);
                            gl.glVertex3d(minX, minY, minZ);
                            gl.glVertex3d(minX, maxY, maxZ);
                            gl.glVertex3d(minX, maxY, minZ);
                            gl.glVertex3d(maxX, maxY, maxZ);
                            gl.glVertex3d(maxX, maxY, minZ);
                            gl.glVertex3d(maxX, minY, maxZ);
                            gl.glVertex3d(maxX, minY, minZ);

                            gl.glVertex3d(minX, minY, maxZ);
                            gl.glVertex3d(maxX, minY, maxZ);
                            gl.glVertex3d(minX, minY, minZ);
                            gl.glVertex3d(maxX, minY, minZ);
                            gl.glVertex3d(minX, maxY, maxZ);
                            gl.glVertex3d(maxX, maxY, maxZ);
                            gl.glVertex3d(minX, maxY, minZ);
                            gl.glVertex3d(maxX, maxY, minZ);

                            gl.glVertex3d(minX, minY, maxZ);
                            gl.glVertex3d(minX, maxY, maxZ);
                            gl.glVertex3d(minX, minY, minZ);
                            gl.glVertex3d(minX, maxY, minZ);
                            gl.glVertex3d(maxX, minY, maxZ);
                            gl.glVertex3d(maxX, maxY, maxZ);
                            gl.glVertex3d(maxX, minY, minZ);
                            gl.glVertex3d(maxX, maxY, minZ);
                            gl.glEnd();

                            double k = honeycomb.getShortestDistance();
                            gl.glColor3f(1.0f, 0.6f, 0.9f);

                            gl.glPushMatrix();
                            gl.glTranslated(minX + 1, minY, maxZ);
                            for (double x = minX; x <= maxX; x += k) {
                                glut.glutSolidCube(.7f);
                                gl.glTranslated(k, 0, 0);
                            }
                            gl.glPopMatrix();

                            gl.glPushMatrix();
                            gl.glTranslated(minX, minY + 1, maxZ);
                            for (double x = minY; x <= maxY; x += k) {
                                glut.glutSolidCube(.7f);
                                gl.glTranslated(0, k, 0);
                            }
                            gl.glPopMatrix();

                            gl.glPushMatrix();
                            gl.glTranslated(minX, minY, maxZ - 1);
                            for (double x = minZ; x <= maxZ; x += k) {
                                glut.glutSolidCube(.7f);
                                gl.glTranslated(0, 0, -k);
                            }
                            gl.glPopMatrix();

                        }

                        gl.glPopMatrix();
                    }
                });
            }
        }.start();
    }

    public static Color[] colors = new Color[]{Color.black, Color.decode("#835C3B"), Color.red, Color.orange, Color.yellow, Color.green, Color.cyan, Color.blue, Color.gray, Color.white};
    public static String[] cn = new String[]{"black", "brown", "red", "orange", "yellow", "green", "blue", "violet", "gray", "white"};

    public static Color[] resistorColors(int v) {
        String str = new Integer(v).toString();
        Color[] cs = new Color[4];
        cs[0] = Color.lightGray;
        cs[1] = colors[str.length() - 2];
        cs[2] = colors[Integer.parseInt("" + str.charAt(1))];
        cs[3] = colors[Integer.parseInt("" + str.charAt(0))];
        return cs;
    }

    public void saveScreenshot(String str) {
        p3d.saveScreenshot(str);
    }

    public void saveScreenshot() {
        p3d.saveScreenshot();
    }

    private static void drawCube(GL2 gl, float x, float y, float z, float scale, int i) {
        int r = (i & 0x000000FF);
        int g = (i & 0x0000FF00) >> 8;
        int b = (i & 0x00FF0000) >> 16;
        gl.glPushMatrix();
        gl.glTranslatef(x, y, z);
        gl.glScalef(scale, scale, scale);
        gl.glColor4f(r / 255.0f, g / 255.0f, b / 255.0f, 1.0f);
        gl.glBegin(gl.GL_QUADS);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glEnd();
        gl.glPopMatrix();
    }
}
