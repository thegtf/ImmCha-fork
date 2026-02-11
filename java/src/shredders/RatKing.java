package shredders;

import battlecode.common.*;

public class RatKing extends RobotSubPlayer{
    static MapLocation KingTargetMine = null;
        public RatKing(RobotController rc) {
        super(rc);
        //TODO Auto-generated constructor stub
    }

 @Override
    public void doAction() throws GameActionException {
        int currentCost = rc.getCurrentRatCost();

        MapLocation[] potentialSpawnLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);
        boolean spawn = currentCost <= 10 || rc.getAllCheese() > currentCost + 500;

        for (MapLocation loc : potentialSpawnLocations) {
            if (spawn && rc.canBuildRat(loc)) {
                rc.buildRat(loc);
                int numRats = 1;
                rc.writeSharedArray(0, numRats);
                break;
            }

            if (rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
                break;
            }
        }

    RatKing.moveRandom(rc);
    rc.setIndicatorString("repositioning Rat King");

    }
    
    

}
