import static java.lang.Math.toRadians;
import ddf.minim.*;

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
final float cylinderOffset     = -boxThickness/2.0;
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
final float speedFactor  = 2;

//Variables
float speed      = 1.0;
float angleY     = 0.0;
float tiltAngleX = 0.0;
float tiltAngleZ = 0.0;
Mover mover = new Mover();
boolean addingCylinderMode   = false;
boolean ignoreYRotation      = true;
boolean specialRemoveAllowed = true;
boolean specialRemoveBegin   = false;
boolean specialEdgeCollision = false;
SpecialRemoval[] bonus = new SpecialRemoval[1];
float specialRemoveX;
float specialRemoveY;
PImage[]images = new PImage[17];
Minim minim = new Minim(this);
AudioPlayer cylinderAudio = minim.loadFile("data/Zelda Main Theme Song.mp3");
int cylinderSoundLength = cylinderAudio.length()/1000;
AudioPlayer edgeAudio = minim.loadFile("data/Zelda Main Theme Song.mp3");
int edgeSoundLength = edgeAudio.length()/1000;
int edgeAudioTimer = 0;

ArrayList<Cylinder> cylinders = new ArrayList<Cylinder>();

void setup() {
  downloadBonus();
  size(1000, 1000, P3D);
  frameRate(fps);

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
  text("sphereCoordinates: " + mover.sphere.coordinates, 5, 70);
  text("SphereVelocity: "    + mover.sphere.velocity, 5, 85);
  text("Bonus: " + specialRemoveAllowed + " toggle with 'x'", 5, 100);
  lights();

  translate(width/2, height/2, 0);        //Set the matrix in the middle of the screen
  placeBoxAndSphere();
  if (addingCylinderMode) {
    cursorCylinder();
  } else {
    cursor();
  }
  placeCylinders();
  if (specialEdgeCollision) {
    edgeSoundPlay();
  }
}

void edgeSoundPlay() {
  ++edgeAudioTimer;
  if(edgeAudioTimer>=edgeSoundLength*fps) {
    edgeAudio.pause();
  } else {
    edgeAudio.play();
  }
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
      specialRemoveAllowed = ! specialRemoveAllowed;
    }
  }

  if (angleY >= 360)
    angleY -= 360;
  if (angleY < 0)
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
    if (!ignoreYRotation) {
      rotateY((float) toRadians(angleY));     //Rotation along Y
    }
    rotateX((float) toRadians(tiltAngleX)); //Rotation along X
    rotateZ((float) toRadians(tiltAngleZ)); //Rotation along Z
  } else {
    rotateX(-HALF_PI);
  }
  fill(boxColor);
  box(boxWidth, boxThickness, boxHeight); //Creation of the box
  pushMatrix();
  mover.update();
  pushMatrix();
  translate(0, sphereOffset, 0);
  rotateX(HALF_PI);
  mover.display();
  popMatrix();
  pushMatrix();
  translate(0, -boxThickness/2 -1, 0);
  rotateX(HALF_PI);
  if (specialRemoveAllowed) {
    if (specialRemoveBegin) {
      
      bonus[0].display();
    }
  }
  popMatrix();
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
  cylinder.display(true);
  popMatrix();
}

void placeCylinders() {
  for (Cylinder c : cylinders) {
    pushMatrix();
    translate(0, cylinderOffset);
    rotateX(HALF_PI);
    c.display(false);
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

void downloadBonus() {
  images[0]  = loadImage("data/bonus00.png");//"https://www.dropbox.com/s/k21kcoy0l74wx7a/bonus00.png?raw=1");
  images[1]  = loadImage("data/bonus01.png");//"https://www.dropbox.com/s/mzrmjqo1c6fzo29/bonus01.png?raw=1");
  images[2]  = loadImage("data/bonus02.png");//"https://www.dropbox.com/s/fdxu5lplukg58nt/bonus02.png?raw=1");
  images[3]  = loadImage("data/bonus03.png");//"https://www.dropbox.com/s/8hruq4sy54mqmil/bonus03.png?raw=1");
  images[4]  = loadImage("data/bonus04.png");//"https://www.dropbox.com/s/n1070rlnhynmjpk/bonus04.png?raw=1");
  images[5]  = loadImage("data/bonus05.png");//"https://www.dropbox.com/s/fvxp6lejn71v9hi/bonus05.png?raw=1");
  images[6]  = loadImage("data/bonus06.png");//"https://www.dropbox.com/s/pz1jehhinthm0ad/bonus06.png?raw=1");
  images[7]  = loadImage("data/bonus07.png");//"https://www.dropbox.com/s/rfj803o39uzofkw/bonus07.png?raw=1");
  images[8]  = loadImage("data/bonus08.png");//"https://www.dropbox.com/s/xyerp3ntff6fv0h/bonus08.png?raw=1");
  images[9]  = loadImage("data/bonus09.png");//"https://www.dropbox.com/s/seubw9yoz46bizv/bonus09.png?raw=1");
  images[10] = loadImage("data/bonus10.png");//"https://www.dropbox.com/s/875dv98ed6brgi8/bonus10.png?raw=1");
  images[11] = loadImage("data/bonus11.png");//"https://www.dropbox.com/s/wa7s8313o1c7y0s/bonus11.png?raw=1");
  images[12] = loadImage("data/bonus12.png");//"https://www.dropbox.com/s/8l4vq83bv1eygjj/bonus12.png?raw=1");
  images[13] = loadImage("data/bonus13.png");//"https://www.dropbox.com/s/4qr7brlj800nw9h/bonus13.png?raw=1");
  images[14] = loadImage("data/bonus14.png");//"https://www.dropbox.com/s/87qlmejxixjsdvp/bonus14.png?raw=1");
  images[15] = loadImage("data/bonus15.png");//"https://www.dropbox.com/s/fiqrbm5dhtxa9w0/bonus15.png?raw=1");
  images[16] = loadImage("data/bonus16.png");//"https://www.dropbox.com/s/nhlk94zr6xxuvl1/bonus16.png?raw=1");
}

