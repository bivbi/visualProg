import static java.lang.Math.toRadians;

//Number of Frame per Seconds
final float fps          = 60.0;

//Multiplying factor for the velocity changes
final float timeFactor   = 1.0 / fps;

//Gravitation constant
final float g            = 9.81;

final float elasticity   = 0.9;

//Box parameters
final float boxThickness = 20.0;
final float boxWidth     = 500.0;
final float boxHeight    = 500.0;
final color boxColor     = #B404A9;

//Sphere parameters
final float sphereRadius = 24.0;
final float sphereOffset = -sphereRadius - boxThickness/2.0;
final color sphereColor  = #1D10E0;

//Cylinder parameters
final float cylinderBaseSize   = 50;
final float cylinderHeight     = 50;
final float cylinderOffset     = -cylinderHeight -boxThickness/2.0;
final int   cylinderResolution = 40;
final color cylinderColor      = #42EA70;
PShape openCylinder            = new PShape();
PShape topCylinder             = new PShape();
PShape bottomCylinder          = new PShape();

//Maximum value in degree for the angles along X and Z
final float maxTilt      = 60.0;

//Mouse wheel rotation speed
final float maxSpeed     = 1.5;
final float minSpeed     = 0.2;
final float speedScaling = 0.1;

//Constant to have faster changes for the angles
final float speedFactor  = 3.7;

//Variables
float speed      = 1.0;
float angleY     = 0.0;
float tiltAngleX = 0.0;
float tiltAngleZ = 0.0;
Mover mover = new Mover();
boolean addingCylinderMode = false;

ArrayList<Cylinder> cylinders = new ArrayList<Cylinder>();

void setup() {
  size(1000, 1000, P3D);
  frameRate(fps);
  noStroke();

  float angle;
  float[] x = new float[cylinderResolution +1];
  float[] y = new float[cylinderResolution +1];

  for (int i = 0; i < x.length; ++i) {
    angle = (TWO_PI / cylinderResolution) * i;
    x[i] = sin(angle) * cylinderBaseSize;
    y[i] = cos(angle) * cylinderBaseSize;
  }

  openCylinder = createShape();
  openCylinder.setFill(cylinderColor);
  openCylinder.beginShape(QUAD_STRIP);
  for (int i = 0; i < x.length; ++i) {
    openCylinder.vertex(x[i], 0, y[i]);
    openCylinder.vertex(x[i], cylinderHeight, y[i]);
  }
  openCylinder.endShape();

  topCylinder = createShape();
  topCylinder.setFill(cylinderColor);
  topCylinder.beginShape(TRIANGLES);
  for (int i = 0; i < x.length; ++i) {
    topCylinder.vertex(x[i], 0, y[i]);
    topCylinder.vertex(x[clamp(i+1, 0, x.length-1)], 0, y[clamp(i+1, 0, x.length-1)]);
    topCylinder.vertex(0, 0, 0);
  }
  topCylinder.endShape();

  bottomCylinder = createShape();
  bottomCylinder.setFill(cylinderColor);
  bottomCylinder.beginShape(TRIANGLES);
  for (int i = 0; i < x.length; ++i) {
    bottomCylinder.vertex(x[i], cylinderHeight, y[i]);
    bottomCylinder.vertex(x[clamp(i+1, 0, x.length-1)], cylinderHeight, y[clamp(i+1, 0, x.length-1)]);
    bottomCylinder.vertex(0, cylinderHeight, 0);
  }
  bottomCylinder.endShape();
}

void draw() {
  background(200);
  fill(0);

  //Information in upper left corner
  text("RotationX: "         + tiltAngleX, 5, 10);
  text("RotationY: "         +     angleY, 5, 25);
  text("RotationZ: "         + tiltAngleZ, 5, 40);
  text("Speed: "             + speed, 5, 55);
  text("sphereCoordinates: " + mover.sphere.coordinates, 5, 70);
  text("SphereVelocity: "    + mover.sphere.velocity, 5, 85);

  lights();

  translate(width/2, height/2, 0);        //Set the matrix in the middle of the screen
  placeBoxAndSphere();
  if (addingCylinderMode) {
    cursorCylinder();
  } else {
    cursor();
  }
  placeCylinders();
}

