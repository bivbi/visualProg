class Sphere {

  PVector coordinates;
  PVector velocity;

  Sphere() {
    coordinates = new PVector(0, 0);
    velocity    = new PVector(0, 0);
  }

  Sphere(float x, float y) {
    coordinates = new PVector(x, y);
    velocity = new PVector(0, 0);
  }

  Sphere(float x, float y, float vx, float vy) {
    this(x, y);
    velocity.x = vx;
    velocity.y = vy;
  }

  void display(boolean addingCylinderMode) {
    pushMatrix();
    if (!addingCylinderMode) {
      translate(coordinates.x, coordinates.y);
    }
    fill(sphereColor);
    sphere(sphereRadius);
    popMatrix();
  }

  void updateCoordinates() {
    PVector gravityForce = new PVector(0, 0);
    gravityForce.x       = +sin((float)toRadians(tiltAngleZ)) * g * timeFactor; //Gravity force on X
    gravityForce.y       = -sin((float)toRadians(tiltAngleX)) * g * timeFactor; //Gravity force on Z

    float normalForce = 1.0;
    float mu= 0.01;
    float frictionMagnitude = normalForce * mu;
    PVector friction = velocity.get();
    friction.normalize();
    friction.mult(frictionMagnitude * timeFactor); //Friction factor

    velocity.add(gravityForce); //Change velocity according to gravity
    velocity.add(friction);     //Change velocity according to friction


    coordinates.add(sphere.velocity); //Change the coordinates according to the velocity
  }
}

