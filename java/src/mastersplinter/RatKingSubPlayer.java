package mastersplinter;

import battlecode.common.*;

public class RatKingSubPlayer extends RobotSubPlayer {

    // King camping target (mine)
    private MapLocation targetMine = null;

    // Throttle expensive work
    private int lastMineRetargetRound = -9999;

    // Tuning knobs (your “happy medium” controls)
    private static final int MIN_GLOBAL_RESERVE_ONE_KING = 40;   // keep at least this much so you don’t starve early
    private static final int MIN_GLOBAL_RESERVE_TWO_KINGS = 80;  // higher reserve once you have 2 kings
    private static final int SPAWN_COST_BUFFER = 40;             // extra buffer beyond spawn cost
    private static final int RETARGET_PERIOD = 25;               // only retarget mine occasionally

    private static final int PROMO_RALLY_CHEESE = 0;

    public RatKingSubPlayer(RobotController rc) {
        super(rc);
    }

    @Override
    public void step() throws GameActionException {
        Comms.writeKingBeacon(rc);

int cheese = rc.getAllCheese();
int cost = rc.getCurrentRatCost();

// Reserve so you don't starve.
// With 1 king, keep 20. With 2 kings, keep 40. (Simple.)
int kings = Comms.readKingCount(rc);
int reserve = 20 * kings;

// 1) Try to spawn if affordable
if (cheese >= cost + reserve) {
    // Try all ring tiles at Chebyshev distance 2 around the king center
    MapLocation c = rc.getLocation();
    for (int dx = -2; dx <= 2; dx++) {
        for (int dy = -2; dy <= 2; dy++) {
            if (Math.max(Math.abs(dx), Math.abs(dy)) != 2) continue;
            MapLocation loc = new MapLocation(c.x + dx, c.y + dy);
            if (rc.onTheMap(loc) && rc.canBuildRat(loc)) {
                rc.buildRat(loc);
                rc.setIndicatorString("KING spawn cost=" + cost + " cheese=" + cheese);
                return;
            }
        }
    }
}

// 2) If no spawn spot exists, dig ONE ring tile (to open space)
MapLocation c = rc.getLocation();
for (int dx = -2; dx <= 2; dx++) {
    for (int dy = -2; dy <= 2; dy++) {
        if (Math.max(Math.abs(dx), Math.abs(dy)) != 2) continue;
        MapLocation loc = new MapLocation(c.x + dx, c.y + dy);
        if (rc.onTheMap(loc) && rc.canRemoveDirt(loc)) {
            rc.removeDirt(loc);
            rc.setIndicatorString("KING dig spawn ring");
            return;
        }
    }
}

// 3) Otherwise hold position (no wandering)
rc.setIndicatorString("KING hold");

        // 3) Emergency cat response (cheap + simple)
        // Cats are Team.NEUTRAL per your earlier code usage.
        RobotInfo[] cats = rc.senseNearbyRobots(rc.getType().getVisionRadiusSquared(), Team.NEUTRAL);
        if (cats.length > 0) {
            MapLocation catLoc = cats[0].getLocation();
            int d2 = rc.getLocation().distanceSquaredTo(catLoc);

            // If cat is close, run away (and dig if needed).
            if (d2 <= 16) {
                Direction away = catLoc.directionTo(rc.getLocation());
                if (rc.canTurn(away)) rc.turn(away);

                MapLocation next = rc.getLocation().add(away);
                if (!rc.canMove(away) && rc.canRemoveDirt(next)) {
                    rc.removeDirt(next);
                } else if (rc.canMove(away)) {
                    rc.move(away);
                }

                // Also bite if adjacent (sometimes you can finish a cat / weaken)
                if (d2 <= 2 && rc.canAttack(catLoc)) {
                    rc.attack(catLoc);
                }

                rc.setIndicatorString("KING: CAT CLOSE -> evade");
                // Still try spawning after evasion (fall through)
            }
        }

        // 4) Spawn control: don’t over-spawn, don’t starve, don’t blow bytecode
        trySpawnOrClearRing();

        // 5) Mine camping / movement (minimal wandering)
        chooseMineTargetIfNeeded();

        if (targetMine != null) {
            rc.setIndicatorString("KING->MINE " + targetMine);
            // Kings have long movement cooldown. Only attempt move if movement-ready;
            // otherwise prefer digging forward if blocked (helps escape cages).
            if (rc.isMovementReady()) {
                moveTowardWithDig(targetMine);
            } else {
                // If not movement-ready, still try to dig a forward tile if boxed in.
                MapLocation forward = rc.adjacentLocation(rc.getDirection());
                if (rc.canRemoveDirt(forward)) {
                    rc.removeDirt(forward);
                }
            }
        } else {
            rc.setIndicatorString("KING holding (no mine yet)");
            // Don’t wander every turn; just dig forward if boxed.
            MapLocation forward = rc.adjacentLocation(rc.getDirection());
            if (rc.canRemoveDirt(forward)) rc.removeDirt(forward);
        }
    }

