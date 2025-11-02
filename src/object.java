import java.awt.*;
import java.util.ArrayList;

public class object{
    public double x;
    public double y;
    public double z;

    public Color colour;
    ArrayList<Face> faces;

    public object(double x, double y, double z, Color colour){
        this.x = x;
        this.y = y;
        this.z = z;

        this.colour = colour;

        //faces
    }

    protected Vector generateNormals(Vector[] vertices) {
        Vector u = Vector.subtract(vertices[1], vertices[0]);
        Vector v = Vector.subtract(vertices[2], vertices[0]);
        return Vector.cross(v, u).normalise();
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

    protected Vector[] generateVertices(int width){
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
                {0, 4, 6}, {0, 6, 2},//back
                {1, 3, 7}, {1, 7, 5},//front
                {0, 2, 3}, {0, 3, 1},//left
                {4, 5, 7}, {4, 7, 6},//right
                {2, 6, 7}, {2, 7, 3},//top
                {0, 1, 5}, {0, 5, 4}//bottom
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
        }
        return faces;
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


