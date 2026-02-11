package shredders;

import battlecode.common.*;

public class Attacker extends BabyRat {
    public Attacker(RobotController rc) {
        super(rc);
        rc.setIndicatorString("Cat attacker reporting for duty");
    }

    public void doAction() throws GameActionException {
        // search for cheese
        RobotInfo[] nearbyInfos = rc.senseNearbyRobots();

        System.out.println("Sensed " + nearbyInfos.length + " robots");
        MapLocation enemyLoc = null;
        for (RobotInfo info : nearbyInfos) {
            if (info.getType().isCatType() || info.getType().isBabyRatType() || info.getType().isRatKingType() ) {
                Direction toEnemy = rc.getLocation().directionTo(enemyLoc);
                enemyLoc = info.getLocation();                
                if (rc.canTurn(toEnemy)) {
                    rc.turn(toEnemy);
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

        if ((catLoc != null) && rc.canAttack(enemyLoc)) {
            rc.attack(enemyLoc);
            rc.setIndicatorString("Victory or death!");
        }

    }

    
}
