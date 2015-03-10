class Mover {
  PVector location;
  PVector velocity;
  PVector gravity = new PVector(0,3);
  
  Mover() {
    location = new PVector(width/2, height/2);
    velocity = new PVector(0,3);
  }
  
  void update() {
    location.add(velocity);
  }
  
  void display() {
    stroke(0);
    strokeWeight(2);
    fill(127);
    ellipse(location.x, location.y, 48, 48);
  }
  
  void checkEdges() {
    if (location.x > width) {
      velocity.set(velocity.x*(-1),velocity.y);
    } else if (location.x < 0) {
      velocity.set(velocity.x*(-1),velocity.y);
    }

    if (location.y > height) {
      velocity.set(velocity.x,velocity.y*(-1));
    } else if (location.y < 0) {
      velocity.set(velocity.x,velocity.y*(-1));
    }
    
    velocity.add(gravity);
    
    if(location.y >= height) location.set(location.x, height);
    
  } 
}
