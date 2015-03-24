class TopView {
  PGraphics graphic;
  int topViewWidth;
  int h;
  color c;
  float ratio;

  TopView(int topViewWidth, int h, color c) {
    this.topViewWidth = topViewWidth;
    this.h            = h;
    this.c            = c;

    ratio = h/boxHeight;

    graphic = createGraphics(topViewWidth, h, P2D);
  }


  PGraphics drawTopView() {
    graphic.beginDraw();
    graphic.background(c);

    //BOX
    graphic.fill(boxColor);
    graphic.rect(0, 0, boxWidth*ratio, boxHeight*ratio);

    //BALL
    graphic.pushMatrix();
      PVector coords = mover.sphere.coordinates;
      graphic.translate(ratio * coords.x + boxWidth * ratio / 2.0, ratio * coords.y + boxHeight*ratio / 2.0);
      graphic.fill(sphereColor);
      graphic.ellipse(0, 0, sphereRadius*2*ratio, sphereRadius*2*ratio);
    graphic.popMatrix();

    //CYLINDERS
    for (Cylinder c : cylinders) {
      graphic.pushMatrix();
        graphic.translate(ratio * (c.coordinates.x +boxWidth/2.0), ratio * (c.coordinates.y + boxHeight/2.0));
        graphic.fill(c.c);
        graphic.ellipse(0, 0, cylinderBaseSize*2*ratio, cylinderBaseSize*2*ratio);
      graphic.popMatrix();
    }

    graphic.endDraw();

    return graphic;
  }
}

