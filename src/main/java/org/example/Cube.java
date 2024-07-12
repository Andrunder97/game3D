package org.example;

public class Cube {

    private float x;
    private float y;
    private float z;
    private float angle;

    public Cube(float x, float y, float z, float angle) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.angle = angle;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = getX() + x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = getY() + y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = getZ() + z;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = getAngle() + angle;
    }

}
