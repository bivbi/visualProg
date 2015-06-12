package ch.epfl.cs211.game;

import java.util.ArrayList;

import processing.core.PGraphics;
import static java.lang.Math.round;

public class ScoreRecap {
	Game parent;
	PGraphics graphic;
	int scoreRecapWidth;
	int h;
	int c;
	float ratio;

	final int updatingRate = 10;
	int updatingCounter = 0;
	int scoreRecapSize = 0;
	ArrayList<Double> scoreRecap = new ArrayList<Double>();
	int sWidth;
	int numCol;

	ScoreRecap(Game parent, int scoreRecapWidth, int h, int c, float ratio) {
		this.parent = parent;
		this.scoreRecapWidth = scoreRecapWidth;
		this.h = h;
		sWidth = h / 20;
		this.c = c;
		this.ratio = ratio;
		graphic = parent.createGraphics(scoreRecapWidth, h, parent.P2D);
		numCol = scoreRecapWidth / sWidth;
	}

	public PGraphics drawScoreRecap() {
		updatingCounter += 1;
		if (updatingCounter > updatingRate) {
			++scoreRecapSize;
			updatingCounter -= updatingRate;
			scoreRecap.add((double) GLOBAL_VAR.score);
		}
		graphic.beginDraw();
		graphic.background(c);
		int i0 = (int) round(GLOBAL_VAR.bottomPanel.scrollbar.getPos() * numCol);
		for (int i = i0; i < scoreRecapSize; ++i) {
			graphic.pushMatrix();
			graphic.translate((i - i0) * sWidth, 0);
			for (int j = 0; j < (int) (java.lang.Math.ceil(scoreRecap.get(i)) / 2.5f); ++j) {
				graphic.pushMatrix();
				graphic.translate(0, h - (j * sWidth));
				drawSmallBox();
				graphic.popMatrix();
			}
			graphic.popMatrix();
		}
		graphic.endDraw();
		return graphic;
	}

	public void drawSmallBox() {
		graphic.stroke(parent.color(255));
		graphic.strokeWeight(2);
		graphic.fill(0xff00FAE7);
		graphic.rect(sWidth / 2, sWidth / 2, sWidth, sWidth);
	}

}
