package teacher07a;

import battlecode.common.*;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

public class CatAttacker extends BabyRat {

    int senseInterval = 5;

    public CatAttacker(RobotController rc) {
        super(rc);
        rc.setIndicatorString("Cat attacker reporting for duty");
        setRandomDirection();
    }

    public void doAction() throws GameActionException {
        // search for cheese

        MapLocation here = rc.getLocation();

        RobotInfo[] nearbyInfos = rc.senseNearbyRobots();

        System.out.println("Sensed " + nearbyInfos.length + " robots");
        MapLocation catLoc = null;
        for (RobotInfo info : nearbyInfos) {
            if (info.getType().isCatType()) {
                Direction toCat = here.directionTo(catLoc);
                catLoc = info.getLocation();                
                System.out.println("Found cat at " + catLoc.toString());
                if (rc.canTurn(toCat) && (d != toCat)) {
                    d = toCat;
                    rc.turn(toCat);
                }
            }
            
        }

        if (rc.canAttack(catLoc)) {
            rc.attack(catLoc);
        }

        MapLocation nextLoc = rc.adjacentLocation(rc.getDirection());
        if (rc.canRemoveDirt(nextLoc)) {
            rc.removeDirt(nextLoc);
        }

        if (rc.canMoveForward()) {
            rc.moveForward();
            rc.setIndicatorString("Finding cat.");
        } else {
            if (gettingUnstuck && (catLoc != null)) {
                // If we are stuck while getting unstuck, just turn back to the cat
                gettingUnstuck = false;
                d = here.directionTo(catLoc);
                rc.setIndicatorString("Unstuck from finding cat, turning to " + d.toString());
            } else {
                d = directions[rand.nextInt(directions.length-1)];
                gettingUnstuck = true;
                rc.setIndicatorString("Stuck while finding cat, turning to " + d.toString());
            }
            if (rc.canTurn()) {
                rc.turn(d);
            }
            rc.setIndicatorString("Blocked while finding cat, turning " + d.toString());
            return;
        }

        if ((catLoc != null) && rc.canAttack(catLoc)) {
            rc.attack(catLoc);
            rc.setIndicatorString("Victory or death!");
        }

    }

    
}
