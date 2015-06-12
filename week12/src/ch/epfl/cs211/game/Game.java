package ch.epfl.cs211.game;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

public class Game extends PApplet {
	protected Mover mover = new Mover(this);

	public void setup() {
		size(800, 800, P3D);
		GLOBAL_VAR.tree = loadShape("tree.obj");
		GLOBAL_VAR.tree.scale(20);

		GLOBAL_VAR.bottomPanel = new BottomPanel(this, width,
				(int) (height * GLOBAL_VAR.ratio), GLOBAL_VAR.scoreBoxWidth,
				(int) (height * GLOBAL_VAR.ratio), GLOBAL_VAR.bottomPanelColor,
				GLOBAL_VAR.space, GLOBAL_VAR.ratio, GLOBAL_VAR.blankWidth);
		frameRate(GLOBAL_VAR.fps);

		createCylinderShape(GLOBAL_VAR.cylinderBaseSize,
				GLOBAL_VAR.cylinderHeight);

		noStroke();
	}

	// Move the box along X and Y
	public void mouseDragged() {
		if (mouseY < height - GLOBAL_VAR.bottomPanel.h) {
			if (!GLOBAL_VAR.addingCylinderMode) {
				GLOBAL_VAR.tiltAngleX -= GLOBAL_VAR.speed * (mouseY - pmouseY)
						* 2 * GLOBAL_VAR.maxTilt / height; // Same thing as a
															// map(0,width,-60,60)
															// but no probleme
															// when
				GLOBAL_VAR.tiltAngleZ += GLOBAL_VAR.speed * (mouseX - pmouseX)
						* 2 * GLOBAL_VAR.maxTilt / width; // you move the mouse
															// between 2 drags

				// Check if the Angle are bigger than the maxTilt
				if (GLOBAL_VAR.tiltAngleX > +GLOBAL_VAR.maxTilt)
					GLOBAL_VAR.tiltAngleX = +GLOBAL_VAR.maxTilt;
				if (GLOBAL_VAR.tiltAngleX < -GLOBAL_VAR.maxTilt)
					GLOBAL_VAR.tiltAngleX = -GLOBAL_VAR.maxTilt;
				if (GLOBAL_VAR.tiltAngleZ > +GLOBAL_VAR.maxTilt)
					GLOBAL_VAR.tiltAngleZ = +GLOBAL_VAR.maxTilt;
				if (GLOBAL_VAR.tiltAngleZ < -GLOBAL_VAR.maxTilt)
					GLOBAL_VAR.tiltAngleZ = -GLOBAL_VAR.maxTilt;
			}
		} else {
			GLOBAL_VAR.bottomPanel.scrollbar.update();
		}
	}

	// Rotation along Y, entering addingCylinderMode and toogle rotation along Y
	public void keyPressed() {
		if (key == CODED) {
			if (keyCode == LEFT && !GLOBAL_VAR.addingCylinderMode && !GLOBAL_VAR.ignoreYRotation)
				GLOBAL_VAR.angleY -= GLOBAL_VAR.speedFactor * GLOBAL_VAR.speed;
			if (keyCode == RIGHT && !GLOBAL_VAR.addingCylinderMode && !GLOBAL_VAR.ignoreYRotation)
				GLOBAL_VAR.angleY += GLOBAL_VAR.speedFactor * GLOBAL_VAR.speed;
			if (keyCode == SHIFT)
				GLOBAL_VAR.addingCylinderMode = true;
		} else {
			if (key == 'y') {
				GLOBAL_VAR.ignoreYRotation = !GLOBAL_VAR.ignoreYRotation;
			}
		}

		if (GLOBAL_VAR.angleY >= 360) // to display a nice angle between 0\u00b0 and
							// 360\u00b0
			GLOBAL_VAR.angleY -= 360;
		if (GLOBAL_VAR.angleY < 0)
			GLOBAL_VAR.angleY += 360;
	}

	// Rotation speed increase or decrease with mouseWheel
	public void mouseWheel(MouseEvent event) {
		if (!GLOBAL_VAR.addingCylinderMode) {
			if (event.getCount() < 0) {
				if (GLOBAL_VAR.speed < GLOBAL_VAR.maxSpeed)
					GLOBAL_VAR.speed += GLOBAL_VAR.speedScaling;
			} else {
				if (GLOBAL_VAR.speed > GLOBAL_VAR.minSpeed)
					GLOBAL_VAR.speed -= GLOBAL_VAR.speedScaling;
			}
			GLOBAL_VAR.speed = Math.round(10 * GLOBAL_VAR.speed); // to have one digit float
			GLOBAL_VAR.speed /= 10.0;
		}
	}

	// Leave addingCylinderMode when releasing SHIFT key
	public void keyReleased() {
		if (key == CODED) {
			if (keyCode == SHIFT)
				GLOBAL_VAR.addingCylinderMode = false;
		}
	}

