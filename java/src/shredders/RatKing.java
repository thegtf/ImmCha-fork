package shredders;

import battlecode.common.*;

public class RatKing extends RobotSubPlayer {

    public final PathFinding pf = new PathFinding();

    // Shared array slots (used ONLY here + KingBuilder)
    private static final int SA_NUM_SPAWNED = 0;
    private static final int SA_KING_X = 1;
    private static final int SA_KING_Y = 2;

    // ===== TUNING KNOBS =====
    private static final int LOCAL_BABY_CAP = 14;   // fewer babies near the king
    private static final int COST_CEILING = 100;     // stop spawning once cost rises
    private static final int RESERVE_CHEESE = 30;   // keep this much in bank

    // Promotion lock window: do NOT spawn here so we don't dip under 50
    private static final int PROMO_LOCK_START = 45;
    private static final int PROMO_LOCK_END   = 70; // keep bank steady until 2nd king forms

    public RatKing(RobotController rc) {
        super(rc);
    }

    @Override
    public void doAction() throws GameActionException {
        MapLocation here = rc.getLocation();

        // 1) Publish beacon every turn
        rc.writeSharedArray(SA_KING_X, here.x);
        rc.writeSharedArray(SA_KING_Y, here.y);

        // 2) If boxed, dig adjacent
        if (isBoxed()) {
            digAnyAdjacent();
            rc.setIndicatorString("KING: boxed -> dig");
            return;
        }

        // 3) Local congestion count
        int localBabies = 0;
        RobotInfo[] allies = rc.senseNearbyRobots(18, rc.getTeam());
        for (RobotInfo r : allies) {
            if (r.getType().isBabyRatType()) localBabies++;
        }

        int global = rc.getAllCheese();
        int cost = rc.getCurrentRatCost();

        // 4) Promotion lock: if we haven't made 2nd king yet, bank cheese
        // If you have a "second king built" flag elsewhere, ignore this; we keep it self-contained:
        // we treat "global cheese in [45..70)" as "do not spawn" time.
        boolean promoLock = (global >= PROMO_LOCK_START && global < PROMO_LOCK_END);

        // 5) Spawn gating
        boolean spawnAllowed =
                !promoLock
                && (localBabies < LOCAL_BABY_CAP)
                && (cost <= COST_CEILING)
                && (global >= cost + RESERVE_CHEESE);

        if (spawnAllowed) {
            MapLocation[] spots = rc.getAllLocationsWithinRadiusSquared(here, 8);
            for (MapLocation loc : spots) {
                if (rc.canBuildRat(loc)) {
                    rc.buildRat(loc);
                    int n = rc.readSharedArray(SA_NUM_SPAWNED);
                    rc.writeSharedArray(SA_NUM_SPAWNED, n + 1);
                    rc.setIndicatorString("KING: spawn cost=" + cost + " local=" + localBabies + " g=" + global);
                    return;
                }
            }
        }

        // 6) Grab adjacent cheese if available
        MapLocation[] nearby = rc.getAllLocationsWithinRadiusSquared(here, 8);
        for (MapLocation loc : nearby) {
            if (rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
                rc.setIndicatorString("KING: pickup");
                return;
            }
        }

        rc.setIndicatorString(promoLock
                ? "KING: PROMO LOCK banking g=" + global + " cost=" + cost
                : "KING: hold g=" + global + " cost=" + cost + " local=" + localBabies);
    }

    private boolean isBoxed() throws GameActionException {
        for (Direction dir : directions) {
            if (rc.canMove(dir)) return false;
        }
        return true;
    }

    private void digAnyAdjacent() throws GameActionException {
        MapLocation here = rc.getLocation();
        for (Direction dir : directions) {
            MapLocation adj = here.add(dir);
            if (rc.canRemoveDirt(adj)) {
                rc.removeDirt(adj);
                return;
            }
        }
    }
}