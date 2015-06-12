package ch.epfl.cs211.game;

import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import processing.core.PApplet;
import processing.core.PVector;

public class Sphere {
	PApplet parent;
	PVector coordinates;
	PVector velocity;

	// create a ball in the top left corner of the matrix,
	// it's always used in the box referencial so in the middle of the screen
	Sphere(PApplet parent) {
		this.parent = parent;
		coordinates = new PVector(0, 0);
		velocity = new PVector(0, 0);
	}

	// display the ball at the right coords and in the right color
	public void display() {
		parent.pushMatrix();
		parent.translate(coordinates.x, coordinates.y);
		parent.fill(GLOBAL_VAR.sphereColor);
		parent.sphere(GLOBAL_VAR.sphereRadius);
		parent.popMatrix();
	}

	// Compute the impact of the gravity and friction on the ball
	public void updateCoordinates() {
		PVector gravityForce = new PVector(0, 0);
		gravityForce.x = (float) +sin(toRadians(GLOBAL_VAR.tiltAngleZ))
				* GLOBAL_VAR.g * GLOBAL_VAR.timeFactor;
		gravityForce.y = (float) -sin(toRadians(GLOBAL_VAR.tiltAngleX))
				* GLOBAL_VAR.g * GLOBAL_VAR.timeFactor;

		float normalForce = 1.0f;
		float mu = 0.01f;
		float frictionMagnitude = normalForce * mu;
		PVector friction = velocity.get();
		friction.normalize();
		friction.mult(frictionMagnitude * GLOBAL_VAR.timeFactor); // Friction
																	// factor

		velocity.add(gravityForce); // Change velocity according to gravity
		velocity.add(friction); // Change velocity according to friction

		coordinates.add(velocity); // Change the coordinates according to the
									// velocity
	}

	// Test if there is a collision with an edge or a cylinder
	public void checkCollisions() {
		edgeCollision();
		cylindersCollision();
	}

	// Compute a bounce on an edge, loss of velocity with the elasticity
	// parameter
	private void edgeCollision() {
		if (coordinates.x - GLOBAL_VAR.sphereRadius / 2 < -GLOBAL_VAR.boxWidth / 2) { // LEFT
			computeCollision(new PVector(-GLOBAL_VAR.boxWidth / 2
					+ GLOBAL_VAR.sphereRadius / 2, coordinates.y)); // compute
																	// new
																	// velocity
																	// and
																	// coordinates
			coordinates.x = -GLOBAL_VAR.boxWidth / 2 + GLOBAL_VAR.sphereRadius
					/ 2; // avoid that the
			// sphere leave
			// the box
			velocity.mult(GLOBAL_VAR.elasticity); // reduce speed with velocity
													// factor
			GLOBAL_VAR.score -= velocity.mag();
			GLOBAL_VAR.lastScore = -velocity.mag();
			if (GLOBAL_VAR.score < 0) {
				GLOBAL_VAR.score = 0;
			}
		} else if (coordinates.x + GLOBAL_VAR.sphereRadius / 2 > GLOBAL_VAR.boxWidth / 2) { // RIGHT
			computeCollision(new PVector(GLOBAL_VAR.boxWidth / 2
					- GLOBAL_VAR.sphereRadius / 2, coordinates.y));
			coordinates.x = GLOBAL_VAR.boxWidth / 2 - GLOBAL_VAR.sphereRadius
					/ 2;
			velocity.mult(GLOBAL_VAR.elasticity);
			GLOBAL_VAR.score -= velocity.mag();
			GLOBAL_VAR.lastScore = -velocity.mag();
			if (GLOBAL_VAR.score < 0) {
				GLOBAL_VAR.score = 0;
			}
		}
		if (coordinates.y - GLOBAL_VAR.sphereRadius / 2 < -GLOBAL_VAR.boxHeight / 2) { // TOP
			computeCollision(new PVector(coordinates.x, -GLOBAL_VAR.boxHeight
					/ 2 + GLOBAL_VAR.sphereRadius / 2));
			coordinates.y = -GLOBAL_VAR.boxHeight / 2 + GLOBAL_VAR.sphereRadius
					/ 2;
			velocity.mult(GLOBAL_VAR.elasticity);
			GLOBAL_VAR.score -= velocity.mag();
			GLOBAL_VAR.lastScore = -velocity.mag();
			if (GLOBAL_VAR.score < 0) {
				GLOBAL_VAR.score = 0;
			}
		} else if (coordinates.y + GLOBAL_VAR.sphereRadius / 2 > GLOBAL_VAR.boxHeight / 2) { // BOTTOM
			computeCollision(new PVector(coordinates.x, GLOBAL_VAR.boxHeight
					/ 2 - GLOBAL_VAR.sphereRadius / 2));
			coordinates.y = GLOBAL_VAR.boxHeight / 2 - GLOBAL_VAR.sphereRadius
					/ 2;
			velocity.mult(GLOBAL_VAR.elasticity);
			GLOBAL_VAR.score -= velocity.mag();
			GLOBAL_VAR.lastScore = -velocity.mag();
			if (GLOBAL_VAR.score < 0) {
				GLOBAL_VAR.score = 0;
			}
		}
	}

	// Compute the bounce on a cylinder
	private void cylindersCollision() {
		Cylinder cylinder = new Cylinder(parent, 0, 0);
		boolean collisionHappens = false;
		for (Cylinder c : GLOBAL_VAR.cylinders) { // find if there is a contact
													// with a
			// cylinder and this cylinder
			if (collisionWithCylinder(c.coordinates) && !collisionHappens) {
				collisionHappens = true;
				cylinder = c;
			}
		}
		if (collisionHappens) {
			computeCollision(cylinder.coordinates); // compute the changes
			GLOBAL_VAR.score += 3 * velocity.mag();
			GLOBAL_VAR.lastScore = 3 * velocity.mag();
		}
	}

	// Compute the velocity and coords changes of the sphere
	private void computeCollision(PVector coords) {
		PVector n = coords.get();
		n.sub(coordinates);
		n.normalize(); // get normal vector
		float c = n.x * velocity.x + n.y * velocity.y; // this is the dot
														// product
		PVector V1NN = n.get(); // this is 2 * (v1 dot n) * n
		V1NN.mult(2);
		V1NN.mult(c);
		PVector v2 = velocity.get();
		v2.sub(V1NN); // this is v1 - 2 * (v1 dot n) * n
		velocity.set(v2);
		velocity.mult(GLOBAL_VAR.elasticity);
		coordinates.sub(n); // prevents the cylinder to get in the cylinder it
							// collided with
	}

	// check if there is a collision between the ball and a given cylinder
	// true if the dist between the 2 is less than the sum of the radius
	private boolean collisionWithCylinder(PVector cylinderCoordinates) {
		return coordinates.dist(cylinderCoordinates) <= GLOBAL_VAR.sphereRadius
				+ GLOBAL_VAR.cylinderBaseSize;
	}
}