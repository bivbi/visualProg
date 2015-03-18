class SpecialRemoval {
  int imageCount = 17;
  PImage[] images;
  AudioPlayer cylinderAudio;
  float w = 71/2.0;
  float h = 100/2.0;
  float xpos;
  float ypos;
  int frame = 0;

  SpecialRemoval(float x, float y, PImage[] images, AudioPlayer cylinderAudio) {
    xpos = x-w;
    ypos = y-h;
    this.images = images;
    this.cylinderAudio = cylinderAudio;
  }

  void display() {
    image(images[frame/3], xpos, ypos);
    if (!addingCylinderMode) {
      ++frame;
    }
    if (frame/3 >= imageCount) {
      specialRemoveBegin = false;
    }
    if (frame >= cylinderSoundLength) {
      cylinderAudio.pause();
    }
  }
}

