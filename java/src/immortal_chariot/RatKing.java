package immortal_chariot;

import battlecode.common.*;

public class RatKing extends RobotSubPlayer {

    int ratCount;

    public RatKing(RobotController rc) {
        super(rc);
        ratCount = 0;
    }

    @Override
    public void doAction() throws GameActionException {
        int currentCost = rc.getCurrentRatCost();

        MapLocation[] potentialSpawnLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);
        boolean spawn = (currentCost <= 20 || rc.getAllCheese() > currentCost + 2500) && rc.getAllCheese() >= 100; 
        // spawn rats if (we have less than 8 rats OR we have more than 2500 cheese) AND we have more than 100 cheese

        for (MapLocation loc : potentialSpawnLocations) {
            if (spawn && rc.canBuildRat(loc)) {
                rc.buildRat(loc);
                ratCount += 1;
                rc.writeSharedArray(0, ratCount);
                rc.setIndicatorString("Rat Count = "+ratCount);
            } else { if (ratCount==0 && rc.canBuildRat(loc)) { //if we are hungry, but we have no rats, we have to bite the bullet and spawn one anyway
                // this doesnt work because ratCount 
                    rc.buildRat(loc);
                    ratCount += 1;
                    rc.writeSharedArray(0, ratCount);
                    rc.setIndicatorString("Rat Count = "+ratCount);
                }
            }

            if (rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
                break;
            }
        }

        // moveRandom(rc);

    }
    
    public void selfDefense() throws GameActionException {

        if (rc.getHealth() < 500) {
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(1, rc.getTeam());
            for (RobotInfo robotInfo : nearbyRobots) {
                if (robotInfo.getTeam() == rc.getTeam().opponent()) {
                    MapLocation ankleBiterLoc = robotInfo.getLocation();
                    if (rc.canAttack(ankleBiterLoc)) {
                        rc.attack(ankleBiterLoc);
                    }
                }
            }
        }
    }

}