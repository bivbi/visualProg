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

//Sphere parameters
final float sphereRadius = 48.0;
final float sphereOffset = -sphereRadius - boxThickness/2.0;

//Maximum value in degree for the angles along X and Z
final float maxTilt      = 60.0;

//Mouse wheel rotation speed
final float maxSpeed     = 1.5;
final float minSpeed     = 0.2;
final float speedScaling = 0.1;

//Constant to have faster changes for the angles
final float speedFactor  = 3.7;

void setup() {
  size(1000, 1000, P3D);
  frameRate(fps);
  noStroke();
}

float speed      = 1.0;
float angleY     = 0.0;
float tiltAngleX = 0.0;
float tiltAngleZ = 0.0;
PVector sphereCoordinates = new PVector(0, 0, 0);
PVector sphereVelocity    = new PVector(0, 0, 0);
PVector gravityForce      = new PVector(0, 0, 0);

void draw() {
  background(200);
  fill(0);
  
  //Information in upper left corner
  text("RotationX: "         + tiltAngleX, 5, 10);
  text("RotationZ: "         + tiltAngleZ, 5, 25);
  text("Speed: "             + speed, 5, 40);
  text("sphereCoordinates: " + sphereCoordinates, 5, 55);
  text("SphereVelocity: "    + sphereVelocity, 5, 70);
  
  fill(#B404A9); //Box color (purple)
  lights();
  
  translate(width/2, height/2, 0);        //Set the box in the middle of the screen
  rotateY((float) toRadians(angleY));     //Rotation along Y
  rotateX((float) toRadians(tiltAngleX)); //Rotation along X
  rotateZ((float) toRadians(tiltAngleZ)); //Rotation along Z
  box(boxWidth, boxThickness, boxHeight); //Creation of the box
  updateSphereCoordinates();              //Compute the new sphere coordinates
  translate(sphereCoordinates.x, sphereCoordinates.y + sphereOffset, sphereCoordinates.z); //Set the origin for the sphere in the box referencial
  fill(#1D10E0);                          //Sphere color (Blue)
  sphere(sphereRadius);                   //Creation of the sphere
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
    if(sphereVelocity.x > 0) {
      sphereVelocity.x *= -elasticity;
    }
    sphereCoordinates.x = boxWidth/2;
  } else if (sphereCoordinates.x < -boxWidth/2) {   //Touch left
    if(sphereVelocity.x < 0) {
      sphereVelocity.x *= -elasticity;
    }
    sphereCoordinates.x = -boxWidth/2;
  }
  if (sphereCoordinates.z > boxHeight/2) {          //Touch down
    if(sphereVelocity.z > 0) {
      sphereVelocity.z *= -elasticity;
    }    
    sphereCoordinates.z = boxHeight/2;
  } else if (sphereCoordinates.z < -boxHeight/2) {  //Touch up
    if(sphereVelocity.z < 0) {
      sphereVelocity.z *= -elasticity;
    }
    sphereCoordinates.z = -boxWidth/2;
  }
}

void mouseDragged() {
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

//Rotation along Y
void keyPressed () {
  if (key == CODED) {
    if (keyCode == LEFT)
      angleY += speedFactor*speed;
    if (keyCode == RIGHT)
      angleY -= speedFactor*speed;
  }
}

//Rotation speed increase or decrease with mouseWheel
void mouseWheel(MouseEvent event) {
  if (event.getCount() < 0) {
    if (speed <= maxSpeed)
      speed += speedScaling;
  } else {
    if (speed >= minSpeed)
      speed -= speedScaling;
  }
}

