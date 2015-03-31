class Cylinder {
  PVector coordinates = new PVector(0,0);
  color c = color(0,150,0);//color(random(0,255), random(0,255), random(0,255));
  Cylinder(float x, float y) {
    coordinates.x = x;
    coordinates.y = y;
  }
  
  //display a cylinder with or without a color
  //at the right coordinates
  void display(boolean noFill) {
    pushMatrix();
    translate(coordinates.x, coordinates.y);
    rotateX(HALF_PI); //makes the cylinder on the board in the right angle
    /*if(noFill) {
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
  */
    shape(tree);
    popMatrix();
  }
}
