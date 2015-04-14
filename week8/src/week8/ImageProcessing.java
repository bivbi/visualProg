package week8;

import processing.core.PApplet;
import processing.core.PImage;

public class ImageProcessing extends PApplet {
	PImage img;
	PImage result;
	
	public void setup() {
		size(800, 600);
		img = loadImage("../imgLego/board1.jpg");
		noLoop(); // no interactive behaviour: draw() will be called only once.
	}

	public void draw() {
		image(img, 0, 0);
	}
}