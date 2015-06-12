package ch.epfl.cs211.game;

import processing.core.PApplet;
import processing.core.PVector;

public class Mover { // One class for all things that move
	PApplet parent;
	PVector gravityForce = new PVector(0, 0);
	Sphere sphere;

	public Mover(PApplet parent) {
		this.parent = parent;
		sphere = new Sphere(parent);
	}
	
	// display the sphere
	public void display() {
		sphere.display();
	}

	// update the sphere
	public void update() {
		if (!GLOBAL_VAR.addingCylinderMode) {
			sphere.updateCoordinates();
		}
		sphere.checkCollisions();
	}
}