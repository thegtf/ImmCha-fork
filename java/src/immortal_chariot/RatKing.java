package immortal_chariot;

import battlecode.common.*;

public class RatKing extends RobotSubPlayer {

    int numRats;

    public RatKing(RobotController rc) {
        super(rc);
        numRats = 0;
    }

    @Override
    public void doAction() throws GameActionException {
        int currentCost = rc.getCurrentRatCost();

        MapLocation[] potentialSpawnLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);
        boolean spawn = currentCost <= 10 || rc.getAllCheese() > currentCost + 2500;

        for (MapLocation loc : potentialSpawnLocations) {
            if (spawn && rc.canBuildRat(loc)) {
                rc.buildRat(loc);
                numRats += 1;
                rc.writeSharedArray(0, numRats);
                break;
            }

            if (rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
                break;
            }
        }

        // moveRandom(rc);

    }
    
    

}
