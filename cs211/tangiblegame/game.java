package ch.epfl.cs211;

import processing.core.*;
import processing.event.*;
import processing.video.Capture;

import static java.lang.Math.toRadians;

import java.util.ArrayList;

public class game extends PApplet {


    //Number of Frame per Seconds
    final float fps = 60.0f;

    //Multiplying factor for the velocity changes
    final float timeFactor = 1.0f / fps;

    //Gravitation constant
    final float g = 9.81f;

    //Elasticity used for bounces, 1 = no energy loss
    final float elasticity = 0.97f;

    //Box parameters
    final float boxThickness = 20.0f;
    final float boxWidth = 400.0f;
    final float boxHeight = 400.0f;
    final int boxColor = 0xffB404A9;

    //Sphere parameters
    final float sphereRadius = 24.0f;
    final float sphereOffset = -sphereRadius - boxThickness / 2.0f;
    final int sphereColor = 0xff1D10E0;

    //Cylinder parameters
    final float cylinderBaseSize = 50;
    final float cylinderHeight = 50;
    final float cylinderOffset = -boxThickness / 2.0f;
    final int cylinderResolution = 40;
    PShape openCylinder = new PShape();
    PShape topCylinder = new PShape();
    PShape bottomCylinder = new PShape();
    PShape tree = new PShape();

    //Maximum value in degree for the angles along X and Z
    final float maxTilt = 60.0f;

    //Mouse wheel rotation speed
    final float maxSpeed = 1.5f;
    final float minSpeed = 0.2f;
    final float speedScaling = 0.1f;

    //Constant for Y angle with keyboard
    final float speedFactor = 2;

    //Variables
    float speed = 1.0f;
    float angleY = 0.0f;
    float tiltAngleX = 0.0f;
    float tiltAngleZ = 0.0f;

    Mover mover = new Mover();
    boolean addingCylinderMode = false;
    boolean ignoreYRotation = true;

    ArrayList<Cylinder> cylinders = new ArrayList<>();

    final float ratio = 1.0f / 5.0f;
    final int bottomPanelColor = 0xff4EA042;
    final int space = 20;
    final int blankWidth = 10;
    final int scoreBoxWidth = 100;
    BottomPanel bottomPanel;

    float score = 0;
    float lastScore = 0;

    public void setAngles(PVector rot) {
        tiltAngleX = degrees(rot.x);
        angleY = degrees(rot.y);
        tiltAngleZ = degrees(rot.z);
    }

    public void setup() {
        size(1280, 800, P3D);
        tree = loadShape("tree.obj");
        tree.scale(20);

        bottomPanel = new BottomPanel(width, (int) (height * ratio), scoreBoxWidth, (int) (height * ratio), bottomPanelColor, space, ratio, blankWidth);

        createCylinderShape(cylinderBaseSize, cylinderHeight);

        noStroke();
    }

    public void draw() {
        pushMatrix();
        background(200);
        bottomPanel.drawBottomPanel();

        fill(0);

        //Information in upper left corner
        text("RotationX: " + tiltAngleX, 5, 10);
        if (ignoreYRotation) {
            text("RotationY: DISABLED", 5, 25);
        } else {
            text("RotationY: " + angleY, 5, 25);
        }
        text("RotationZ: " + tiltAngleZ, 5, 40);
        text("Speed: " + speed, 5, 55);
        text("sphereCoordinates: " + mover.sphere.coordinates, 5, 70);
        text("SphereVelocity: " + mover.sphere.velocity, 5, 85);


        lights();
        translate(width / 2, height / 2, 0);        //Set the matrix in the middle of the screen

        placeBoxAndSphere();

        if (addingCylinderMode) {  //replace the cursor with a transparent cylinder only if the mouse is in the box
            cursorCylinder();        //else display the transparent cylinder on the edge (at least one pixel on the board)
        } else {                   //and display the cursor where it is.
            cursor();
        }

        placeCylinders();
        popMatrix();
    }


