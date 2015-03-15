class Mover {
  PVector location;
  PVector velocity;
  PVector gravity;
  final float g = 9.81;
  final float timeFactor = 1.0/60.0;
  final float elasticity = 0.9;
  
  Mover() {
    location = new PVector(width/2, height/4);
    velocity = new PVector(3,3);
    gravity = new PVector(0,g);
    gravity.mult(timeFactor);
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
    if (location.x > width) {  // Touche la droite
      velocity.set(elasticity * velocity.x*(-1),velocity.y);
    } else if (location.x < 0) {   // Touche la gauche
      velocity.set(elasticity * velocity.x*(-1),velocity.y);
    }

    if (location.y > height) {  // Touche le bas
      velocity.set(velocity.x,elasticity * velocity.y*(-1));
    } else if (location.y < 0) {  // Touche le haut
      velocity.set(velocity.x,elasticity * velocity.y*(-1));
    }
    
    velocity.add(gravity);
    
    if (location.y >= height) {
      location.set(location.x, height);
    }
    
  } 
}
