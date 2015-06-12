class Mover {  //One class for all things that move
  PVector gravityForce = new PVector(0, 0);
  Sphere sphere = new Sphere();

  //display the sphere
  void display() {
    sphere.display();
  }

  //update the sphere
  void update() {
    if(!addingCylinderMode) {
      sphere.updateCoordinates();
    }
    sphere.checkCollisions();
  }
}

