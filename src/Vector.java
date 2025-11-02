public class Vector{
    //RIGHT - UP - FORWARDS?? I don't even know anymore
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
