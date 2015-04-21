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
		size(appletWidth * 2, 600);
		colorMinBar = new HScrollBar(this, 0f, 580f, appletWidth * 2, 20f);
		colorMaxBar = new HScrollBar(this, 0f, 540f, appletWidth * 2, 20f);
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
		// colorMaxBar.display();
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
		/*
		 * PImage result = createImage(appletWidth, height, RGB); for (int i =
		 * 0; i < img.width * img.height; ++i) { if (colorMinBar.getPos()*255 <=
		 * hue(img.pixels[i]) && hue(img.pixels[i]) <= colorMaxBar.getPos()*255)
		 * { result.pixels[i] = img.pixels[i]; } else { result.pixels[i] =
		 * color(0, 0, 0); } } return result;
		 */
		/*float[][] kernel = { { 9, 12, 9 }, { 12, 15, 12 }, { 9, 12, 9 } };
		return convolute(img, kernel);*/

		//return convolute(convolute(img, hKernel), vKernel);
		return sobel(img);
	}

	public PImage convolute(PImage img, float[][] kernel) {

		float weight = colorMinBar.getPos() * 100;

		PImage result = createImage(img.width, img.height, ALPHA);

		for (int x = 0; x < img.width; ++x) {
			for (int y = 0; y < img.height; ++y) {
				int reds = 0;
				int greens = 0;
				int blues = 0;
				for (int i = 0; i < kernel.length; ++i) {
					for (int j = 0; j < kernel[i].length; ++j) {
						if (0 <= x - i + kernel.length / 2
								&& x - i + kernel.length / 2 < img.width
								&& 0 <= y - j + kernel[i].length / 2
								&& y - j + kernel[i].length / 2 < img.height) {
							reds += red(img.pixels[x - i + kernel.length / 2
									+ (y - j + kernel.length / 2) * img.width])
									* kernel[i][j];
							blues += blue(img.pixels[x - i + kernel.length / 2
									+ (y - j + kernel.length / 2) * img.width])
									* kernel[i][j];
							greens += green(img.pixels[x - i + kernel.length
									/ 2 + (y - j + kernel.length / 2)
									* img.width])
									* kernel[i][j];
						}
					}
				}

				result.pixels[x + y * img.width] = color(reds / weight, greens
						/ weight, blues / weight);
			}
		}

		return result;
	}

	public PImage sobel(PImage img) {
		float[][] hKernel = { { 0, 1, 0 }, { 0, 0, 0 }, { 0, -1, 0 } };
		float[][] vKernel = { { 0, 0, 0 }, { 1, 0, -1 }, { 0, 0, 0 } };
		PImage result = createImage(img.width, img.height, ALPHA);
		// clear the image
		for (int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = color(0);
		}
		float max = 0;
		float[] buffer = new float[img.width * img.height];

		for (int x = 0; x < img.width; ++x) {
			for (int y = 0; y < img.height; ++y) {
				int sum_h = 0;
				int sum_v = 0;
				for(int i = 0; i < hKernel.length; ++i) {
					for(int j = 0; j < hKernel.length; ++j) {
						int cx=f(x, i, hKernel.length, img.width-1, 0);
						int cy=f(y, j, hKernel.length, img.height-1, 0);
						sum_h += img.pixels[cx+cy*img.width]*hKernel[i][j];
						sum_v += img.pixels[cx+cy*img.width]*vKernel[i][j];
					}
				}
				float sum = sqrt(pow(sum_h, 2) + pow(sum_v, 2));
				if(sum > max)
					max = sum;
				buffer[x+y*img.width] = sum;
			}
		}

		
		
		
		for (int y = 2; y < img.height - 2; y++) { // Skip top and bottom edges
			for (int x = 2; x < img.width - 2; x++) { // Skip left and right
				if (buffer[y * img.width + x] > (int) (max * 0.3f)) { // 30% of
																		// the
																		// max
					result.pixels[y * img.width + x] = color(255);
				} else {
					result.pixels[y * img.width + x] = color(0);
				}
			}
		}
		return result;
	}
	
	private int f(int x, int i, int kLength, int max, int min) {
		if(x-i+kLength/2 < min)
			return min;
		else if (x-i+kLength/2 > max)
			return max;
		else
			return x-i+kLength/2;
	}
}