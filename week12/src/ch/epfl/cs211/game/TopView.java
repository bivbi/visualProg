package ch.epfl.cs211.game;

import processing.core.*;

public class TopView {
	PGraphics graphic;
	int topViewWidth;
	int h;
	int c;
	float ratio;
	Game parent;

	TopView(Game parent, int topViewWidth, int h, int c) {
		this.parent = parent;
		this.topViewWidth = topViewWidth;
		this.h = h;
		this.c = c;

		ratio = h / GLOBAL_VAR.boxHeight;

		graphic = parent.createGraphics(topViewWidth, h, parent.P2D);
	}

	public PGraphics drawTopView() {
		graphic.beginDraw();
		graphic.background(c);

		// BOX
		graphic.fill(GLOBAL_VAR.boxColor);
		graphic.rect(0, 0, GLOBAL_VAR.boxWidth * ratio, GLOBAL_VAR.boxHeight * ratio);

		// BALL
		graphic.pushMatrix();
		PVector coords = parent.mover.sphere.coordinates;
		graphic.translate(ratio * coords.x + GLOBAL_VAR.boxWidth * ratio / 2.0f, ratio
				* coords.y + GLOBAL_VAR.boxHeight * ratio / 2.0f);
		graphic.fill(GLOBAL_VAR.sphereColor);
		graphic.ellipse(0, 0, GLOBAL_VAR.sphereRadius * 2 * ratio, GLOBAL_VAR.sphereRadius * 2
				* ratio);
		graphic.popMatrix();

		// CYLINDERS
		for (Cylinder c : GLOBAL_VAR.cylinders) {
			graphic.pushMatrix();
			graphic.translate(ratio * (c.coordinates.x + GLOBAL_VAR.boxWidth / 2.0f),
					ratio * (c.coordinates.y + GLOBAL_VAR.boxHeight / 2.0f));
			graphic.fill(c.c);
			graphic.ellipse(0, 0, GLOBAL_VAR.cylinderBaseSize * 2 * ratio,
					GLOBAL_VAR.cylinderBaseSize * 2 * ratio);
			graphic.popMatrix();
		}

		graphic.endDraw();

		return graphic;
	}
}
