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

  void updateCoordinates(float minWidth, float maxWidth, float minHeight, float maxHeight) {
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
    checkEdges(minWidth, maxWidth, minHeight, maxHeight);                    //Check if the sphere is touching the edges of the box
  }


  void checkEdges(float minWidth, float maxWidth, float minHeight, float maxHeight) {
    if (coordinates.x >= maxWidth) {           //Touch right
        velocity.x *= -elasticity;
      coordinates.x = maxWidth;
    } else if (coordinates.x <= minWidth) {   //Touch left
        velocity.x *= -elasticity;
      coordinates.x = minWidth;
    }
    if (coordinates.y >= maxHeight) {          //Touch down
        velocity.y *= -elasticity;
      coordinates.y = maxHeight;
    } else if (coordinates.y <= minHeight) {  //Touch up
        velocity.y *= -elasticity;
      coordinates.y = minHeight;
    }
  }
}

