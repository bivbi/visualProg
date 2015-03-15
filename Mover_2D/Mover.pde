class Mover {
  //Gravity constant
  final float g          = 9.81;
  
  //Multiplying factor for the velocity changes
  final float timeFactor = 1.0/60.0;
  
  //Amount of energy lost in collisions
  final float elasticity = 0.9;
  
  PVector location;
  PVector velocity;
  PVector gravity;
  
  //Constructor
  Mover() {
    location = new PVector(width/2, height/4);
    velocity = new PVector(3,3);
    gravity = new PVector(0,g);
    gravity.mult(timeFactor);
  }
  
  //Apply velocity on the location
  void update() {
    location.add(velocity);
  }
  
  //Display a grey ball
  void display() {
    stroke(0);
    strokeWeight(2);
    fill(127);
    ellipse(location.x, location.y, 48, 48);
  }
  
  //Check if the ball touches an edge
  void checkEdges() {
    if (location.x > width) {     //Touch right
      velocity.set(elasticity * velocity.x*(-1),velocity.y);
    } else if (location.x < 0) {  //Touch left
      velocity.set(elasticity * velocity.x*(-1),velocity.y);
    }
    if (location.y > height) {    //Touch down
      velocity.set(velocity.x,elasticity * velocity.y*(-1));
    } else if (location.y < 0) {  //Touch up
      velocity.set(velocity.x,elasticity * velocity.y*(-1));
    }
    
    velocity.add(gravity); //Change velocity according to gravity force
    
    if (location.y >= height) { // Check if the ball is under the floor
      location.set(location.x, height);
    }
    
  } 
}
