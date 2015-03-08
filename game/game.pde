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
  text(speed,0,10);
  fill(#B404A9);
  lights();
  translate(width/2, height/2, 0);
  rotateY((float) toRadians(angleY));
  rotateX((float) toRadians(tiltAngleX));
  rotateZ((float) toRadians(tiltAngleZ));
  box(100,30,100);

}

void keyPressed() {
  if(key == CODED) {
    if(keyCode == LEFT) {
        angleY -= 2*speed;
    } else if (keyCode == RIGHT) {
        angleY += 2*speed;
    }
  }
}

void mouseDragged() {
  if (pmouseY < mouseY) {
    if(tiltAngleX >= - maxTilt)
      tiltAngleX -= speed;
  } else if (pmouseY > mouseY) {
    if(tiltAngleX <=  maxTilt)
      tiltAngleX += speed;
  }
  
  if (pmouseX < mouseX) {
    if(tiltAngleZ >= - maxTilt)
      tiltAngleZ -= speed;
  } else if (pmouseX > mouseX) {
    if(tiltAngleZ <=  maxTilt)
      tiltAngleZ += speed;
  }  
}

void mouseWheel(MouseEvent event) {
 if(event.getCount() < 0) {
   if(speed <= 10)
     speed+=0.1;
 } else {
   if(speed >=0)
     speed-=0.1;
 }
}