    //Move the box along X and Y
    public void mouseDragged() {
        if (mouseY < height - bottomPanel.h) {
            if (!addingCylinderMode) {
                tiltAngleX -= speed * (mouseY - pmouseY) * 2 * maxTilt / height;  //Same thing as a map(0,width,-60,60) but no probleme when
                tiltAngleZ += speed * (mouseX - pmouseX) * 2 * maxTilt / width;   //you move the mouse between 2 drags

                //Check if the Angle are bigger than the maxTilt
                if (tiltAngleX > +maxTilt) tiltAngleX = +maxTilt;
                if (tiltAngleX < -maxTilt) tiltAngleX = -maxTilt;
                if (tiltAngleZ > +maxTilt) tiltAngleZ = +maxTilt;
                if (tiltAngleZ < -maxTilt) tiltAngleZ = -maxTilt;
            }
        } else {
            bottomPanel.scrollbar.update();
        }
    }

    //Rotation along Y, entering addingCylinderMode and toogle rotation along Y
    public void keyPressed() {
        if (key == CODED) {
            if (keyCode == LEFT && !addingCylinderMode && !ignoreYRotation)
                angleY -= speedFactor * speed;
            if (keyCode == RIGHT && !addingCylinderMode && !ignoreYRotation)
                angleY += speedFactor * speed;
            if (keyCode == SHIFT)
                addingCylinderMode = true;
        } else {
            if (key == 'y') {
                ignoreYRotation = !ignoreYRotation;
            }
        }

        if (angleY >= 360) //to display a nice angle between 0\u00b0 and 360\u00b0
            angleY -= 360;
        if (angleY < 0)
            angleY += 360;
    }

    //Rotation speed increase or decrease with mouseWheel
    public void mouseWheel(MouseEvent event) {
        if (!addingCylinderMode) {
            if (event.getCount() < 0) {
                if (speed < maxSpeed)
                    speed += speedScaling;
            } else {
                if (speed > minSpeed)
                    speed -= speedScaling;
            }
            speed = Math.round(10 * speed); //to have one digit float
            speed /= 10;
        }
    }

    //Leave addingCylinderMode when releasing SHIFT key
    public void keyReleased() {
        if (key == CODED) {
            if (keyCode == SHIFT)
                addingCylinderMode = false;
        }
    }

    //Add cylinder ONLY if it will create no collision
    public void mouseClicked() {
        if (addingCylinderMode && cylinderCheckBall(mouseX - width / 2, mouseY - height / 2)) {
            PVector coords = cylinderCheckEdges(mouseX - width / 2, mouseY - height / 2);
            cylinders.add(new Cylinder(coords.x, coords.y));
        }
    }

    //Place the box and sphere according to the different modes
//Also call the update methode to update the sphere coordinates
    public void placeBoxAndSphere() {
        if (!addingCylinderMode) {
            if (!ignoreYRotation) {
                rotateY((float) toRadians(angleY));     //Rotation along Y
            }
            rotateX((float) toRadians(tiltAngleX)); //Rotation along X
            rotateZ((float) toRadians(tiltAngleZ)); //Rotation along Z
        } else {
            rotateX(-HALF_PI); //make the box face us
        }
        fill(boxColor);
        box(boxWidth, boxThickness, boxHeight); //Creation of the box
        pushMatrix();
        mover.update();
        translate(0, sphereOffset, 0); //Offset so the sphere isn't in the box
        rotateX(HALF_PI); //to transphere 2D coordinates of the ball into 3D (y->z)
        mover.display();
        popMatrix();
    }

    //display a transparent cylinder at the mouse coordinates
    public void cursorCylinder() {
        pushMatrix();
        translate(0, cylinderOffset, 0); //Offset so the cylinder is not in the box
        rotateX(HALF_PI); //to transphere 2D coordinates of the cylinder into 3D (y->z)
        PVector coords = cylinderCheckEdges(mouseX - width / 2, mouseY - height / 2);

        if (mouseX - width / 2 == coords.x && mouseY - height / 2 == coords.y)
            noCursor(); //hide cursor if mouse in the box
        else
            cursor();  //display cursor if mouse outside the box

        Cylinder cylinder = new Cylinder(coords.x, coords.y);
        cylinder.display(); //the boolean is the noFill boolean, if true the cylinder is transparent
        popMatrix();
    }

