import static java.lang.Math.toRadians;

void setup() {
  size(1000, 1000, P3D);
  noStroke();
}
float angleY = 0;
float tiltAngleX = 0;
float tiltAngleZ = 0;
float maxTilt = 60;
float speed = 1;

void draw() {
  background(200);
  fill(0);
    text("RotationX: " + tiltAngleX, 0, 10);
    text("RotationZ: " + tiltAngleZ, 0, 25);
    text("Speed: " + speed, 0, 40);
  fill(#B404A9);
  lights();
  translate(width/2, height/2, 0);
  rotateY((float) toRadians(angleY));
  rotateX((float) toRadians(tiltAngleX));
  rotateZ((float) toRadians(tiltAngleZ));
  box(500,20,500);

}

void mouseDragged() {
  if (pmouseY < mouseY) {
    if(tiltAngleX > -maxTilt)
      tiltAngleX -= 5*speed;
  } else if (pmouseY > mouseY) {
    if(tiltAngleX < +maxTilt)
      tiltAngleX += 5*speed;
  }
  
  if (pmouseX < mouseX) {
    if(tiltAngleZ > -maxTilt)
      tiltAngleZ += 5*speed;
  } else if (pmouseX > mouseX) {
    if(tiltAngleZ < +maxTilt)
      tiltAngleZ -= 5*speed;
  }  
}

void mouseWheel(MouseEvent event) {
 if(event.getCount() < 0) {
   if(speed <= 1.5)
     speed += 0.1;
 } else {
   if(speed >= 0.2)
     speed -= 0.1;
 }
}


