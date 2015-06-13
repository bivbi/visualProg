class Mover {  //One class for all things that move
  PVector gravityForce = new PVector(0, 0);
  ArrayList<Sphere> spheres = new ArrayList<Sphere>();

  //Create a mover with a ball in the middle of the screen
  Mover() {
    addSphere(width/2, height/2);
  }

  //Remove a ball at random
  void removeSphere() {
    if (!spheres.isEmpty())
      spheres.remove(int(random(0, spheres.size()-1)));
  }

  //add a ball at random coordinates without creating collision on other balls or cylinders
  void addRandomSphere() {
    if (spheres.size() < maxSphere) {
      float x;
      float y;
      Sphere s;
      do {
        x = random(-boxWidth/2, boxWidth/2);
        y = random(-boxHeight/2, boxHeight/2);
        s = new Sphere(x, y);
      } while (s.sphereCollision() || s.cylindersCollision());
      spheres.add(new Sphere(x,y));
    }
  }

  //Add a ball at specified x and y
  void addSphere(float x, float y) {
    spheres.add(new Sphere(x, y));
  }

  //dispaly all balls
  void display() {
    for (Sphere sphere : spheres) {
      sphere.display();
    }
  }

  //update all balls
  void update() {
    if (!addingCylinderMode) {
      for (Sphere sphere : spheres) {
        sphere.updateCoordinates();
      }
    }
    for (Sphere sphere : spheres) {
      sphere.checkCollisions();
    }
  }
}

