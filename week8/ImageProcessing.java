package week8;

import processing.core.PApplet;
import processing.core.PImage;

public class ImageProcessing extends PApplet {
	PImage img;
	PImage result;
	HScrollBar colorMinBar;
	HScrollBar colorMaxBar;
	int i = 1;
	int appletWidth = 800;

	public void setup() {
		size(appletWidth*2, 600);
		colorMinBar = new HScrollBar(this, 0f, 580f, appletWidth*2, 20f);
		colorMaxBar = new HScrollBar(this, 0f, 540f, appletWidth*2, 20f);
		String imgName = "board" + String.valueOf(i) + ".jpg";
		img = loadImage(imgName);
		result = createImage(appletWidth, height, RGB);
	}

	public void draw() {
		img = load();
		result = updateResult();
		background(color(0, 0, 0));
		image(img, 0, 0);
		image(result, appletWidth, 0);
		colorMinBar.display();
		colorMinBar.update();
		//colorMaxBar.display();
		colorMaxBar.update();
	}

	private PImage load() {
		String imgName = "board" + String.valueOf(i) + ".jpg";
		return loadImage(imgName);
	}
	
	public void keyPressed() {
		if (key == '1') {
			i = 1;
		}
		if (key == '2') {
			i = 2;
		}
		if (key == '3') {
			i = 3;
		}
		if (key == '4') {
			i = 4;
		}
	}

	private PImage updateResult() {
		/*PImage result = createImage(appletWidth, height, RGB);
		for (int i = 0; i < img.width * img.height; ++i) {
			if (colorMinBar.getPos()*255 <= hue(img.pixels[i]) && hue(img.pixels[i]) <= colorMaxBar.getPos()*255) {
				result.pixels[i] = img.pixels[i];
			} else {
				result.pixels[i] = color(0, 0, 0);
			}
		}
		return result;*/
		return convolute(img);
	}
	
	public PImage convolute(PImage img) {
		float[][] kernel = { { 9, 12, 9},
							 { 12, 15, 12},
							 { 9, 12, 9} };
		
		float weight = colorMinBar.getPos()*100;
		
		PImage result = createImage(img.width, img.height, ALPHA);
		
		for(int x = 0; x < img.width; ++x) {
			for(int y = 0; y < img.height; ++y) {
				int reds = 0;
				int greens = 0;
				int blues = 0;
				for(int i = 0; i < kernel.length; ++i) {
					for(int j = 0; j < kernel[i].length; ++j) {
						if(0 <= x-i+kernel.length/2 && x-i+kernel.length/2 < img.width &&
							0 <= y-j+kernel[i].length/2 && y-j+kernel[i].length/2 < img.height) {
							reds += red(img.pixels[x-i+kernel.length/2+(y-j+kernel.length/2)*img.width])*kernel[i][j];
							blues += blue(img.pixels[x-i+kernel.length/2+(y-j+kernel.length/2)*img.width])*kernel[i][j];
							greens += green(img.pixels[x-i+kernel.length/2+(y-j+kernel.length/2)*img.width])*kernel[i][j];
						}
					}
				}
				
				result.pixels[x+y*img.width] = color(reds/weight, greens/weight, blues/weight);
			}
		}

		return result;
	}
}