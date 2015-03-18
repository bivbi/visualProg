class SpecialRemoval {
  int imageCount = 17;
  PImage[] images;
  float w = 71/2.0;
  float h = 100/2.0;
  float xpos;
  float ypos;
  int frame = 0;
  
  SpecialRemoval(float x, float y, PImage[] images) {
    xpos = x-w;
    ypos = y-h;
    this.images = images;
  }
  
  void display() {
    image(images[frame/3], xpos, ypos);
    if(!addingCylinderMode) {
      ++frame;
    }
    if(frame/3 >= imageCount) {
      specialRemoveBegin = false;
    }
  }
}
