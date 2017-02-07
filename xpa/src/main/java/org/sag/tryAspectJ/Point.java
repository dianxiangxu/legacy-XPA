package org.sag.tryAspectJ;

/**
 * Created by shuaipeng on 9/7/16.
 */
public class Point {
    double x;
    double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public int move(int i) {
		return 0;
	}

    public static void main(String[] args) {
        new Point(0, 0).move(1);
    }
}
