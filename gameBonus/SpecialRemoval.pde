class SpecialRemoval {
  int imageCount = 17;
  AudioPlayer cylinderAudio;
  float w = 71/2.0;
  float h = 100/2.0;
  float xpos;
  float ypos;
  int frame = 0;

  SpecialRemoval(float x, float y, AudioPlayer cylinderAudio) {
    xpos = x-w;
    ypos = y-h;
    this.cylinderAudio = cylinderAudio;
  }

  void display() {
    if (!addingCylinderMode) {
      ++frame;
    }
    if (frame >= cylinderSoundLength) {
      cylinderAudio.pause();
    }
  }
}

