package shredders;

import battlecode.common.*;

public class CatAttacker extends BabyRat {
    public CatAttacker(RobotController rc) {
        super(rc);
        rc.setIndicatorString("Cat attacker reporting for duty");
    }

    public void doAction() throws GameActionException {
        // search for cheese
        RobotInfo[] nearbyInfos = rc.senseNearbyRobots();

        MapLocation catLoc = null;
        for (RobotInfo info : nearbyInfos) {
            if (info.getType().isCatType()) {
                Direction toCat = rc.getLocation().directionTo(catLoc);
                catLoc = info.getLocation();
                System.out.println("FoundCat at " + catLoc.toString()); // diagnostics              
                if (rc.canTurn(toCat)) {
                    rc.turn(toCat);
                    break;
                }
            }

        }

        MapLocation nextLoc = rc.adjacentLocation(rc.getDirection());
        if (rc.canRemoveDirt(nextLoc)) {
            rc.removeDirt(nextLoc);
        }

        if (rc.canMoveForward()) {
            rc.moveForward();
            rc.setIndicatorString("Finding cat.");
        } else {
            d = directions[rand.nextInt(directions.length-1)];
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
