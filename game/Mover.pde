class Mover {  
  PVector gravityForce = new PVector(0, 0);
  Sphere sphere = new Sphere();


  void display() {
    sphere.display();
  }

  void update() {
    if(!addingCylinderMode) {
      sphere.updateCoordinates();
    }
    sphere.checkCollisions();
  }
}

