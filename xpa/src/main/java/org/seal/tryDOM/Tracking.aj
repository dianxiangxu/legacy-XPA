package org.sag.tryDOM;
import org.sag.tryAspectJ.Point;
aspect Tracking {
    pointcut trackMove():
        call (* Point.move(*));
//        call (* org.sag.tryAspectJ.Point.move(*));

    before(): trackMove() {
        System.out.println("entering " + thisJoinPoint);
    }
}