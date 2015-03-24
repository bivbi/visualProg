class BottomPanel {
  TopView topView;
  ScoreBox scoreBox;
  PGraphics bottomPanel;
  int bottomPanelWidth;
  int h;
  color bottomPanelColor;
  float ratio;
  int blankWidth;
  
  BottomPanel(int bottomPanelWidth, int topViewWidth, int scoreBoxWidth, int h, color bottomPanelColor, int space, float ratio, int blankWidth) {
    this.bottomPanelWidth = bottomPanelWidth;
    this.h                = h;
    this.bottomPanelColor = bottomPanelColor;
    this.ratio            = ratio;
    this.blankWidth       = blankWidth;
    
    
    bottomPanel = createGraphics(bottomPanelWidth, h, P2D);
    bottomPanel.beginDraw();
    bottomPanel.background(bottomPanelColor);  
    bottomPanel.endDraw();
    
    topView  = new TopView(topViewWidth, h-2*blankWidth, bottomPanelColor);
    scoreBox = new ScoreBox(scoreBoxWidth, h-2*blankWidth, ratio, space, bottomPanelColor);
  }
  
  void drawBottomPanel() {
    image(bottomPanel, 0, height-h);
    image(topView.drawTopView(), blankWidth, height-h + blankWidth);
    image(scoreBox.drawScoreBox(), topView.topViewWidth + 2 * blankWidth, height-h + blankWidth);
  }  
}
