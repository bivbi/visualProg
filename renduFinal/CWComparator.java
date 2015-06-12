package ch.epfl.cs211;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import processing.core.PVector;

class CWComparator implements Comparator<PVector> {
	PVector center;

	public CWComparator(PVector center) {
		this.center = center;
	}

	@Override
	public int compare(PVector b, PVector d) {
		if (Math.atan2(b.y - center.y, b.x - center.x) < Math.atan2(d.y
				- center.y, d.x - center.x))
			return -1;
		else
			return 1;
	}

	public static List<PVector> sortCorners(List<PVector> quad) {
		// Sort corners so that they are ordered clockwise
		PVector a = quad.get(0);
		PVector b = quad.get(2);
		PVector center = new PVector((a.x + b.x) / 2, (a.y + b.y) / 2);
		Collections.sort(quad, new CWComparator(center));
		float accumulator = Float.MAX_VALUE;
		int index = 0;
		PVector zero = new PVector(0,0,0);
		for(int i = 0; i < quad.size(); i++) {
			float d = quad.get(i).dist(zero);
			if(d < accumulator) {
				accumulator = d;
				index = i;
			}
		}
		
		Collections.rotate(quad, index);
		
		// TODO:
		// Re-order the corners so that the first one is the closest to the
		// origin (0,0) of the image.
		//
		// You can use Collections.rotate to shift the corners inside the quad.
		return quad;
	}
}