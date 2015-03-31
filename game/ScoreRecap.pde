class ScoreRecap {
  PGraphics graphic;
  int scoreRecapWidth;
  int h;
  color c;
  float ratio;

  final int updatingRate = 30;
  int updatingCounter    = 0;
  int scoreRecapSize     = 0;
  ArrayList<Double> scoreRecap = new ArrayList<Double>(); 
  int sWidth;
  
  ScoreRecap(int scoreRecapWidth, int h, color c, float ratio) {
    this.scoreRecapWidth = scoreRecapWidth;
    this.h = h;
    sWidth = h/20;
    this.c = c;
    this.ratio = ratio;
    graphic = createGraphics(scoreRecapWidth, h, P2D);
  }
  
  PGraphics drawScoreRecap() {
    updatingCounter += 1;
    if(updatingCounter > updatingRate) {
      ++scoreRecapSize;
      updatingCounter -= updatingRate;
      scoreRecap.add((double)score);
    }
    graphic.beginDraw();
      for(int i = 0; i < scoreRecapSize; ++i) {
        graphic.pushMatrix();
          graphic.translate(i*sWidth, 0);
          for(int j = 0; j < (int)java.lang.Math.ceil(scoreRecap.get(i)); ++j) {
            graphic.pushMatrix();
              graphic.translate(0,h-(j*sWidth));
              drawSmallBox();
            graphic.popMatrix();
          }
        graphic.popMatrix();
      }
    graphic.endDraw();
    return graphic;
  }
  void drawSmallBox() {
    graphic.stroke(color(255));
    graphic.strokeWeight(2);
    graphic.fill(#00FAE7);
    graphic.rect(sWidth/2,sWidth/2,sWidth,sWidth);
  }
  
  
}