    //Place the cylinders
    public void placeCylinders() {
        for (Cylinder c : cylinders) {
            pushMatrix();
            translate(0, cylinderOffset); //Offset so the cylinders are not in the box
            rotateX(HALF_PI);  //to transphere 2D coordinates of the cylinders into 3D (y->z)
            c.display();  //display cylinders with a color
            popMatrix();
        }
    }

    //Simple clamp function, return x if it is in the boundaries, otherwise the right boundarie
    private static int clamp(int x, int min, int max) {
        return (int) clamp((float) x, (float) min, (float) max);
    }

    //same but with floats
    private static float clamp(float x, float min, float max) {
        if (x > max)
            return max;
        else if (x < min)
            return min;
        else return x;
    }

    //Check if the cylinder at coords x and y is not in contact with the ball
//Used to avoid placing cylinder on the ball
    private boolean cylinderCheckBall(float x, float y) {
        return (mover.sphere.coordinates.dist(new PVector(x, y))) > sphereRadius + cylinderBaseSize;
    }

    //Check if the cylinder at coords x and y isn't outside the box
//If it is, return coordinates where it would be in the box
    private PVector cylinderCheckEdges(float x, float y) {
        float x2 = clamp(x, -boxWidth / 2 - sphereRadius + 1, boxWidth / 2 + sphereRadius - 1);
        float y2 = clamp(y, -boxHeight / 2 - sphereRadius + 1, boxHeight / 2 + sphereRadius - 1);
        return new PVector(x2, y2);
    }

    //Create the 3 shapes that model a cylinder
    public void createCylinderShape(float cylinderBaseSize, float cylinderHeight) {
        float angle;
        float[] x = new float[cylinderResolution + 1];
        float[] y = new float[cylinderResolution + 1];

        for (int i = 0; i < x.length; ++i) {
            angle = (TWO_PI / cylinderResolution) * i;
            x[i] = sin(angle) * cylinderBaseSize;
            y[i] = cos(angle) * cylinderBaseSize;
        }

        openCylinder = createShape();
        openCylinder.beginShape(QUAD_STRIP);
        for (int i = 0; i < x.length; ++i) {
            openCylinder.vertex(x[i], 0, y[i]);
            openCylinder.vertex(x[i], cylinderHeight, y[i]);
        }
        openCylinder.endShape();

        topCylinder = createShape();
        topCylinder.beginShape(TRIANGLES);
        for (int i = 0; i < x.length; ++i) {
            topCylinder.vertex(x[i], 0, y[i]);
            topCylinder.vertex(x[clamp(i + 1, 0, x.length - 1)], 0, y[clamp(i + 1, 0, x.length - 1)]);
            topCylinder.vertex(0, 0, 0);
        }
        topCylinder.endShape();

        bottomCylinder = createShape();
        bottomCylinder.beginShape(TRIANGLES);
        for (int i = 0; i < x.length; ++i) {
            bottomCylinder.vertex(x[i], cylinderHeight, y[i]);
            bottomCylinder.vertex(x[clamp(i + 1, 0, x.length - 1)], cylinderHeight, y[clamp(i + 1, 0, x.length - 1)]);
            bottomCylinder.vertex(0, cylinderHeight, 0);
        }
        bottomCylinder.endShape();
    }

    class BottomPanel {
        TopView topView;
        ScoreBox scoreBox;
        ScoreRecap scoreRecap;
        PGraphics bottomPanel;
        int bottomPanelWidth;
        int h;
        int bottomPanelColor;
        float ratio;
        int blankWidth;
        int HScrollHeight;
        HScrollbar scrollbar;

