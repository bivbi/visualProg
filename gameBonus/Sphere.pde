class Sphere {

  PVector coordinates;
  PVector velocity;
  color c = color(random(0,255), random(0,255), random(0,255));

  //create a ball in the top left corner of the matrix,
  //it's always used in the box referencial so in the middle of the screen
  Sphere() {
    coordinates = new PVector(0, 0);
    velocity    = new PVector(0, 0);
  }

  //Create a ball at specified coordinates
  Sphere(float x, float y) {
    coordinates = new PVector(x, y);
    velocity = new PVector(0, 0);
  }

  //display the ball at the right coords and in the right color
  void display() {
    pushMatrix();
    translate(coordinates.x, coordinates.y);
    fill(c);
    sphere(sphereRadius);
    popMatrix();
  }
  
  //Compute the impact of the gravity and friction on the ball
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

  //Test if there is a collision with an edge or a cylinder
  void checkCollisions() {
    edgeCollision();
    sphereCollision();
    cylindersCollision();
  }

  //Compute a bounce on an edge, loss of velocity with the elasticity parameter
  private void edgeCollision() {
    if (coordinates.x - sphereRadius/2 < -boxWidth/2) {  //LEFT
      computeCollision(new PVector(-boxWidth/2 + sphereRadius/2, coordinates.y)); //compute new velocity and coordinates
      coordinates.x = -boxWidth/2 + sphereRadius/2; //avoid that the sphere leave the box
      specialEdgeCollision = true; //set this boolean to true to allow the bounce music to start
      velocity.mult(elasticity); //reduce speed with velocity factor
      edgeAudioTimer = 0;  //reset timer
      edgeAudio.rewind();  //and rewind the tape (special function)
    } else if (coordinates.x + sphereRadius/2 > boxWidth/2) { //Right
      computeCollision(new PVector(boxWidth/2 - sphereRadius/2, coordinates.y));
      coordinates.x = boxWidth/2 - sphereRadius/2;
      specialEdgeCollision = true;
      velocity.mult(elasticity);
      edgeAudioTimer = 0;
      edgeAudio.rewind();
    } 
    if (coordinates.y - sphereRadius/2 < -boxHeight/2) { //TOP
      computeCollision(new PVector(coordinates.x, -boxHeight/2 + sphereRadius/2));
      coordinates.y = -boxHeight/2 + sphereRadius/2;
      specialEdgeCollision = true;
      velocity.mult(elasticity);
      edgeAudioTimer = 0;
      edgeAudio.rewind();
    } else if (coordinates.y + sphereRadius/2 > boxHeight/2) { //BOTTOM
      computeCollision(new PVector(coordinates.x, boxHeight/2 - sphereRadius/2));
      coordinates.y = boxHeight/2 - sphereRadius/2;
      specialEdgeCollision = true;
      velocity.mult(elasticity);
      edgeAudioTimer = 0;
      edgeAudio.rewind();
    }
  }

  //Compute the bounce on a cylinder
  private boolean cylindersCollision() {
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
      if(specialAllowed) {  //start the cylinderCollision sound
        cylinderAudio.rewind();
        cylinderAudio.play();
        specialBegin = true;
      }
    }
    return collisionHappens;
  }
  
  //Compute the bounce on other sphere
  private boolean sphereCollision() {
    Sphere that = new Sphere();
    boolean collisionHappens = false;
    for(Sphere s : mover.spheres) {
      if(collisionWithSphere(s.coordinates) && !collisionHappens && !(s == this)) {
        collisionHappens = true;
        that = s;
      }
      if(collisionHappens) {
        computeCollision(that.coordinates);
      }
    }
    return collisionHappens;
  }

  //Compute the velocity and coords changes of the sphere
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
    coordinates.sub(n);
  }

  //check if there is a collision between the ball and a given cylinder
  //true if the dist between the 2 is less than the sum of the radius
  private boolean collisionWithCylinder(PVector cylinderCoordinates) {
    return coordinates.dist(cylinderCoordinates) <= sphereRadius + cylinderBaseSize;
  }
  
  //same as above but for a given sphere
  private boolean collisionWithSphere(PVector sphereCoordinates) {
    return coordinates.dist(sphereCoordinates) <= 2*sphereRadius;
  }
}

