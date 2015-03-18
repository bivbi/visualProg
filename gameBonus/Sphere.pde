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

  void display() {
    pushMatrix();
    translate(coordinates.x, coordinates.y);
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


      coordinates.add(velocity); //Change the coordinates according to the velocity
  }


  void checkCollisions() {
    edgeCollision();
    cylindersCollision();
  }

  private void edgeCollision() {
    if (coordinates.x - sphereRadius < -boxWidth/2) {
      computeCollision(new PVector(-boxWidth/2 + sphereRadius, coordinates.y));
      coordinates.x = -boxWidth/2 + sphereRadius;
      specialEdgeCollision = true;
      velocity.mult(elasticity);
      edgeAudioTimer = 0;
      edgeAudio.rewind();
    } else if (coordinates.x + sphereRadius > boxWidth/2) {
      computeCollision(new PVector(boxWidth/2 - sphereRadius, coordinates.y));
      coordinates.x = boxWidth/2 - sphereRadius;
      specialEdgeCollision = true;
      velocity.mult(elasticity);
      edgeAudioTimer = 0;
      edgeAudio.rewind();
    } 
    if (coordinates.y - sphereRadius < -boxHeight/2) {
      computeCollision(new PVector(coordinates.x, -boxHeight/2 + sphereRadius));
      coordinates.y = -boxHeight/2 + sphereRadius;
      specialEdgeCollision = true;
      velocity.mult(elasticity);
      edgeAudioTimer = 0;
      edgeAudio.rewind();
    } else if (coordinates.y + sphereRadius > boxHeight/2) {
      computeCollision(new PVector(coordinates.x, boxHeight/2 - sphereRadius));
      coordinates.y = boxHeight/2 - sphereRadius;
      specialEdgeCollision = true;
      velocity.mult(elasticity);
      edgeAudioTimer = 0;
      edgeAudio.rewind();
    }
  }

  private void cylindersCollision() {
    Cylinder cylinder = new Cylinder(0,0);
    boolean collisionHappens = false;
    for (Cylinder c : cylinders) {
      if (collisionWithCylinder(c.coordinates) && !collisionHappens) {
        collisionHappens = true;
        cylinder = c;
      }
    }
    if (collisionHappens) {
      computeCollision(cylinder.coordinates);
      if(specialRemoveAllowed) {
        cylinderAudio.rewind();
        cylinderAudio.play();
        specialRemoveBegin = true;
        bonus[0] = new SpecialRemoval(cylinder.coordinates.x, cylinder.coordinates.y, images, cylinderAudio);    
      }
      cylinders.remove(cylinder);
    }
  }

  private void computeCollision(PVector coords) {
    PVector n = coords.get();
    n.sub(coordinates);
    n.normalize();
    float c = n.x*velocity.x + n.y*velocity.y;
    PVector V1NN = n.get();
    V1NN.mult(2);
    V1NN.mult(c);
    PVector v2 = velocity.get();
    v2.sub(V1NN);
    velocity.set(v2);
    velocity.mult(elasticity);
  }

  private boolean collisionWithCylinder(PVector cylinderCoordinates) {
    return coordinates.dist(cylinderCoordinates) <= sphereRadius + cylinderBaseSize;
  }
}

