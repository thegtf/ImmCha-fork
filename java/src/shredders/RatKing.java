package shredders;

import battlecode.common.*;

public class RatKing extends RobotSubPlayer {

    // ===== Shared Array Contract =====
    // 0 is often used by your BabyRat role toggle; keep it stable.
    private static final int SA_NUM_SPAWNED = 0;
    private static final int SA_KING_X = 1;
    private static final int SA_KING_Y = 2;

    // ===== Knobs (tune these, don’t rewrite) =====

    // Banking window: hold cheese so promotion can happen (when KingBuilder is enabled)
    private static final int BANK_START = 50;
    private static final int BANK_STOP  = 60;

    // Spawn brakes (prevents runaway baby spam)
    private static final int MAX_SPAWNS_BEFORE_2K = 30; // lifetime cap, BUT we allow recovery when cost is low
    private static final int LOCAL_BABY_CAP = 20;       // congestion cap near the king
    private static final int COST_CEILING = 60;         // stop spawning once cost is too high

    // Reserves: keeps kings alive late game
    private static final int RESERVE_1K = 40;

    // Endgame conservation: only freeze spawns if bank is low
    private static final int ENDGAME_ROUND = 900;
    private static final int ENDGAME_MIN_BANK_TO_SPAWN = 200;

    // ===== LONG GAME SWITCH =====
    // After this, we loosen spawn gates to avoid "no babies with 800+ cheese"
    private static final int LONG_GAME_ROUND = 1300;

    // Long-game spawn profile (tune these)
    private static final int LG_LOCAL_BABY_CAP = 25;
    private static final int LG_COST_CEILING   = 70;
    private static final int LG_RESERVE_1K     = 250;  // keep enough bank so king(s) don't bleed HP
    private static final int LG_MIN_BANK_TO_SPAWN = 300; // if we have plenty of cheese, keep fielding units

    public RatKing(RobotController rc) {
        super(rc);
    }

    @Override
    public void doAction() throws GameActionException {
        final MapLocation here = rc.getLocation();
        final int global = rc.getAllCheese();
        final int cost = rc.getCurrentRatCost();
        final int round = rc.getRoundNum();

        // 1) Write king beacon (cheap)
        rc.writeSharedArray(SA_KING_X, here.x);
        rc.writeSharedArray(SA_KING_Y, here.y);

        // 2) HARD FAILSAFE: if king starts boxed by dirt, dig an exit FIRST.
        if (!hasAnySpawnTile()) {
            if (unboxByDigging()) return;
        }

        // 3) Pickup any adjacent cheese
        MapLocation[] close = rc.getAllLocationsWithinRadiusSquared(here, 8);
        for (MapLocation loc : close) {
            if (rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
                rc.setIndicatorString("KING pickup g=" + global + " cost=" + cost);
                return;
            }
        }

        // 4) Count babies near the king (congestion guard)
        int localBabies = 0;
        RobotInfo[] allies = rc.senseNearbyRobots(18, rc.getTeam());
        for (RobotInfo r : allies) {
            if (r.getType().isBabyRatType()) localBabies++;
        }

        // 5) Promo banking window (only matters if KingBuilder is enabled)
        boolean bankPromo = (global >= BANK_START && global < BANK_STOP);

        // 6) Endgame freeze ONLY if bank is low
        boolean endgameFreeze = (round >= ENDGAME_ROUND && global < ENDGAME_MIN_BANK_TO_SPAWN);

        // 7) Lifetime spawn counter + recovery bypass
        int spawnedSoFar = rc.readSharedArray(SA_NUM_SPAWNED);

        // ===== Long game profile selection =====
        boolean longGame = (round >= LONG_GAME_ROUND);

        int localCap = longGame ? LG_LOCAL_BABY_CAP : LOCAL_BABY_CAP;
        int costCeil = longGame ? LG_COST_CEILING   : COST_CEILING;
        int reserve  = longGame ? LG_RESERVE_1K     : RESERVE_1K;

        // Long game: if bank is clearly healthy, ignore lifetime cap entirely.
        boolean ignoreLifetimeCap = longGame && global >= LG_MIN_BANK_TO_SPAWN;

        boolean spawnAllowed =
                // Never spawn during promo-banking window
                !bankPromo &&
                // Only freeze spawns if endgame bank is low
                !endgameFreeze &&
                // Prevent king being body-blocked
                localBabies < localCap &&
                // Don't spawn when cost has exploded (except long-game has higher ceiling)
                cost <= costCeil &&
                // Must keep reserve so kings don't bleed out
                global >= (cost + reserve) &&
                // Lifetime cap or bypass if repop / long-game bank is strong
                (ignoreLifetimeCap || spawnedSoFar < MAX_SPAWNS_BEFORE_2K || cost <= 20);

        // 8) Spawn if allowed
        if (spawnAllowed) {
            for (MapLocation loc : close) {
                if (rc.canBuildRat(loc)) {
                    rc.buildRat(loc);
                    rc.writeSharedArray(SA_NUM_SPAWNED, spawnedSoFar + 1);
                    rc.setIndicatorString("KING spawn#" + (spawnedSoFar + 1)
                            + " g=" + global + " cost=" + cost + " local=" + localBabies
                            + (longGame ? " LONG" : ""));
                    return;
                }
            }

            // If we wanted to spawn but couldn't (blocked), dig an adjacent tile to create space.
            if (digAnyAdjacent()) {
                rc.setIndicatorString("KING dig-to-spawn g=" + global + " cost=" + cost);
                return;
            }
        }

        // 9) Otherwise: hold (low bytecode)
        rc.setIndicatorString(
                (bankPromo ? "PROMO_LOCK " : "HOLD ")
                        + "g=" + global
                        + " cost=" + cost
                        + " local=" + localBabies
                        + " spawned=" + spawnedSoFar
                        + (endgameFreeze ? " ENDGAME_FREEZE" : "")
                        + (longGame ? " LONG" : "")
        );
    }

    // ===== Boxed / Unbox Helpers =====

    private boolean hasAnySpawnTile() throws GameActionException {
        MapLocation here = rc.getLocation();
        for (Direction dir : Direction.values()) {
            if (dir == Direction.CENTER) continue;
            MapLocation adj = here.add(dir);
            if (rc.canBuildRat(adj)) return true;
        }
        return false;
    }

    private boolean unboxByDigging() throws GameActionException {
        MapLocation here = rc.getLocation();
        MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
        Direction preferred = here.directionTo(center);

        Direction[] order = new Direction[] {
                preferred,
                preferred.rotateLeft(),
                preferred.rotateRight(),
                preferred.rotateLeft().rotateLeft(),
                preferred.rotateRight().rotateRight(),
                preferred.opposite()
        };

        for (Direction dir : order) {
            if (dir == Direction.CENTER) continue;
            MapLocation adj = here.add(dir);
            if (rc.canRemoveDirt(adj)) {
                rc.removeDirt(adj);
                rc.setIndicatorString("KING UNBOX dig " + dir);
                return true;
            }
        }
        return digAnyAdjacent();
    }

    private boolean digAnyAdjacent() throws GameActionException {
        MapLocation here = rc.getLocation();
        for (Direction dir : Direction.values()) {
            if (dir == Direction.CENTER) continue;
            MapLocation adj = here.add(dir);
            if (rc.canRemoveDirt(adj)) {
                rc.removeDirt(adj);
                return true;
            }
        }
        return false;
    }
}