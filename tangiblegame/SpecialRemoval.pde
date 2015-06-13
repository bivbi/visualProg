class SpecialRemoval {  //Class for the collision with cylinder audio,
//It used to contain an explosion animation when the ball touches a cylinder,
//but since it's not suppose to remove the cylinder we removed it
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

