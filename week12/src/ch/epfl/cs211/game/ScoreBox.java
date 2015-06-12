package ch.epfl.cs211.game;

import processing.core.PGraphics;

public class ScoreBox {
	Game parent;
	int scoreBoxWidth;
	int h;
	PGraphics graphic;
	float ratio;
	int space;
	int c;

	ScoreBox(Game parent, int scoreBoxWidth, int h, float ratio, int space, int c) {
		this.parent = parent;
		this.scoreBoxWidth = scoreBoxWidth;
		this.h = h;
		this.ratio = ratio;
		this.space = space;
		this.c = c;
		graphic = parent.createGraphics(scoreBoxWidth, h, parent.P2D);
	}

	public PGraphics drawScoreBox() {
		graphic.beginDraw();
		graphic.background(c);
		graphic.pushMatrix();
		// White rect
		graphic.noFill();
		graphic.strokeWeight(4);
		graphic.stroke(255);
		graphic.rect(0, 0, scoreBoxWidth, h);
		graphic.fill(0);
		graphic.translate(space, 0);

		// score display
		graphic.pushMatrix();
		graphic.translate(0, space + 4); // 4=stroke weight
		graphic.text("Total Score:", 0, 0);
		graphic.translate(0, 8 * 2); // 8 = font size
		graphic.text(GLOBAL_VAR.score, 0, 0);
		graphic.popMatrix();

		// velocity
		graphic.pushMatrix();
		graphic.translate(0, h / 2.0f - 8);
		graphic.text("Velocity", 0, 0);
		graphic.text(parent.mover.sphere.velocity.mag(), 0, 8 * 2);
		graphic.popMatrix();

		// last score
		graphic.pushMatrix();
		graphic.translate(0, h - space - 4 - 8); // 4=stroke weight
		graphic.text("Last Score:", 0, 0);
		graphic.translate(0, 8 * 2); // 8 = font size
		graphic.text(GLOBAL_VAR.lastScore, 0, 0);
		graphic.popMatrix();
		graphic.noStroke();
		graphic.popMatrix();

		graphic.endDraw();

		return graphic;
	}
}