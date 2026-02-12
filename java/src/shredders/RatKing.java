package shredders;

import battlecode.common.*;

public class RatKing extends RobotSubPlayer {

    // ===== Shared Array Contract =====
    // NOTE: SA_NUM_SPAWNED is commonly used by teams for role selection.
    // KingBuilder can read SA_KING_X/SA_KING_Y as the king beacon.
    private static final int SA_NUM_SPAWNED = 0;
    private static final int SA_KING_X = 1;
    private static final int SA_KING_Y = 2;

    // ===== Knobs (adjust these instead of rewriting logic) =====

    // Banking window: when global cheese is in this range, STOP spawning to hold 50+ for promotion.
    private static final int BANK_START = 45;
    private static final int BANK_STOP  = 60;

    // Hard brakes on rat production
    private static final int MAX_SPAWNS_BEFORE_2K = 18; // tune down to reduce babies (e.g., 14–20)
    private static final int LOCAL_BABY_CAP = 12;       // max babies near the king before we stop spawning
    private static final int COST_CEILING = 40;         // stop spawning once spawn cost gets too high

    // Reserves (you already have these names—keeping them)
    private static final int RESERVE_1K = 60;           // reserve cheese to reduce endgame bleed
    private static final int RESERVE_2K = 90;           // kept for later, not used without king-count API

    // Endgame conservation: after this round, do not spawn unless very rich.
    private static final int ENDGAME_ROUND = 900;
    private static final int ENDGAME_MIN_BANK_TO_SPAWN = 200;

    public RatKing(RobotController rc) {
        super(rc);
    }

    @Override
    public void doAction() throws GameActionException {
        MapLocation here = rc.getLocation();

        // 1) Write king beacon every turn (cheap, helps builders)
        rc.writeSharedArray(SA_KING_X, here.x);
        rc.writeSharedArray(SA_KING_Y, here.y);

        int global = rc.getAllCheese();
        int cost = rc.getCurrentRatCost();

        // 2) If boxed in, dig a hole (don’t wander; just unbox)
        if (isBoxed()) {
            digAnyAdjacent();
            rc.setIndicatorString("KING boxed->dig g=" + global + " cost=" + cost);
            return;
        }

        // 3) Pick up adjacent cheese if available (free value, minimal bytecode)
        // (This is intentionally small-radius to stay cheap.)
        MapLocation[] close = rc.getAllLocationsWithinRadiusSquared(here, 8);
        for (MapLocation loc : close) {
            if (rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
                rc.setIndicatorString("KING pickup g=" + global + " cost=" + cost);
                return;
            }
        }

        // 4) Count babies near the king (prevents local congestion / walling)
        int localBabies = 0;
        RobotInfo[] allies = rc.senseNearbyRobots(18, rc.getTeam());
        for (RobotInfo r : allies) {
            if (r.getType().isBabyRatType()) localBabies++;
        }

        // 5) Banking window: do NOT spawn while trying to hold >= 50
        boolean bankPromo = (global >= BANK_START && global < BANK_STOP);

        // 6) Endgame conservation: stop spawning late game unless very rich
        boolean endgameFreeze = (rc.getRoundNum() >= ENDGAME_ROUND);

        // 7) Hard spawn caps
        int spawnedSoFar = rc.readSharedArray(SA_NUM_SPAWNED);

        // Reserve rule (lite): use RESERVE_1K without trying to infer king count
        int reserve = RESERVE_1K;

        boolean spawnAllowed =
                !bankPromo &&
                !endgameFreeze &&
                spawnedSoFar < MAX_SPAWNS_BEFORE_2K &&
                localBabies < LOCAL_BABY_CAP &&
                cost <= COST_CEILING &&
                global >= (cost + reserve);

        // 8) Spawn if allowed (small loop, tight radius)
        if (spawnAllowed) {
            for (MapLocation loc : close) {
                if (rc.canBuildRat(loc)) {
                    rc.buildRat(loc);

                    // IMPORTANT: increment, do not reset
                    rc.writeSharedArray(SA_NUM_SPAWNED, spawnedSoFar + 1);

                    rc.setIndicatorString("KING spawn #" + (spawnedSoFar + 1)
                            + " g=" + global + " cost=" + cost + " local=" + localBabies);
                    return;
                }
            }
        }

        // 9) Otherwise: hold position (no wandering -> low bytecode)
        rc.setIndicatorString(
                (bankPromo ? "PROMO_LOCK " : "HOLD ")
                + "g=" + global
                + " cost=" + cost
                + " local=" + localBabies
                + " spawned=" + spawnedSoFar
                + (endgameFreeze ? " ENDGAME_FREEZE" : "")
        );
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