    private void moveTowardWithDig(MapLocation targetMine2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'moveTowardWithDig'");
    }

    /** Spawning must fail gracefully when boxed. If boxed, dig a spawn ring. */
    private void trySpawnOrClearRing() throws GameActionException {
        int global = rc.getGlobalCheese();
        int cost = rc.getCurrentRatCost();

        boolean hasSecondKing = secondKingBuilt();

        int reserve = hasSecondKing ? MIN_GLOBAL_RESERVE_TWO_KINGS : MIN_GLOBAL_RESERVE_ONE_KING;

        // Banking phase to hit 50 for promotion (optional)
        // If you’re between 45..49 and still only 1 king, stop spawning to reach 50.
        if (!hasSecondKing && global >= PROMO_ASSIST_CHEESE && global < PROMO_RALLY_CHEESE) {
            rc.setIndicatorString("KING BANKING to 50 (no spawn)");
            // While banking, still clear ring so we can spawn immediately after 50 if needed
            clearSpawnRingIfNeeded();
            return;
        }

        // Decide whether we can afford a spawn *without* draining the bank
        boolean canAfford = (global - cost) >= (reserve + SPAWN_COST_BUFFER);

        if (!canAfford) {
            // If we can’t afford spawning, at least keep the spawn ring open.
            clearSpawnRingIfNeeded();
            return;
        }

        // Attempt spawn on the ring (Chebyshev distance 2 from king center).
        // This is the “adjacent to the 3x3 body” ring.
        MapLocation c = rc.getLocation();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                int cheb = Math.max(Math.abs(dx), Math.abs(dy));
                if (cheb != 2) continue; // ring only
                MapLocation loc = new MapLocation(c.x + dx, c.y + dy);
                if (!rc.onTheMap(loc)) continue;

                if (rc.canBuildRat(loc)) {
                    rc.buildRat(loc);
                    rc.setIndicatorString("KING spawned (cost=" + cost + ", global=" + global + ")");
                    return;
                }
            }
        }

        // If we got here, we *wanted* to spawn but couldn’t → we’re boxed.
        clearSpawnRingIfNeeded();
    }

    private boolean secondKingBuilt() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'secondKingBuilt'");
    }

    /** Remove dirt on the spawn ring to open at least one buildable tile. */
    private void clearSpawnRingIfNeeded() throws GameActionException {
        MapLocation c = rc.getLocation();

        // First pass: dig any ring dirt you can.
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                int cheb = Math.max(Math.abs(dx), Math.abs(dy));
                if (cheb != 2) continue;
                MapLocation loc = new MapLocation(c.x + dx, c.y + dy);
                if (!rc.onTheMap(loc)) continue;

                if (rc.canRemoveDirt(loc)) {
                    rc.removeDirt(loc);
                    rc.setIndicatorString("KING digging spawn ring");
                    return; // only one action per turn
                }
            }
        }

        // Second pass: if no ring dirt to dig, try digging forward to escape cages/traps.
        MapLocation forward = rc.adjacentLocation(rc.getDirection());
        if (rc.canRemoveDirt(forward)) {
            rc.removeDirt(forward);
            rc.setIndicatorString("KING digging forward (escape)");
        }
    }

    /** Keep mine target stable; don’t retarget constantly. */
    private void chooseMineTargetIfNeeded() throws GameActionException {
        int r = rc.getRoundNum();

        // If we can directly see a mine, lock it quickly (cheap)
        MapInfo[] infos = rc.senseNearbyMapInfos();
        for (MapInfo info : infos) {
            if (info.hasCheeseMine()) {
                targetMine = info.getMapLocation();
                lastMineRetargetRound = r;
                return;
            }
        }

        // Periodic retarget from shared array
        if (targetMine == null || r - lastMineRetargetRound >= RETARGET_PERIOD) {
            targetMine = Comms.nearestKnownMine(rc, rc.getLocation());
            lastMineRetargetRound = r;
        }
    }
}