        BottomPanel(int bottomPanelWidth, int topViewWidth, int scoreBoxWidth, int h, int bottomPanelColor, int space, float ratio, int blankWidth) {
            this.bottomPanelWidth = bottomPanelWidth;
            this.h = h;
            this.bottomPanelColor = bottomPanelColor;
            this.ratio = ratio;
            this.blankWidth = blankWidth;
            this.HScrollHeight = 4 * blankWidth;

            bottomPanel = createGraphics(bottomPanelWidth, h, P2D);
            bottomPanel.beginDraw();
            bottomPanel.background(bottomPanelColor);
            bottomPanel.endDraw();

            topView = new TopView(topViewWidth, h - 2 * blankWidth, bottomPanelColor);
            scoreBox = new ScoreBox(scoreBoxWidth, h - 2 * blankWidth, ratio, space, bottomPanelColor);
            scoreRecap = new ScoreRecap(width - topViewWidth - scoreBoxWidth, h - 2 * blankWidth - HScrollHeight, bottomPanelColor, ratio);
            scrollbar = new HScrollbar(topView.topViewWidth + scoreBox.scoreBoxWidth + 4 * blankWidth, height - h + blankWidth + scoreRecap.h, width - topViewWidth - scoreBoxWidth - 6 * blankWidth, HScrollHeight);
            scrollbar.sliderPosition = scrollbar.xPosition;
        }

        public void drawBottomPanel() {
            //Adding score to array
            image(bottomPanel, 0, height - h);
            image(topView.drawTopView(), blankWidth, height - h + blankWidth);
            image(scoreBox.drawScoreBox(), topView.topViewWidth + 2 * blankWidth, height - h + blankWidth);
            image(scoreRecap.drawScoreRecap(), topView.topViewWidth + scoreBox.scoreBoxWidth + 4 * blankWidth, height - h + blankWidth);
            scrollbar.display();
        }
    }

    class Cylinder {
        PVector coordinates = new PVector(0, 0);
        int c = color(0, 150, 0);//color(random(0,255), random(0,255), random(0,255));

        Cylinder(float x, float y) {
            coordinates.x = x;
            coordinates.y = y;
        }

        //display a cylinder with or without a color
        //at the right coordinates
        public void display() {
            pushMatrix();
            translate(coordinates.x, coordinates.y);
            rotateX(HALF_PI); //makes the cylinder on the board in the right angle

            shape(tree);
            popMatrix();
        }
    }

    class HScrollbar {
        float barWidth;  //Bar's width in pixels
        float barHeight; //Bar's height in pixels
        float xPosition;  //Bar's x position in pixels
        float yPosition;  //Bar's y position in pixels

        float sliderPosition, newSliderPosition;    //Position of slider
        float sliderPositionMin, sliderPositionMax; //Max and min values of slider

        boolean mouseOver;  //Is the mouse over the slider?
        boolean locked;     //Is the mouse clicking and dragging the slider now?

        /**
         * Creates a new horizontal scrollbar
         *
         * @param x The x position of the top left corner of the bar in pixels
         * @param y The y position of the top left corner of the bar in pixels
         * @param w The width of the bar in pixels
         * @param h The height of the bar in pixels
         */
        HScrollbar(float x, float y, float w, float h) {
            barWidth = w;
            barHeight = h;
            xPosition = x;
            yPosition = y;

            sliderPosition = xPosition + barWidth / 2 - barHeight / 2;
            newSliderPosition = sliderPosition;

            sliderPositionMin = xPosition;
            sliderPositionMax = xPosition + barWidth - barHeight;
        }

        /**
         * Updates the state of the scrollbar according to the mouse movement
         */
        public void update() {
            mouseOver = isMouseOver();

            if (mousePressed && mouseOver) {
                locked = true;
            }
            if (!mousePressed) {
                locked = false;
            }
            if (locked) {
                newSliderPosition = constrain(mouseX - barHeight / 2, sliderPositionMin, sliderPositionMax);
            }
            if (abs(newSliderPosition - sliderPosition) > 1) {
                sliderPosition = sliderPosition + (newSliderPosition - sliderPosition);
            }
        }

