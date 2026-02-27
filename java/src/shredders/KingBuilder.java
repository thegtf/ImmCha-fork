package shredders;

import battlecode.common.*;

public class KingBuilder extends BabyRat {

    // ===== Comms-Lite: shared array slots (MUST match your current Shredders layout) =====
    // [0] used elsewhere (spawned count / role toggle)
    // [1],[2] king beacon written by RatKing
    private static final int SA_KING_X = 1;
    private static final int SA_KING_Y = 2;

    // [3] SAFE per you: use as flags bitfield
    private static final int SA_FLAGS = 3;

    // Flag bits
    private static final int FLAG_SECOND_KING_BUILT = 1 << 0;

    // ===== Promotion / Rally Knobs =====
    private static final int PROMO_COST = 50;

    // Start drifting back toward king before PROMO_COST so we can form the 7-pack
    private static final int RALLY_START = 45;

    // Builders should NOT all pile onto the same exact squares; use a slightly larger “near king” zone
    private static final int RALLY_RADIUS2 = 20; // ~4-5 tiles

    // When we're very close, hold to help satisfy 3x3 packing
    private static final int HOLD_RADIUS2 = 5;   // inside-ish of 3x3 region

    // Reserve to avoid draining kings to death
    private static final int RESERVE = 60;

    // Long-game option: re-enable promotion attempts harder late (optional)
    private static final int LONG_GAME_ROUND = 1300;

    // Simple target memory for cheese pickup (optional light)
    private MapLocation lastSeenCheese = null;
    private int lastSeenCheeseRound = -9999;
    private static final int CHEESE_MEMORY_ROUNDS = 20;

    public KingBuilder(RobotController rc) {
        super(rc);
    }

    @Override
    public void doAction() throws GameActionException {

        // If we're already at 2+ kings, builders should stop camping—become normal econ/fight behavior.
        // We can't count kings without heavier comms, so we use a simple global flag: second king built at least once.
        if (secondKingBuilt()) {
            // Behave like a more aggressive CheeseFinder-lite:
            econRoamAndDeliver();
            return;
        }

        MapLocation king = readKingBeacon();
        int global = rc.getAllCheese();

        // 0) If we can become a king RIGHT NOW, do it first.
        // (This is the only place we flip the shared flag)
        if (king != null && canPromoteNow(global)) {
            if (rc.canBecomeRatKing()) {
                rc.becomeRatKing();
                setSecondKingBuilt();
                rc.setIndicatorString("KINGBUILDER PROMOTED");
                return;
            }
        }

        // 1) If we're carrying cheese, deliver it (always).
        if (rc.getRawCheese() > 0) {
            deliverToKingIfVisible(king);
            moveToward(king); // continue moving toward king to deliver
            rc.setIndicatorString("KINGBUILDER deliver");
            return;
        }

        // 2) RALLY behavior when global cheese is near promotion.
        // Goal: get enough bodies near the king without freezing the whole swarm.
        if (king != null && shouldRally(global)) {

            int d2 = rc.getLocation().distanceSquaredTo(king);

            // If very close, hold/jitter to help satisfy 7-in-3x3 without hard-locking
            if (d2 <= HOLD_RADIUS2 && global >= PROMO_COST) {
                // Try to stay put (cheap). If blocked by dirt in front, dig it.
                MapLocation forward = rc.adjacentLocation(rc.getDirection());
                if (rc.canRemoveDirt(forward)) rc.removeDirt(forward);
                rc.setIndicatorString("KINGBUILDER HOLD g=" + global);
                return;
            }

            // Otherwise, drift toward king, but don't all stack on one line:
            // use tiny deterministic sidestep when close
            if (d2 <= RALLY_RADIUS2) {
                // sidestep/jitter to spread in the king zone
                jitter();
                rc.setIndicatorString("KINGBUILDER spread");
                return;
            }

            // Farther away: go toward king
            moveToward(king);
            rc.setIndicatorString("KINGBUILDER rally");
            return;
        }

        // 3) Otherwise: do econ work—find cheese, pick it up, then deliver.
        econRoamAndDeliver();
    }

    // ===== Core behaviors =====

