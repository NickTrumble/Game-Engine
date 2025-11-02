import java.awt.*;
import java.util.*;
import javax.swing.*;

public class Engine extends JPanel{
    public static int panelSize = 800;
    ArrayList<object> objects;

    Vector lightPos = new Vector(0, 0, -5);
    Vector camDir = new Vector(0, 0, -100);
    Cube cube;

    public Engine() {

        objects = new ArrayList<>();

        cube = new Cube(0 , 0, 5, Color.ORANGE, 6, 30, 0, 0);
        objects.add(cube);
        repaint();

        new javax.swing.Timer(16, e -> {
            cube.yaw += 2;
            cube.pitch += 1;
            cube.roll -= 2;
            cube.faces = cube.generateFaces(6);
            repaint();
        }).start();

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
            int[] xpoints = new int[face.vertices.size()];
            int[] ypoints = new int[face.vertices.size()];

            for (int i = 0; i < face.vertices.size(); i++){
                Point p = project(face.vertices.get(i));
                xpoints[i] = p.x;
                ypoints[i] = p.y;
            }
            Color base = Color.orange;
            Color shadedColour = Shader(face, base);

            g2d.setColor(shadedColour);
            g2d.fillPolygon(xpoints, ypoints, face.vertices.size());
            g2d.setColor(Color.black);
            //g2d.drawPolygon(xpoints, ypoints, face.vertices.size());

        }
        //DEBUG
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

class object{
    public double x;
    public double y;
    public double z;

    public Color colour;
    public ArrayList<Face> faces;

    public object(double x, double y, double z, Color colour){
        this.x = x;
        this.y = y;
        this.z = z;

        this.colour = colour;

        //faces
    }
}

class Cube extends object{

    public int yaw; // around y axis
    public int pitch; // around x axis
    public int roll; // around z axis

    public Vector[] vertices;

    public Cube(double x, double y, double z, Color colour, int width, int yaw, int pitch, int roll) {
        super(x, y, z, colour);

        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;

        this.vertices = generateVertices(width);
        this.faces = generateFaces(width);
    }

    private Vector[] generateVertices(int width){
        double rad = width / 2.0;
        ArrayList<Vector> vertices = new ArrayList<>();
        /*
        (-1, -1, -1)
        (-1, -1, 1)
        (-1, 1, -1)
        (-1, 1, 1)
        (1, -1, -1)
        (1, -1, 1)
        (1, 1, -1)
        (1, 1, 1)
         */
        for (int i = -1; i < 2; i++){
            for (int j = -1; j < 2; j++){
                for (int k = -1; k < 2; k++){
                    if (i == 0 || j == 0 || k == 0){
                        continue;
                    }
                    //add angles
                    Vector V = new Vector(
                            rad * i,
                            rad * j,
                            rad * k
                    );

                    V = V.rotateY(yaw).rotateX(pitch).rotateZ(roll);
                    V = Vector.add(V, new Vector(x, y, z));
                    vertices.add(V);
                }

            }
        }
        return vertices.toArray(new Vector[0]);
    }

    ArrayList<Face> generateFaces(int width){
        ArrayList<Face> faces = new ArrayList<>();

        Vector[] vertices = generateVertices(width);

        //FACES
        int[][] faceIndices = {
                {0, 4, 6}, {0, 6, 2},
                {1, 3, 7}, {1, 7, 5},
                {0, 2, 3}, {0, 3, 1},
                {4, 5, 7}, {4, 7, 6},
                {2, 6, 7}, {2, 7, 3},
                {0, 1, 5}, {0, 5, 4}
        };

        Vector[] normals = new Vector[faceIndices.length];
        for (int i = 0; i < faceIndices.length; i++){
            normals[i] = generateNormals(new Vector[]{
                    vertices[faceIndices[i][0]],
                    vertices[faceIndices[i][1]],
                    vertices[faceIndices[i][2]]
            });
        }

//        Vector cubeCentre = new Vector(x, y, z);

        for (int i = 0; i < 12; i++) {
            ArrayList<Vector> vert = new ArrayList<>();
            vert.add(vertices[faceIndices[i][0]]);
            vert.add(vertices[faceIndices[i][1]]);
            vert.add(vertices[faceIndices[i][2]]);

            faces.add(new Face(normals[i], vert));

//            Vector faceToCube = Vector.subtract(cubeCentre, faces.get(i).centre);
//            if (Vector.dot(faceToCube, faces.get(i).normal) > 0){
//                faces.get(i).normal = faces.get(i).normal.multiply(-1);
//            }

        }
        return faces;
    }

    private Vector generateNormals(Vector[] vertices) {
        Vector u = Vector.subtract(vertices[1], vertices[0]);
        Vector v = Vector.subtract(vertices[2], vertices[0]);
        return Vector.cross(v, u).normalise();
    }
}

class Face {
    public ArrayList<Vector> vertices;
    public Vector normal;
    public Vector centre;

    public Face(Vector normal, ArrayList<Vector> vertices){
        this.normal = normal;
        this.vertices = vertices;

        this.centre = getCentre();
    }

    private Vector getCentre(){
        Vector sum = new Vector(0, 0, 0);
        for (Vector v : vertices){
            sum = Vector.add(sum, v);
        }
        sum = sum.multiply(1.0 / (long) vertices.size());

        return sum;
    }
}

class Vector{
    //RIGHT - UP - FORWARDS
    public double x;
    public double y;
    public double z;

    public Vector(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector multiply(double num){
        return new Vector(this.x * num, this.y * num, this.z * num);
    }

    public static Vector add(Vector V, Vector V2){
        return new Vector(V.x + V2.x, V.y + V2.y, V.z + V2.z);
    }

    public static Vector subtract(Vector V, Vector V2){
        return new Vector(V.x - V2.x, V.y - V2.y, V.z - V2.z);
    }

    public static double dot(Vector V1, Vector V2){
        return V1.x * V2.x + V1.y * V2.y + V1.z * V2.z;
    }

    public Vector rotateX(double angle){
        double rad = Math.toRadians(angle);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double newY = this.y * cos - this.z * sin;
        double newZ = this.y * sin + this.z * cos;
        return new Vector(this.x, newY, newZ);
    }

    public Vector rotateY(double angle) {
        double rad = Math.toRadians(angle);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double newX = this.x * cos + this.z * sin;
        double newZ = -this.x * sin + this.z * cos;
        return new Vector(newX, this.y, newZ);
    }

    public Vector rotateZ(double angle) {
        double rad = Math.toRadians(angle);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double newX = this.x * cos - this.y * sin;
        double newY = this.x * sin + this.y * cos;
        return new Vector(newX, newY, this.z);
    }

    public Vector normalise(){
        double mag = Math.sqrt(x * x + y * y + z * z);
        if (mag == 0)
            return new Vector(0, 0, 0);
        return new Vector(
                x / mag,
                y / mag,
                z / mag
        );
    }

    public static Vector cross(Vector V1, Vector V2){
        return new Vector(
                V1.y * V2.z - V1.z * V2.y,
                V1.z * V2.x - V1.x * V2.z,
                V1.x * V2.y - V1.y * V2.x
        );
    }
}

