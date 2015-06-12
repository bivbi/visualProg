package ch.epfl.cs211.game;

import ch.epfl.cs211.HScrollbar;
import processing.core.PApplet;
import processing.core.PGraphics;

public class BottomPanel {
	Game parent;
	TopView topView;
	ScoreBox scoreBox;
	ScoreRecap scoreRecap;
	PGraphics bottomPanel;
	int bottomPanelWidth;
	int h;
	int bottomPanelColor;
	float ratio;
	int blankWidth;
	int HScrollHeight;
	HScrollbar scrollbar;

	float score = 0;
	float lastScore = 0;

	BottomPanel(Game parent, int bottomPanelWidth, int topViewWidth,
			int scoreBoxWidth, int h, int bottomPanelColor, int space,
			float ratio, int blankWidth) {
		this.parent = parent;
		this.bottomPanelWidth = bottomPanelWidth;
		this.h = h;
		this.bottomPanelColor = bottomPanelColor;
		this.ratio = ratio;
		this.blankWidth = blankWidth;
		this.HScrollHeight = 4 * blankWidth;

		bottomPanel = parent.createGraphics(bottomPanelWidth, h, parent.P2D);
		bottomPanel.beginDraw();
		bottomPanel.background(bottomPanelColor);
		bottomPanel.endDraw();

		topView = new TopView(parent, topViewWidth, h - 2 * blankWidth,
				bottomPanelColor);
		scoreBox = new ScoreBox(parent, scoreBoxWidth, h - 2 * blankWidth, ratio,
				space, bottomPanelColor);
		scoreRecap = new ScoreRecap(parent, GLOBAL_VAR.width - topViewWidth - scoreBoxWidth, h - 2
				* blankWidth - HScrollHeight, bottomPanelColor, ratio);
		scrollbar = new HScrollbar(parent, topView.topViewWidth
				+ scoreBox.scoreBoxWidth + 4 * blankWidth, GLOBAL_VAR.height
				- h + blankWidth + scoreRecap.h, GLOBAL_VAR.width
				- topViewWidth - scoreBoxWidth - 6 * blankWidth, HScrollHeight);
		scrollbar.sliderPosition = scrollbar.xPosition;
	}

	public void drawBottomPanel() {
		// Adding score to array
		parent.image(bottomPanel, 0, GLOBAL_VAR.height - h);
		parent.image(topView.drawTopView(), blankWidth, GLOBAL_VAR.height - h
				+ blankWidth);
		parent.image(scoreBox.drawScoreBox(), topView.topViewWidth + 2
				* blankWidth, GLOBAL_VAR.height - h + blankWidth);
		parent.image(scoreRecap.drawScoreRecap(), topView.topViewWidth
				+ scoreBox.scoreBoxWidth + 4 * blankWidth, GLOBAL_VAR.height
				- h + blankWidth);
		scrollbar.display();
	}
}