    private void econRoamAndDeliver() throws GameActionException {
        // Try immediate adjacent attack (opportunistic)
        for (Direction dir : directions) {
            MapLocation adj = rc.getLocation().add(dir);
            if (rc.canAttack(adj)) {
                rc.attack(adj);
                return;
            }
        }

        // Sense cheese nearby and bias toward it
        MapInfo[] infos = rc.senseNearbyMapInfos();
        MapLocation bestCheese = null;
        int bestAmt = 0;

        for (MapInfo mi : infos) {
            if (mi.getCheeseAmount() > bestAmt) {
                bestAmt = mi.getCheeseAmount();
                bestCheese = mi.getMapLocation();
            }
        }

        if (bestCheese != null) {
            lastSeenCheese = bestCheese;
            lastSeenCheeseRound = rc.getRoundNum();
            Direction to = rc.getLocation().directionTo(bestCheese);
            tryTurnMoveOrDig(to);
            // pick up if possible (on adjacent tile)
            if (rc.canPickUpCheese(bestCheese)) rc.pickUpCheese(bestCheese);
            return;
        }

        // If we recently saw cheese, keep moving toward last location for a bit
        if (lastSeenCheese != null && rc.getRoundNum() - lastSeenCheeseRound <= CHEESE_MEMORY_ROUNDS) {
            Direction to = rc.getLocation().directionTo(lastSeenCheese);
            tryTurnMoveOrDig(to);
            return;
        }

        // Otherwise roam (center-biased, cheap)
        MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
        Direction toCenter = rc.getLocation().directionTo(center);
        tryTurnMoveOrDig(toCenter);
        jitter();
    }

    private void deliverToKingIfVisible(MapLocation king) throws GameActionException {
        if (king == null) return;

        // Only transfer if the actual king robot is sensed nearby
        RobotInfo[] allies = rc.senseNearbyRobots(king, 8, rc.getTeam());
        int raw = rc.getRawCheese();
        for (RobotInfo r : allies) {
            if (r.getType().isRatKingType()) {
                MapLocation kLoc = r.getLocation();
                if (rc.canTransferCheese(kLoc, raw)) {
                    rc.transferCheese(kLoc, raw);
                }
                return;
            }
        }
    }

    private void moveToward(MapLocation target) throws GameActionException {
        if (target == null) {
            jitter();
            return;
        }
        Direction dir = rc.getLocation().directionTo(target);
        tryTurnMoveOrDig(dir);
    }

    // Turn toward dir if possible, then either dig forward (if blocked by dirt) or move forward.
    private void tryTurnMoveOrDig(Direction dir) throws GameActionException {
        if (rc.canTurn(dir)) rc.turn(dir);

        MapLocation forward = rc.adjacentLocation(rc.getDirection());
        if (!rc.canMoveForward() && rc.canRemoveDirt(forward)) {
            rc.removeDirt(forward);
            return;
        }
        if (rc.canMoveForward()) {
            rc.moveForward();
        }
    }

    private void jitter() throws GameActionException {
        Direction dir = directions[(rc.getID() + rc.getRoundNum()) % directions.length];
        if (rc.canTurn(dir)) rc.turn(dir);
        if (rc.canMoveForward()) rc.moveForward();
        else {
            MapLocation forward = rc.adjacentLocation(rc.getDirection());
            if (rc.canRemoveDirt(forward)) rc.removeDirt(forward);
        }
    }

    // ===== Comms-Lite helpers =====

    private MapLocation readKingBeacon() throws GameActionException {
        int x = rc.readSharedArray(SA_KING_X);
        int y = rc.readSharedArray(SA_KING_Y);
        if (x == 0 && y == 0) return null;
        return new MapLocation(x, y);
    }

    private boolean secondKingBuilt() throws GameActionException {
        return (rc.readSharedArray(SA_FLAGS) & FLAG_SECOND_KING_BUILT) != 0;
    }

    private void setSecondKingBuilt() throws GameActionException {
        int flags = rc.readSharedArray(SA_FLAGS);
        rc.writeSharedArray(SA_FLAGS, flags | FLAG_SECOND_KING_BUILT);
    }

    // ===== Decision helpers =====

    private boolean shouldRally(int global) {
        // Only rally when we're close to promo OR in long-game (tuneable)
        if (global >= RALLY_START) return true;
        return rc.getRoundNum() >= LONG_GAME_ROUND && global >= (PROMO_COST + 10);
    }

    private boolean canPromoteNow(int global) {
        // Simple safe gate: do not spend the last cheese and kill the kings.
        return global >= (PROMO_COST + RESERVE) && rc.isActionReady();
    }
}