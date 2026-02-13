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

        MapLocation enemyLoc = null;
        for (RobotInfo info : nearbyInfos) {
            if (info.getTeam() != rc.getTeam() && (info.getType().isCatType() || info.getType().isRatKingType())) {
                enemyLoc = info.getLocation();  
                Direction toEnemy = rc.getLocation().directionTo(enemyLoc);              
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
            Direction left = rc.getDirection().rotateLeft();
            Direction right = rc.getDirection().rotateRight();
            if (rc.canMove(right)) {
                rc.move(right);
            } else if (rc.canMove(left)) {
                rc.move(left);
            } else {
                d = directions[rand.nextInt(directions.length - 1)];
                if (rc.canTurn(d)) 
                    rc. turn(d);
            }
        }

        if ((enemyLoc != null) && rc.canAttack(enemyLoc)) {
            rc.attack(enemyLoc);
            rc.setIndicatorString("Victory or death!");
        }

    }

    
}
