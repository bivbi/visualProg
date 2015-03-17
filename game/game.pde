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
PVector sphereCoordinates  = new PVector(0, 0, 0);
PVector sphereVelocity     = new PVector(0, 0, 0);
PVector gravityForce       = new PVector(0, 0, 0);
boolean addingCylinderMode = false;

ArrayList<PVector> cylinders = new ArrayList<PVector>();
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
  text("RotationZ: "         + tiltAngleZ, 5, 25);
  text("Speed: "             + speed, 5, 40);
  text("sphereCoordinates: " + sphereCoordinates, 5, 55);
  text("SphereVelocity: "    + sphereVelocity, 5, 70);

  lights();

  translate(width/2, height/2, 0);        //Set the matrix in the middle of the screen
  placeBoxAndSphere();
  if (addingCylinderMode)
    cursorCylinder();
  else
    cursor();
  placeCylinders();
}

void updateSphereCoordinates() {
  gravityForce.x = +sin((float)toRadians(tiltAngleZ)) * g * timeFactor; //Gravity force on X
  gravityForce.z = -sin((float)toRadians(tiltAngleX)) * g * timeFactor; //Gravity force on Z

  float normalForce = 1.0;
  float mu= 0.01;
  float frictionMagnitude = normalForce * mu;
  PVector friction = sphereVelocity.get();
  friction.normalize();
  friction.mult(frictionMagnitude * timeFactor); //Friction factor

    sphereVelocity.add(gravityForce); //Change velocity according to gravity
  sphereVelocity.add(friction);     //Change velocity according to friction


    sphereCoordinates.add(sphereVelocity); //Change the coordinates according to the velocity
  sphereCheckEdges();                    //Check if the sphere is touching the edges of the box
}

void sphereCheckEdges() {
  if (sphereCoordinates.x > boxWidth/2) {           //Touch right
    if (sphereVelocity.x > 0) {
      sphereVelocity.x *= -elasticity;
    }
    sphereCoordinates.x = boxWidth/2;
  } else if (sphereCoordinates.x < -boxWidth/2) {   //Touch left
    if (sphereVelocity.x < 0) {
      sphereVelocity.x *= -elasticity;
    }
    sphereCoordinates.x = -boxWidth/2;
  }
  if (sphereCoordinates.z > boxHeight/2) {          //Touch down
    if (sphereVelocity.z > 0) {
      sphereVelocity.z *= -elasticity;
    }    
    sphereCoordinates.z = boxHeight/2;
  } else if (sphereCoordinates.z < -boxHeight/2) {  //Touch up
    if (sphereVelocity.z < 0) {
      sphereVelocity.z *= -elasticity;
    }
    sphereCoordinates.z = -boxWidth/2;
  }
}

void mouseDragged() {
  if (!addingCylinderMode) {
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
  }
}

//Rotation along Y
void keyPressed () {
  if (key == CODED) {
    if (keyCode == LEFT && !addingCylinderMode)
      angleY += speedFactor*speed;
    if (keyCode == RIGHT && !addingCylinderMode)
      angleY -= speedFactor*speed;
    if (keyCode == SHIFT)
      addingCylinderMode = true;
  }
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
  if (addingCylinderMode && cylinderCheckEdges()) {
    cylinders.add(new PVector(mouseX-width/2, cylinderOffset, mouseY-height/2));
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
  if (!addingCylinderMode)
    updateSphereCoordinates();              //Compute the new sphere coordinates
  pushMatrix();
  translate(sphereCoordinates.x, sphereCoordinates.y + sphereOffset, sphereCoordinates.z); //Set the origin for the sphere in the box referencial
  fill(sphereColor);
  sphere(sphereRadius);                   //Creation of the sphere
  popMatrix();
}

void cylinder() {
  shape(openCylinder);
  shape(topCylinder);
  shape(bottomCylinder);
}

void cursorCylinder() {
  noCursor();
  float x = mouseX-width/2;
  float y = mouseY-height/2;
  pushMatrix();
  translate(x, cylinderOffset, y);
  cylinder();
  popMatrix();
}

void placeCylinders() {
  for (PVector v : cylinders) {
    pushMatrix();
    translate(v.x, v.y, v.z);
    cylinder();
    popMatrix();
  }
}

private static int clamp(int x, int min, int max) {
  if (x> max)
    return max;
  else if (x < min)
    return min;
  else return x;
}

private boolean cylinderCheckEdges() {
 float widthOffset = (width - boxWidth) / 2.0;
 float heightOffset = (height - boxHeight) / 2.0;
 boolean xEdges = (widthOffset + cylinderBaseSize) <= mouseX && mouseX <= (width - widthOffset - cylinderBaseSize);
 boolean yEdges = (heightOffset + cylinderBaseSize) <= mouseY && mouseY <= (height - heightOffset- cylinderBaseSize);
 return xEdges && yEdges;
}
