import static java.lang.Math.toRadians;
import ddf.minim.*;

//Number of Frame per Seconds
final float fps          = 60.0;

//Multiplying factor for the velocity changes
final float timeFactor   = 1.0 / fps;

//Gravitation constant
final float g            = 9.81;

//Elasticity used for bounces, 1 = no energy loss
final float elasticity   = 0.97;

//Box parameters
final float boxThickness = 20.0;
final float boxWidth     = 500.0;
final float boxHeight    = 500.0;
final color boxColor     = #B404A9;

//Sphere parameters
final float sphereRadius = 24.0;
final float sphereOffset = -sphereRadius - boxThickness/2.0;
final int maxSphere      = 10;

//Cylinder parameters
final float cylinderBaseSize   = 50;
final float cylinderHeight     = 50;
final float cylinderOffset     = -cylinderHeight -boxThickness/2.0;
final int   cylinderResolution = 40;
PShape openCylinder            = new PShape();
PShape topCylinder             = new PShape();
PShape bottomCylinder          = new PShape();

//Maximum value in degree for the angles along X and Z
final float maxTilt      = 60.0;

//Mouse wheel rotation speed
final float maxSpeed     = 1.5;
final float minSpeed     = 0.2;
final float speedScaling = 0.1;

//Constant for Y angle with keyboard
final float speedFactor  = 2;

//Variables
float speed      = 1.0;
float angleY     = 0.0;
float tiltAngleX = 0.0;
float tiltAngleZ = 0.0;
Mover mover = new Mover();
boolean addingCylinderMode   = false;
boolean ignoreYRotation      = true;

 //Booleans for the collision sounds
boolean specialAllowed       = true;   //global enable or disable
boolean specialBegin         = false;  //indicates when a sound is playing
boolean specialEdgeCollision = false;  //indicates if there is a collision

Minim minim = new Minim(this);  //this class is processing class to represent audio, this is the path.
AudioPlayer cylinderAudio = minim.loadFile("data/xplode.mp3"); //collision with cylinder
int cylinderSoundLength = cylinderAudio.length();  //length of the first audio file
AudioPlayer edgeAudio = minim.loadFile("data/bounce.mp3");  //collision with edge
int edgeAudioTimer = 0;

ArrayList<Cylinder> cylinders = new ArrayList<Cylinder>();

void setup() {
  size(1000, 1000, P3D);
  frameRate(fps);

  createCylinderShape();
  
  noStroke();
}

void draw() {
  background(200);
  fill(0);

  //Information in upper left corner
  text("RotationX: "         + tiltAngleX, 5, 10);
  if (ignoreYRotation) {
    text("RotationY: " + !ignoreYRotation + " toggle with 'y'", 5, 25);
  } else {
    text("RotationY: "         +     angleY, 5, 25);
  }
  text("RotationZ: "         + tiltAngleZ, 5, 40);
  text("Speed: "             + speed, 5, 55);
  text("Bonus: " + specialAllowed + " toggle with 'x'", 5, 70);
  lights();

  translate(width/2, height/2, 0);        //Set the matrix in the middle of the screen
  placeBoxAndSphere();
  if (addingCylinderMode) {
    cursorCylinder();
  } else {
    cursor();
  }
  placeCylinders();
  
  if (specialAllowed && specialEdgeCollision) {
    edgeSoundPlay();  //edge collision sound
  }
}

//handle the edge bouncing sound
void edgeSoundPlay() {
  ++edgeAudioTimer;
  if (edgeAudioTimer>=25) {
    edgeAudio.pause();
  } else {
    edgeAudio.play();
  }
}

//Move the box along X and Y
void mouseDragged() {
  if (!addingCylinderMode) {
    tiltAngleX -= speed*(mouseY-pmouseY)*2*maxTilt/height;  //Same thing as a map(0,width,-60,60) but no probleme when
    tiltAngleZ += speed*(mouseX-pmouseX)*2*maxTilt/width;   //you move the mouse between 2 drags

    //Check if the Angle are bigger than the maxTilt
    if (tiltAngleX > +maxTilt) tiltAngleX = +maxTilt;
    if (tiltAngleX < -maxTilt) tiltAngleX = -maxTilt;
    if (tiltAngleZ > +maxTilt) tiltAngleZ = +maxTilt;
    if (tiltAngleZ < -maxTilt) tiltAngleZ = -maxTilt;
  }
}

//Rotation along Y, entering addingCylinderMode and toogle rotation along Y
void keyPressed () {
  if (key == CODED) {
    if (keyCode == LEFT && !addingCylinderMode && !ignoreYRotation)
      angleY -= speedFactor*speed;
    if (keyCode == RIGHT && !addingCylinderMode && !ignoreYRotation)
      angleY += speedFactor*speed;
    if (keyCode == SHIFT)
      addingCylinderMode = true;
  } else {
    if (key == 'y') {
      ignoreYRotation = !ignoreYRotation;
    }
    if (key == 'x') {
      specialAllowed = ! specialAllowed;
    }
    if (key == 's') {
      mover.addRandomSphere();
    }
    if (key == 'd') {
      mover.removeSphere();
    }
  }

  if (angleY >= 360) //to display a nice angle between 0° and 360°
    angleY -= 360;
  if (angleY < 0)
    angleY += 360;
}

