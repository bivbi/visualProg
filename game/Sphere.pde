class Sphere {

  PVector coordinates;
  PVector velocity;

  //create a ball in the top left corner of the matrix,
  //it's always used in the box referencial so in the middle of the screen
  Sphere() {
    coordinates = new PVector(0, 0);
    velocity    = new PVector(0, 0);
  }

  //display the ball at the right coords and in the right color
  void display() {
    pushMatrix();
    translate(coordinates.x, coordinates.y);
    fill(sphereColor);
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
    cylindersCollision();
  }

  //Compute a bounce on an edge, loss of velocity with the elasticity parameter
  private void edgeCollision() {
    if (coordinates.x - sphereRadius/2 < -boxWidth/2) {  //LEFT
      computeCollision(new PVector(-boxWidth/2 + sphereRadius/2, coordinates.y)); //compute new velocity and coordinates
      coordinates.x = -boxWidth/2 + sphereRadius/2;  //avoid that the sphere leave the box
      velocity.mult(elasticity);  //reduce speed with velocity factor
      score -= velocity.mag();
      lastScore = velocity.mag();
    } else if (coordinates.x + sphereRadius/2 > boxWidth/2) { //RIGHT
      computeCollision(new PVector(boxWidth/2 - sphereRadius/2, coordinates.y));
      coordinates.x = boxWidth/2 - sphereRadius/2;
      velocity.mult(elasticity);
      score -= velocity.mag();
      lastScore = velocity.mag();
    } 
    if (coordinates.y - sphereRadius/2 < -boxHeight/2) { //TOP
      computeCollision(new PVector(coordinates.x, -boxHeight/2 + sphereRadius/2));
      coordinates.y = -boxHeight/2 + sphereRadius/2;
      velocity.mult(elasticity);
      score -= velocity.mag();
      lastScore = velocity.mag();
    } else if (coordinates.y + sphereRadius/2 > boxHeight/2) { //BOTTOM
      computeCollision(new PVector(coordinates.x, boxHeight/2 - sphereRadius/2));
      coordinates.y = boxHeight/2 - sphereRadius/2;
      velocity.mult(elasticity);
      score -= velocity.mag();
      lastScore = velocity.mag();
    }
  }

  //Compute the bounce on a cylinder
  private void cylindersCollision() {
    Cylinder cylinder = new Cylinder(0, 0);
    boolean collisionHappens = false;
    for (Cylinder c : cylinders) { //find if there is a contact with a cylinder and this cylinder
      if (collisionWithCylinder(c.coordinates) && !collisionHappens) {
        collisionHappens = true;
        cylinder = c;
      }
    }
    if (collisionHappens) {
      computeCollision(cylinder.coordinates); //compute the changes
      score += 3*velocity.mag();
      lastScore = 3*velocity.mag();
    }
  }


  //Compute the velocity and coords changes of the sphere
  private void computeCollision(PVector coords) {
    PVector n = coords.get();
    n.sub(coordinates);
    n.normalize();  //get normal vector
    float c = n.x*velocity.x + n.y*velocity.y; //this is the dot product
    PVector V1NN = n.get();  //this is 2 * (v1 dot n) * n
    V1NN.mult(2);
    V1NN.mult(c);
    PVector v2 = velocity.get();
    v2.sub(V1NN);  //this is v1 - 2 * (v1 dot n) * n
    velocity.set(v2);
    velocity.mult(elasticity);
    coordinates.sub(n);  //prevents the cylinder to get in the cylinder it collided with
  }


  //check if there is a collision between the ball and a given cylinder
  //true if the dist between the 2 is less than the sum of the radius
  private boolean collisionWithCylinder(PVector cylinderCoordinates) {
    return coordinates.dist(cylinderCoordinates) <= sphereRadius + cylinderBaseSize;
  }
}

