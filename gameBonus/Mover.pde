class Mover {  
  PVector gravityForce = new PVector(0, 0);
  ArrayList<Sphere> spheres = new ArrayList<Sphere>();

  Mover() {
    addSphere(width/2, height/2);
  }

  void removeSphere() {
    if (!spheres.isEmpty())
      spheres.remove(int(random(0, spheres.size()-1)));
  }

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

  void addSphere(float x, float y) {
    spheres.add(new Sphere(x, y));
  }

  void display() {
    for (Sphere sphere : spheres) {
      sphere.display();
    }
  }

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

