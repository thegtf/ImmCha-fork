package mastersplinter;

import battlecode.common.*;
import java.util.ArrayList;

public class ExplorerSubPlayer extends RobotSubPlayer {

    // Local cache per-robot (instance, not static)
    private MapLocation lastMineSeen = null;

    // Shared (global) mine bookkeeping stays in RobotPlayer (static), but we can also just squeak and let king store.
    public ExplorerSubPlayer(RobotController rc) {
        super(rc);
    }

    @Override
    public void step() throws GameActionException {
        // Pick up adjacent cheese if possible
        for (Direction dir : directions) {
            MapLocation loc = rc.getLocation().add(dir);
            if (rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
                rc.setIndicatorString("EXPLORER picked cheese");
                break;
            }
        }

        // Sense mines / cheese
        MapInfo[] infos = rc.senseNearbyMapInfos();
        MapLocation cheeseLoc = null;

        for (MapInfo info : infos) {
            if (info.hasCheeseMine()) {
                lastMineSeen = info.getMapLocation();
            }
            if (info.getCheeseAmount() > 0 && cheeseLoc == null) {
                cheeseLoc = info.getMapLocation();
            }
        }

        if (rc.getRawCheese() > 0) {
    // Try transfer first (most important)
    if (deliverToNearestVisibleKing()) {
        rc.setIndicatorString("EXPLORER delivered; back out");
        return;
    }
    // If no king in range, move toward beacon
    MapLocation beacon = readKingBeacon();
    if (beacon != null) Pathfinder.stepToward(rc, beacon);
    squeakMineIfAny();
    return;
}

        // If we see cheese, orient toward it
        if (cheeseLoc != null) {
            Direction to = rc.getLocation().directionTo(cheeseLoc);
            if (rc.canTurn(to)) rc.turn(to);
            if (rc.canMoveForward()) rc.moveForward();
            rc.setIndicatorString("EXPLORER -> cheese");
            squeakMineIfAny();
            return;
        }

        // Otherwise wander
        moveWanderWithDig();
        rc.setIndicatorString("EXPLORER wandering");
        squeakMineIfAny();
    }

    private void squeakMineIfAny() throws GameActionException {
    if (lastMineSeen == null) return;
    Comms.squeakMine(rc, lastMineSeen);
    lastMineSeen = null;
}
}