import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.*;

public class Engine extends JPanel{
    public static int panelSize = 800;
    ArrayList<object> objects;

    Vector lightPos = new Vector(0, 0, -5);
    Vector camDir = new Vector(0, 0, -100);
    Cube cube;

    boolean debug;

    public Engine() {
        debug = true;
        objects = new ArrayList<>();

        cube = new Cube(0 , 0, 5, Color.ORANGE, 6, 30, 0, 0);
        objects.add(cube);
        repaint();

        javax.swing.Timer timer = new javax.swing.Timer(16, e -> {
            cube.yaw += 2;
            cube.pitch += 1;
            cube.roll -= 2;
            cube.faces = cube.generateFaces(6);
            repaint();
        });
        timer.start();

        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_SPACE){
                    if (timer.isRunning())
                        timer.stop();
                    else
                        timer.start();
                }
                else if (e.getKeyCode() == KeyEvent.VK_D){
                    debug = !debug;
                    repaint();
                }
            }
        });

    }

    public static void main(String[] args){
        JFrame frame = new JFrame("Noise Map");
        Engine panel = new Engine();
        frame.add(panel);
        frame.setSize(panelSize, panelSize);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        ArrayList<Face> visible = new ArrayList<>();

        for (object O : objects){
            visible.addAll(visibleFaces(O));
        }

        visible.sort(Comparator.comparing(face -> -face.centre.z));

        for (Face face : visible){
            int[] xPoints = new int[face.vertices.size()];
            int[] yPoints = new int[face.vertices.size()];

            for (int i = 0; i < face.vertices.size(); i++){
                Point p = project(face.vertices.get(i));
                xPoints[i] = p.x;
                yPoints[i] = p.y;
            }
            Color base = Color.orange;
            Color shadedColour = Shader(face, base);

            g2d.setColor(shadedColour);
            g2d.fillPolygon(xPoints, yPoints, face.vertices.size());
            g2d.setColor(Color.black);
            //g2d.drawPolygon(xPoints, yPoints, face.vertices.size());
        }
        //DEBUG
        if (debug){
            for (Face face : cube.faces){
                Point c = project(face.centre);
                Vector nEnd = Vector.add(face.centre, face.normal.multiply(0.5));
                Point e = project(nEnd);
                g2d.setColor(Color.RED);
                g2d.drawLine(c.x, c.y, e.x, e.y);
                g2d.setColor(Color.BLUE);
                g2d.fillOval(e.x, e.y, 5, 5);

                Vector viewVector = Vector.subtract(face.centre, camDir);

                double dot = Vector.dot(viewVector, face.normal);

                g2d.drawString(Double.toString(dot), e.x, e.y);
            }
        }


    }

    ArrayList<Face> visibleFaces(object O){
        ArrayList<Face> visible = new ArrayList<>();

        for (Face face : O.faces){
            Vector viewVector = Vector.subtract(face.centre, camDir);

            double dot = Vector.dot(viewVector, face.normal);
            if (dot < 0){
                visible.add(face);
            }
        }

        return visible;
    }

    private Point project(Vector v) {
        double scale = 40; // scale up cube for visibility
        int screenX = (int) (v.x * scale + panelSize / 2.0 - v.z * v.x);
        int screenY = (int) (v.y * scale + panelSize / 2.0 - v.z * v.y); // Y is flipped
        return new Point(screenX, screenY);

    }

    Color Shader(Face f, Color base){
        Vector viewVector = Vector.subtract(lightPos, f.centre).normalise();
        double dot = Vector.dot(viewVector, f.normal.normalise());

        dot = 0.5 + 0.5 * Math.max(0, Math.min(1, dot));

        int r = (int) (base.getRed() * dot);
        int g = (int) (base.getGreen() * dot);
        int b = (int) (base.getBlue() * dot);

        return new Color(r, g, b);

    }

}



