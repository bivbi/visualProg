class Cylinder {
  PVector coordinates = new PVector(0,0);
  color c = color(random(0,255), random(0,255), random(0,255));
  
  Cylinder(float x, float y) {
    coordinates.x = x;
    coordinates.y = y;
  }
  
  void display(boolean noFill) {
    pushMatrix();
    translate(coordinates.x, coordinates.y);
    rotateX(HALF_PI);
    if(noFill) {
      openCylinder.setFill(0);
      topCylinder.setFill(0);
      bottomCylinder.setFill(0);
    } else {
      openCylinder.setFill(c);
      topCylinder.setFill(c);
      bottomCylinder.setFill(c);
    }
    shape(openCylinder);
    shape(topCylinder);
    shape(bottomCylinder);
    popMatrix();
  }
}