	//display a transparent cylinder at the mouse coordinates
	public void cursorCylinder() {
	  pushMatrix();
	  translate(0, GLOBAL_VAR.cylinderOffset, 0); //Offset so the cylinder is not in the box
	  rotateX(HALF_PI); //to transphere 2D coordinates of the cylinder into 3D (y->z)
	  PVector coords = cylinderCheckEdges(mouseX-width/2, mouseY-height/2);
	  float x = clamp(mouseX-width/2, -GLOBAL_VAR.boxWidth/2, GLOBAL_VAR.boxWidth/2);

	  float y = clamp(mouseY-height/2, -GLOBAL_VAR.boxHeight/2, GLOBAL_VAR.boxHeight/2);

	  if (mouseX-width/2 == coords.x && mouseY-height/2 == coords.y)
	    noCursor(); //hide cursor if mouse in the box
	  else
	    cursor();  //display cursor if mouse outside the box

	  Cylinder cylinder = new Cylinder(this, coords.x, coords.y);
	  cylinder.display(true); //the boolean is the noFill boolean, if true the cylinder is transparent
	  popMatrix();
	}
	
	// Add cylinder ONLY if it will create no collision
	public void mouseClicked() {
		if (GLOBAL_VAR.addingCylinderMode && cylinderCheckBall(mouseX - width / 2, mouseY - height / 2)) {
			PVector coords = cylinderCheckEdges(mouseX - width / 2, mouseY
					- height / 2);
			GLOBAL_VAR.cylinders.add(new Cylinder(this, coords.x, coords.y));
		}
	}

	//Check if the cylinder at coords x and y is not in contact with the ball
	//Used to avoid placing cylinder on the ball
	private boolean cylinderCheckBall(float x, float y) {
	  return (mover.sphere.coordinates.dist(new PVector(x, y))) > GLOBAL_VAR.sphereRadius + GLOBAL_VAR.cylinderBaseSize;
	}

	//Check if the cylinder at coords x and y isn't outside the box
	//If it is, return coordinates where it would be in the box
	private PVector cylinderCheckEdges(float x, float y) {
	  float x2 = clamp(x, -GLOBAL_VAR.boxWidth/2 -GLOBAL_VAR.sphereRadius +1, GLOBAL_VAR.boxWidth/2 + GLOBAL_VAR.sphereRadius -1);
	  float y2 = clamp(y, -GLOBAL_VAR.boxHeight/2 -GLOBAL_VAR.sphereRadius +1, GLOBAL_VAR.boxHeight/2 + GLOBAL_VAR.sphereRadius -1);
	  return new PVector(x2, y2);
	}
	
	public void createCylinderShape(float cylinderBaseSize, float cylinderHeight) {
		float angle;
		float[] x = new float[GLOBAL_VAR.cylinderResolution + 1];
		float[] y = new float[GLOBAL_VAR.cylinderResolution + 1];

		for (int i = 0; i < x.length; ++i) {
			angle = (TWO_PI / GLOBAL_VAR.cylinderResolution) * i;
			x[i] = sin(angle) * cylinderBaseSize;
			y[i] = cos(angle) * cylinderBaseSize;
		}

		GLOBAL_VAR.openCylinder = createShape();
		GLOBAL_VAR.openCylinder.beginShape(QUAD_STRIP);
		for (int i = 0; i < x.length; ++i) {
			GLOBAL_VAR.openCylinder.vertex(x[i], 0, y[i]);
			GLOBAL_VAR.openCylinder.vertex(x[i], cylinderHeight, y[i]);
		}
		GLOBAL_VAR.openCylinder.endShape();

		GLOBAL_VAR.topCylinder = createShape();
		GLOBAL_VAR.topCylinder.beginShape(TRIANGLES);
		for (int i = 0; i < x.length; ++i) {
			GLOBAL_VAR.topCylinder.vertex(x[i], 0, y[i]);
			GLOBAL_VAR.topCylinder.vertex(x[clamp(i + 1, 0, x.length - 1)], 0,
					y[clamp(i + 1, 0, x.length - 1)]);
			GLOBAL_VAR.topCylinder.vertex(0, 0, 0);
		}
		GLOBAL_VAR.topCylinder.endShape();

		GLOBAL_VAR.bottomCylinder = createShape();
		GLOBAL_VAR.bottomCylinder.beginShape(TRIANGLES);
		for (int i = 0; i < x.length; ++i) {
			GLOBAL_VAR.bottomCylinder.vertex(x[i], cylinderHeight, y[i]);
			GLOBAL_VAR.bottomCylinder.vertex(x[clamp(i + 1, 0, x.length - 1)],
					cylinderHeight, y[clamp(i + 1, 0, x.length - 1)]);
			GLOBAL_VAR.bottomCylinder.vertex(0, cylinderHeight, 0);
		}
		GLOBAL_VAR.bottomCylinder.endShape();
	}

	// Simple clamp function, return x if it is in the boundaries, otherwise the
	// right boundarie
	private static int clamp(int x, int min, int max) {
		return (int) clamp((float) x, (float) min, (float) max);
	}

	// same but with floats
	private static float clamp(float x, float min, float max) {
		if (x > max)
			return max;
		else if (x < min)
			return min;
		else
			return x;
	}
}