        /**
         * Clamps the value into the interval
         *
         * @param val    The value to be clamped
         * @param minVal Smallest value possible
         * @param maxVal Largest value possible
         * @return val clamped into the interval [minVal, maxVal]
         */
        public float constrain(float val, float minVal, float maxVal) {
            return min(max(val, minVal), maxVal);
        }

        /**
         * Gets whether the mouse is hovering the scrollbar
         *
         * @return Whether the mouse is hovering the scrollbar
         */
        public boolean isMouseOver() {
            return mouseX > xPosition && mouseX < xPosition + barWidth && mouseY > yPosition && mouseY < yPosition + barHeight;
        }

        /**
         * Draws the scrollbar in its current state
         */
        public void display() {
            noStroke();
            fill(204);
            rect(xPosition, yPosition, barWidth, barHeight);
            if (mouseOver || locked) {
                fill(0, 0, 0);
            } else {
                fill(102, 102, 102);
            }
            rect(sliderPosition, yPosition, barHeight, barHeight);
        }

        /**
         * Gets the slider position
         *
         * @return The slider position in the interval [0,1] corresponding to [leftmost position, rightmost position]
         */
        public float getPos() {
            return (sliderPosition - xPosition) / (barWidth - barHeight);
        }
    }

    class Mover {  //One class for all things that move
        Sphere sphere = new Sphere();

        //display the sphere
        public void display() {
            sphere.display();
        }

        //update the sphere
        public void update() {
            if (!addingCylinderMode) {
                sphere.updateCoordinates();
            }
            sphere.checkCollisions();
        }
    }

    class ScoreBox {
        int scoreBoxWidth;
        int h;
        PGraphics graphic;
        float ratio;
        int space;
        int c;

        ScoreBox(int scoreBoxWidth, int h, float ratio, int space, int c) {
            this.scoreBoxWidth = scoreBoxWidth;
            this.h = h;
            this.ratio = ratio;
            this.space = space;
            this.c = c;
            graphic = createGraphics(scoreBoxWidth, h, P2D);
        }

        public PGraphics drawScoreBox() {
            graphic.beginDraw();
            graphic.background(c);
            graphic.pushMatrix();
            //White rect
            graphic.noFill();
            graphic.strokeWeight(4);
            graphic.stroke(255);
            graphic.rect(0, 0, scoreBoxWidth, h);
            graphic.fill(0);
            graphic.translate(space, 0);

            //score display
            graphic.pushMatrix();
            graphic.translate(0, space + 4); //4=stroke weight
            graphic.text("Total Score:", 0, 0);
            graphic.translate(0, 8 * 2); //8 = font size
            graphic.text(score, 0, 0);
            graphic.popMatrix();

            //velocity
            graphic.pushMatrix();
            graphic.translate(0, h / 2.0f - 8);
            graphic.text("Velocity", 0, 0);
            graphic.text(mover.sphere.velocity.mag(), 0, 8 * 2);
            graphic.popMatrix();

            //last score
            graphic.pushMatrix();
            graphic.translate(0, h - space - 4 - 8); //4=stroke weight
            graphic.text("Last Score:", 0, 0);
            graphic.translate(0, 8 * 2); //8 = font size
            graphic.text(lastScore, 0, 0);
            graphic.popMatrix();
            graphic.noStroke();
            graphic.popMatrix();

            graphic.endDraw();

            return graphic;
        }
    }

    class ScoreRecap {
        PGraphics graphic;
        int scoreRecapWidth;
        int h;
        int c;
        float ratio;
        float factor = 1f;

        final int updatingRate = 10;
        int updatingCounter = 0;
        int scoreRecapSize = 0;
        ArrayList<Float> scoreRecap = new ArrayList<>();
        int sWidth;
        int numCol;

