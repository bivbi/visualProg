Mover mover;

void setup() {
   size(800,200); mover = new Mover();
}

void draw() {
  background(255);
  mover.update();
  mover.checkEdges();
  mover.display();
}

class Mover {
  PVector location;
  PVector velocity;
  
  Mover() {
    location = new PVector(width/2, height/2); velocity = new PVector(1, 1);
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
      location.x = 0;
    } else if (location.x < 0) {
      location.x = width;
    }

    if (location.y > height) {
      location.y = 0;
    } else if (location.y < 0) {
          location.y = height;
    }
  } 
}