void mouseDragged() {
  if (!addingCylinderMode) {
    pushMatrix();
    rotateY(-angleY);
    if (mouseY > pmouseY) {        //Moved down
      if (tiltAngleX > -maxTilt)
        tiltAngleX -= speedFactor*speed;
    } else if (mouseY < pmouseY) { //Moved up
      if (tiltAngleX < +maxTilt)
        tiltAngleX += speedFactor*speed;
    }
    if (mouseX > pmouseX) {        //Moved right
      if (tiltAngleZ < maxTilt)
        tiltAngleZ += speedFactor*speed;
    } else if (mouseX < pmouseX) { //Moved left
      if (tiltAngleZ > -maxTilt)
        tiltAngleZ -= speedFactor*speed;
    }

    //Check if the Angle are bigger than the maxTilt
    if (tiltAngleX > +maxTilt) tiltAngleX = +maxTilt;
    if (tiltAngleX < -maxTilt) tiltAngleX = -maxTilt;
    if (tiltAngleZ > +maxTilt) tiltAngleZ = +maxTilt;
    if (tiltAngleZ < -maxTilt) tiltAngleZ = -maxTilt;
    popMatrix();
  }
}

//Rotation along Y
void keyPressed () {
  if (key == CODED) {
    if (keyCode == LEFT && !addingCylinderMode)
      angleY -= speedFactor*speed;
    if (keyCode == RIGHT && !addingCylinderMode)
      angleY += speedFactor*speed;
    if (keyCode == SHIFT)
      addingCylinderMode = true;
  }
  
  if(angleY >= 360)
    angleY -= 360;
  if(angleY < 0)
    angleY += 360;
  
}

//Rotation speed increase or decrease with mouseWheel
void mouseWheel(MouseEvent event) {
  if (!addingCylinderMode) {
    if (event.getCount() < 0) {
      if (speed <= maxSpeed)
        speed += speedScaling;
    } else {
      if (speed >= minSpeed)
        speed -= speedScaling;
    }
  }
}

void keyReleased() {
  if (key == CODED) {
    if (keyCode == SHIFT)
      addingCylinderMode = false;
  }
}

void mouseClicked() {
  if (addingCylinderMode) {
    PVector coords = cylinderCheckEdges(mouseX-width/2, mouseY-height/2);
    cylinders.add(new Cylinder(coords.x, coords.y));
  }
}

void placeBoxAndSphere() {
  if (!addingCylinderMode) {
    rotateY((float) toRadians(angleY));     //Rotation along Y
    rotateX((float) toRadians(tiltAngleX)); //Rotation along X
    rotateZ((float) toRadians(tiltAngleZ)); //Rotation along Z
  } else {
    rotateX(-HALF_PI);
  }
  fill(boxColor);
  box(boxWidth, boxThickness, boxHeight); //Creation of the box
  pushMatrix();
  mover.update();
  translate(0, sphereOffset, 0);
  rotateX(HALF_PI);
  mover.display();
  popMatrix();
}

void cursorCylinder() {
  pushMatrix();
  translate(0, cylinderOffset, 0);
  rotateX(HALF_PI);
  PVector coords = cylinderCheckEdges(mouseX-width/2, mouseY-height/2);
  float x = clamp(mouseX-width/2, -boxWidth/2, boxWidth/2);

  float y = clamp(mouseY-height/2, -boxHeight/2, boxHeight/2);

  if (mouseX-width/2 == coords.x && mouseY-height/2 == coords.y)
    noCursor();
  else
    cursor();

  Cylinder cylinder = new Cylinder(coords.x, coords.y);
  cylinder.display();
  popMatrix();
}

void placeCylinders() {
  for (Cylinder c : cylinders) {
    pushMatrix();
    translate(0, cylinderOffset);
    rotateX(HALF_PI);
    c.display();
    popMatrix();
  }
}

private static int clamp(int x, int min, int max) {
  return (int) clamp((float) x, (float) min, (float) max);
}

private static float clamp(float x, float min, float max) {
  if (x > max)
    return max;
  else if (x < min)
    return min;
  else return x;
}


private PVector cylinderCheckEdges(float x, float y) {
  float x2 = x, y2 = y;
  if (x < -boxWidth/2 - sphereRadius) {
    x2 = -boxWidth/2 - sphereRadius +1;
  } else if (x > boxWidth/2 + sphereRadius) {
    x2 = boxWidth/2 + sphereRadius -1;
  }
  if (y < -boxHeight/2 - sphereRadius) {
    y2 = -boxHeight/2 - sphereRadius +1;
  } else if (y > boxHeight/2 + sphereRadius) {
    y2 = boxHeight/2 + sphereRadius -1;
  }
  return new PVector(x2, y2);
}

