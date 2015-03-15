import static java.lang.Math.toRadians;

void setup() {
  size(1000, 1000, P3D);
  frameRate(60);
  noStroke();
}
final float timeFactor = 1.0 / 60.0;
final float elasticity = 0.9;
final float g = 9.81;
final float boxThickness = 20;
final float boxWidth = 500;
final float boxHeight = 500;
final float sphereRadius = 48;
final float offset = -sphereRadius - boxThickness/2.0;

float angleY = 0;
float tiltAngleX = 0;
float tiltAngleZ = 0;
float maxTilt = 60;
float speed = 1;
PVector sphereCoordinates = new PVector(0, 0, 0);
PVector sphereVelocity = new PVector(0, 0, 0);
PVector gravityForce = new PVector(0, 0, 0);

void draw() {
  background(200);
  fill(0);
  text("RotationX: " + tiltAngleX, 5, 10);
  text("RotationZ: " + tiltAngleZ, 5, 25);
  text("Speed: " + speed, 5, 40);
  text("sphereCoordinates: " + sphereCoordinates, 5, 55);
  text("SphereVelocity: " + sphereVelocity, 5, 70);
  fill(#B404A9);
  lights();
  translate(width/2, height/2, 0);
  rotateY((float) toRadians(angleY));
  rotateX((float) toRadians(tiltAngleX));
  rotateZ((float) toRadians(tiltAngleZ));
  box(boxWidth, boxThickness, boxHeight);
  updateSphereCoordinates();
  translate(sphereCoordinates.x, sphereCoordinates.y + offset, sphereCoordinates.z);
  fill(#1D10E0);
  sphere(sphereRadius);
}

void updateSphereCoordinates() {
  gravityForce.x = sin((float)toRadians(tiltAngleZ)) * g * timeFactor;
  gravityForce.z = -sin((float)toRadians(tiltAngleX)) * g * timeFactor;
  
  float normalForce = 1;
  float mu= 0.01;
  float frictionMagnitude = normalForce * mu;
  PVector friction = sphereVelocity.get();
  friction.normalize();
  friction.mult(frictionMagnitude * timeFactor);
  
  sphereVelocity.add(gravityForce);
  sphereVelocity.add(friction);
  
  
  sphereCoordinates.add(sphereVelocity);
  sphereCheckEdges();
}

void sphereCheckEdges() {
  if (sphereCoordinates.x > boxWidth/2) { // touch right
    if(sphereVelocity.x > 0) {
      sphereVelocity.x *= -0.5;
    }
    sphereCoordinates.x = boxWidth/2;
  } else if (sphereCoordinates.x < -boxWidth/2) { // touch left
    if(sphereVelocity.x < 0) {
      sphereVelocity.x *= -0.5;
    }
    sphereCoordinates.x = -boxWidth/2;
  }
  if (sphereCoordinates.z > boxHeight/2) { // touch down
    if(sphereVelocity.z > 0) {
      sphereVelocity.z *= -0.5;
    }    
    sphereCoordinates.z = boxHeight/2;
  } else if (sphereCoordinates.z < -boxHeight/2) { // touch up
    if(sphereVelocity.z < 0) {
      sphereVelocity.z *= -0.5;
    }
    sphereCoordinates.z = -boxWidth/2;
  }
}

void mouseDragged() {
  if (pmouseY < mouseY) {
    if (tiltAngleX > -maxTilt)
      tiltAngleX -= 3.7*speed;
  } else if (pmouseY > mouseY) {
    if (tiltAngleX < +maxTilt)
      tiltAngleX += 3.7*speed;
  }
  if (pmouseX < mouseX) {
    if (tiltAngleZ < maxTilt)
      tiltAngleZ += 3.7*speed;
  } else if (pmouseX > mouseX) {
    if (tiltAngleZ > -maxTilt)
      tiltAngleZ -= 3.7*speed;
  }
  if (tiltAngleX > +maxTilt) tiltAngleX = +maxTilt;
  if (tiltAngleX < -maxTilt) tiltAngleX = -maxTilt;
  if (tiltAngleZ > +maxTilt) tiltAngleZ = +maxTilt;
  if (tiltAngleZ < -maxTilt) tiltAngleZ = -maxTilt;
}

void keyPressed () {
  if (key == CODED) {
    if (keyCode == LEFT)
      angleY += 3.7*speed;
    if (keyCode == RIGHT)
      angleY -= 3.7*speed;
  }
}

void mouseWheel(MouseEvent event) {
  if (event.getCount() < 0) {
    if (speed <= 1.5)
      speed += 0.1;
  } else {
    if (speed >= 0.2)
      speed -= 0.1;
  }
}