        ScoreRecap(int scoreRecapWidth, int h, int c, float ratio) {
            this.scoreRecapWidth = scoreRecapWidth;
            this.h = h;
            sWidth = h / 20;
            this.c = c;
            this.ratio = ratio;
            graphic = createGraphics(scoreRecapWidth, h, P2D);
            numCol = scoreRecapWidth / sWidth;
        }

        public PGraphics drawScoreRecap() {
            if (!addingCylinderMode) {
                updatingCounter += 1;
                if (updatingCounter > updatingRate) {
                    ++scoreRecapSize;
                    updatingCounter -= updatingRate;
                    scoreRecap.add(score);
                }
            }
            graphic.beginDraw();
            graphic.background(c);
            int i0 = round(bottomPanel.scrollbar.getPos() * numCol);
            for (int i = i0; i < scoreRecapSize; ++i) {
                if (scoreRecapSize > 10 && scoreRecap.get(scoreRecapSize - 1) / factor > h) {
                    factor *= 1.3;
                }
                graphic.pushMatrix();
                graphic.translate((i - i0) * sWidth, 0);
                graphic.rect(0, h, sWidth, -scoreRecap.get(i) / factor);
                graphic.popMatrix();
            }
            graphic.endDraw();
            return graphic;
        }
    }

    class Sphere {

        PVector coordinates;
        PVector velocity;

        //create a ball in the top left corner of the matrix,
        //it's always used in the box referencial so in the middle of the screen
        Sphere() {
            coordinates = new PVector(0, 0);
            velocity = new PVector(0, 0);
        }

        //display the ball at the right coords and in the right color
        public void display() {
            pushMatrix();
            translate(coordinates.x, coordinates.y);
            fill(sphereColor);
            sphere(sphereRadius);
            popMatrix();
        }

        //Compute the impact of the gravity and friction on the ball
        public void updateCoordinates() {
            PVector gravityForce = new PVector(0, 0);
            gravityForce.x = +sin((float) toRadians(tiltAngleZ)) * g * timeFactor; //Gravity force on X
            gravityForce.y = -sin((float) toRadians(tiltAngleX)) * g * timeFactor; //Gravity force on Z

            float normalForce = 1.0f;
            float mu = 0.01f;
            float frictionMagnitude = normalForce * mu;
            PVector friction = velocity.get();
            friction.normalize();
            friction.mult(frictionMagnitude * timeFactor); //Friction factor

            velocity.add(gravityForce); //Change velocity according to gravity
            velocity.add(friction);     //Change velocity according to friction


            coordinates.add(velocity); //Change the coordinates according to the velocity
        }

        //Test if there is a collision with an edge or a cylinder
        public void checkCollisions() {
            edgeCollision();
            cylindersCollision();
        }

        //Compute a bounce on an edge, loss of velocity with the elasticity parameter
        private void edgeCollision() {
            if (coordinates.x - sphereRadius / 2 < -boxWidth / 2) {  //LEFT
                computeCollision(new PVector(-boxWidth / 2 + sphereRadius / 2, coordinates.y)); //compute new velocity and coordinates
                coordinates.x = -boxWidth / 2 + sphereRadius / 2;  //avoid that the sphere leave the box
                velocity.mult(elasticity);  //reduce speed with velocity factor
                score -= velocity.mag();
                lastScore = -velocity.mag();
                if (score < 0) {
                    score = 0;
                }
            } else if (coordinates.x + sphereRadius / 2 > boxWidth / 2) { //RIGHT
                computeCollision(new PVector(boxWidth / 2 - sphereRadius / 2, coordinates.y));
                coordinates.x = boxWidth / 2 - sphereRadius / 2;
                velocity.mult(elasticity);
                score -= velocity.mag();
                lastScore = -velocity.mag();
                if (score < 0) {
                    score = 0;
                }
            }
            if (coordinates.y - sphereRadius / 2 < -boxHeight / 2) { //TOP
                computeCollision(new PVector(coordinates.x, -boxHeight / 2 + sphereRadius / 2));
                coordinates.y = -boxHeight / 2 + sphereRadius / 2;
                velocity.mult(elasticity);
                score -= velocity.mag();
                lastScore = -velocity.mag();
                if (score < 0) {
                    score = 0;
                }
            } else if (coordinates.y + sphereRadius / 2 > boxHeight / 2) { //BOTTOM
                computeCollision(new PVector(coordinates.x, boxHeight / 2 - sphereRadius / 2));
                coordinates.y = boxHeight / 2 - sphereRadius / 2;
                velocity.mult(elasticity);
                score -= velocity.mag();
                lastScore = -velocity.mag();
                if (score < 0) {
                    score = 0;
                }
            }
        }

