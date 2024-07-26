package org.example;

import org.joml.Matrix4f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    private long window; //id window

    private final List<Cube> cubes = new ArrayList<>();

    private Cube currentCube;

    private boolean rotating = false;

    public void run() {
        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        //GLFW - Setting for OpenGL (working with 2D, 3D)
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(800, 600, "3D game!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS || action == GLFW_REPEAT) { //move object
                switch (key) {
                    case GLFW_KEY_UP -> currentCube.setY(0.05f);
                    case GLFW_KEY_DOWN -> currentCube.setY(-0.05f);
                    case GLFW_KEY_LEFT -> currentCube.setX(-0.05f);
                    case GLFW_KEY_RIGHT -> currentCube.setX(+0.05f);
                    case GLFW_KEY_W -> currentCube.setZ(0.05f);
                    case GLFW_KEY_S -> currentCube.setZ(-0.05f);
                    case GLFW_KEY_R -> rotating = !rotating;
                    case GLFW_KEY_ENTER -> {
                        cubes.add(currentCube);
                        currentCube = new Cube(0, 0, -5, 0);
                    }
                }
            }

            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        currentCube = new Cube(0, 0, -5, 0);

        for (int row = 0; row < 6; row++) {
            float z = -4.1f - row * 0.3f;
            for (int i = 0; i < 25; i++) {
                float x = -3.4f + i * 0.3f;
                cubes.add(new Cube(x, -1.7f, z, 0));
            }
        }

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            if (vidmode != null) {
                // Center the window
                glfwSetWindowPos(
                        window,
                        (vidmode.width() - pWidth.get(0)) / 2,
                        (vidmode.height() - pHeight.get(0)) / 2
                );
            }
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void gradientBg() {
        glMatrixMode(GL_PROJECTION); //create projection switch to 3D mode
        glPushMatrix(); //save matrix state
        glLoadIdentity(); //drop matrix to current state
        glOrtho(0, 800, 0, 600, -1, 1); //ortho projection

        glMatrixMode(GL_MODELVIEW); // switch to view mode
        glPushMatrix(); //save matrix state
        glLoadIdentity(); //drop matrix to current state

        glEnable(GL_BLEND); //mix colours
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_DST_ALPHA); //mix function

        glDepthMask(false); //off depth

        //start draw gradient
        glBegin(GL_QUADS);

        glColor3f(0.0f, 0.3f, 0.7f); //bottom
        glVertex2f(0, 0);
        glVertex2f(800, 0);

        glColor3f(0.0f, 0.7f, 1f); //top
        glVertex2f(800, 600);
        glVertex2f(0, 600);

        glEnd();

        glDepthMask(true);
        glDisable(GL_BLEND);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    private void loop() {
        //create area
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST); //set depth for 3D
        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(45.0), 800.0f / 600.0f, 0.1f, 100f); //set projection
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        projection.get(buf);

        //Set the color to area
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        //drawing
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            //set 3D projection
            glMatrixMode(GL_PROJECTION);
            glLoadMatrixf(buf);

            gradientBg();

            for (Cube cube : cubes) {
                drawCube(cube);
            }

            drawCube(currentCube);
            if (rotating) {
                currentCube.setAngle(0.5f);
            }

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void drawCube(Cube cube) {
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(cube.getX(), cube.getY(), cube.getZ()); //object coords
        glRotatef(cube.getAngle(), 0f, 1f, 0f); //rotation on Y
        glScalef(0.33f, 0.33f, 0.33f); //object size

        //object description
        glBegin(GL_QUADS);

        //front
        glColor3f(79 / 255f, 171 / 255f, 67 / 255f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);

        //back
        glColor3f(79 / 255f, 171 / 255f, 67 / 255f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);

        //top
        glColor3f(36 / 255f, 79 / 255f, 31 / 255f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);

        //bottom
        glColor3f(36 / 255f, 79 / 255f, 31 / 255f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);

        //right
        glColor3f(1, 0, 1);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);

        //left
        glColor3f(0, 1, 1);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);

        glEnd();
    }

    public static void main(String[] args) {
        new Main().run();
    }

}
