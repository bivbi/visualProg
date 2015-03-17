class Cylinder {
  PVector coordinates = new PVector(0,0);
  
  Cylinder(float x, float y) {
    coordinates.x = x;
    coordinates.y = y;
  }
  
  void display() {
    pushMatrix();
    translate(coordinates.x, coordinates.y);
    rotateX(-HALF_PI);
    shape(openCylinder);
    shape(topCylinder);
    shape(bottomCylinder);
    popMatrix();
  }
}