//Rotation speed increase or decrease with mouseWheel
void mouseWheel(MouseEvent event) {
  if (!addingCylinderMode) {
    if (event.getCount() < 0) {
      if (speed < maxSpeed)
        speed += speedScaling;
    } else {
      if (speed > minSpeed)
        speed -= speedScaling;
    }
    speed = Math.round(10*speed);  //to have one digit float
    speed /= 10;
  }
}

//Leave addingCylinderMode when releasing SHIFT key
void keyReleased() {
  if (key == CODED) {
    if (keyCode == SHIFT)
      addingCylinderMode = false;
  }
}

//Add cylinder ONLY if it will create no collision
void mouseClicked() {
  if (addingCylinderMode) {
    if (mover.spheres.isEmpty()) { //if there is no sphere, add a cylinder
      PVector coords = cylinderCheckEdges(mouseX-width/2, mouseY-height/2);
      cylinders.add(new Cylinder(coords.x, coords.y));
    } else {
      for (Sphere sphere : mover.spheres) { // if balls, check if the cylinder will be on a ball
        if (cylinderCheckBalls(mouseX - width/2, mouseY - height/2)) {
          PVector coords = cylinderCheckEdges(mouseX-width/2, mouseY-height/2);
          cylinders.add(new Cylinder(coords.x, coords.y));
        }
      }
    }
  }
}

//Place the box and sphere according to the different modes
//Also call the update methode to update the sphere coordinates
void placeBoxAndSphere() {
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
void cursorCylinder() {
  pushMatrix();
  translate(0, cylinderOffset, 0); //Offset so the cylinder is not in the box
  rotateX(HALF_PI); //to transphere 2D coordinates of the cylinder into 3D (y->z)
  PVector coords = cylinderCheckEdges(mouseX-width/2, mouseY-height/2);
  float x = clamp(mouseX-width/2, -boxWidth/2, boxWidth/2);

  float y = clamp(mouseY-height/2, -boxHeight/2, boxHeight/2);

  if (mouseX-width/2 == coords.x && mouseY-height/2 == coords.y)
    noCursor(); //hide cursor if mouse in the box
  else
    cursor();  //display cursor if mouse outside the box

  Cylinder cylinder = new Cylinder(coords.x, coords.y);
  cylinder.display(true); //the boolean is the noFill boolean, if true the cylinder is transparent
  popMatrix();
}

//Place the cylinders
void placeCylinders() {
  for (Cylinder c : cylinders) {
    pushMatrix();
    translate(0, cylinderOffset); //Offset so the cylinders are not in the box
    rotateX(HALF_PI);  //to transphere 2D coordinates of the cylinders into 3D (y->z)
    c.display(false);  //display cylinders with a color
    popMatrix();
  }
}

//Simple clamp function, return x if it is in the boundaries, otherwise the closest boundarie
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
private boolean cylinderCheckBalls(float x, float y) {
  boolean check = true;
  for (Sphere s : mover.spheres) {
    if (s.coordinates.dist(new PVector(x, y)) <= sphereRadius + cylinderBaseSize) {
      check = false;
    }
  } 
  return check;
}


//Check if the cylinder at coords x and y isn't outside the box
//If it is, return coordinates where it would be in the box
private PVector cylinderCheckEdges(float x, float y) {
  float x2 = clamp(x, -boxWidth/2 -sphereRadius +1, boxWidth/2 + sphereRadius -1);
  float y2 = clamp(y, -boxHeight/2 -sphereRadius +1, boxHeight/2 + sphereRadius -1);
  return new PVector(x2, y2);
}

//Create the 3 shapes that model a cylinder
void createCylinderShape() {
  float angle;
  float[] x = new float[cylinderResolution +1];
  float[] y = new float[cylinderResolution +1];

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
    topCylinder.vertex(x[clamp(i+1, 0, x.length-1)], 0, y[clamp(i+1, 0, x.length-1)]);
    topCylinder.vertex(0, 0, 0);
  }
  topCylinder.endShape();

  bottomCylinder = createShape();
  bottomCylinder.beginShape(TRIANGLES);
  for (int i = 0; i < x.length; ++i) {
    bottomCylinder.vertex(x[i], cylinderHeight, y[i]);
    bottomCylinder.vertex(x[clamp(i+1, 0, x.length-1)], cylinderHeight, y[clamp(i+1, 0, x.length-1)]);
    bottomCylinder.vertex(0, cylinderHeight, 0);
  }
  bottomCylinder.endShape();
}
