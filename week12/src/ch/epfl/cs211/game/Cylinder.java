package ch.epfl.cs211.game;

import processing.core.PApplet;
import processing.core.PVector;
import static processing.core.PConstants.HALF_PI;

public class Cylinder {
	  PApplet parent;
	  PVector coordinates = new PVector(0,0);
	  int c = parent.color(0,150,0);//color(random(0,255), random(0,255), random(0,255));
	  
	  Cylinder(PApplet parent, float x, float y) {
		this.parent = parent;
	    coordinates.x = x;
	    coordinates.y = y;
	  }
	  
	  //display a cylinder with or without a color
	  //at the right coordinates
	  public void display(boolean noFill) {
	    parent.pushMatrix();
	    parent.translate(coordinates.x, coordinates.y);
	    parent.rotateX(HALF_PI); //makes the cylinder on the board in the right angle
	    
	    parent.shape(GLOBAL_VAR.tree);
	    parent.popMatrix();
	  }
	}