        //Compute the bounce on a cylinder
        private void cylindersCollision() {
            Cylinder cylinder = new Cylinder(0, 0);
            boolean collisionHappens = false;
            for (Cylinder c : cylinders) { //find if there is a contact with a cylinder and this cylinder
                if (collisionWithCylinder(c.coordinates) && !collisionHappens) {
                    collisionHappens = true;
                    cylinder = c;
                }
            }
            if (collisionHappens) {
                computeCollision(cylinder.coordinates); //compute the changes
                score += 3 * velocity.mag();
                lastScore = 3 * velocity.mag();
            }
        }


        //Compute the velocity and coords changes of the sphere
        private void computeCollision(PVector coords) {
            PVector n = coords.get();
            n.sub(coordinates);
            n.normalize();  //get normal vector
            float c = n.x * velocity.x + n.y * velocity.y; //this is the dot product
            PVector V1NN = n.get();  //this is 2 * (v1 dot n) * n
            V1NN.mult(2);
            V1NN.mult(c);
            PVector v2 = velocity.get();
            v2.sub(V1NN);  //this is v1 - 2 * (v1 dot n) * n
            velocity.set(v2);
            velocity.mult(elasticity);
            coordinates.sub(n);  //prevents the cylinder to get in the cylinder it collided with
        }


        //check if there is a collision between the ball and a given cylinder
        //true if the dist between the 2 is less than the sum of the radius
        private boolean collisionWithCylinder(PVector cylinderCoordinates) {
            return coordinates.dist(cylinderCoordinates) <= sphereRadius + cylinderBaseSize;
        }
    }

    class TopView {
        PGraphics graphic;
        int topViewWidth;
        int h;
        int c;
        float ratio;

        TopView(int topViewWidth, int h, int c) {
            this.topViewWidth = topViewWidth;
            this.h = h;
            this.c = c;

            ratio = h / boxHeight;

            graphic = createGraphics(topViewWidth, h, P2D);
        }


        public PGraphics drawTopView() {
            graphic.beginDraw();
            graphic.background(c);

            //BOX
            graphic.fill(boxColor);
            graphic.rect(0, 0, boxWidth * ratio, boxHeight * ratio);

            //BALL
            graphic.pushMatrix();
            PVector coords = mover.sphere.coordinates;
            graphic.translate(ratio * coords.x + boxWidth * ratio / 2.0f, ratio * coords.y + boxHeight * ratio / 2.0f);
            graphic.fill(sphereColor);
            graphic.ellipse(0, 0, sphereRadius * 2 * ratio, sphereRadius * 2 * ratio);
            graphic.popMatrix();

            //CYLINDERS
            for (Cylinder c : cylinders) {
                graphic.pushMatrix();
                graphic.translate(ratio * (c.coordinates.x + boxWidth / 2.0f), ratio * (c.coordinates.y + boxHeight / 2.0f));
                graphic.fill(c.c);
                graphic.ellipse(0, 0, cylinderBaseSize * 2 * ratio, cylinderBaseSize * 2 * ratio);
                graphic.popMatrix();
            }

            graphic.endDraw();

            return graphic;
        }
    }

    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[]{"game"};
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }
}
