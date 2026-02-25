package teacher07a;

import battlecode.common.*;

public class RatKing extends RobotSubPlayer {

    int numRats;
    final static int SPAWN_COOLDOWN_IN_TURNS = 5;
    int turnsUntilSpawn = 0;

    public RatKing(RobotController rc) {
        super(rc);
        numRats = 0;
        turnsUntilSpawn = 0;
    }

    @Override
    public void doAction() throws GameActionException {
        int currentCost = rc.getCurrentRatCost();

        MapLocation[] potentialSpawnLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);
        boolean spawn = (currentCost <= 10 || rc.getAllCheese() > currentCost + 500) && (turnsUntilSpawn == 0);

        for (MapLocation loc : potentialSpawnLocations) {
            if (spawn && rc.canBuildRat(loc)) {
                turnsUntilSpawn = SPAWN_COOLDOWN_IN_TURNS;
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

        turnsUntilSpawn -= 1;
        // moveRandom(rc);

    }
    
    

}
