package shredders;

import battlecode.common.*;

public class RatKing extends RobotSubPlayer {

    // ===== Shared Array Contract =====
    // 0 is often used by your BabyRat role toggle; keep it stable.
    private static final int SA_NUM_SPAWNED = 0;
    private static final int SA_KING_X = 1;
    private static final int SA_KING_Y = 2;

    // ===== Knobs (tune these, don’t rewrite) =====

    // Banking window: hold cheese so promotion can happen
    private static final int BANK_START = 45;
    private static final int BANK_STOP  = 60;

    // Spawn brakes (prevents runaway baby spam)
    private static final int MAX_SPAWNS_BEFORE_2K = 18; // lifetime cap, BUT we allow recovery when cost is low
    private static final int LOCAL_BABY_CAP = 12;       // congestion cap near the king
    private static final int COST_CEILING = 40;         // stop spawning once cost is too high

    // Reserves: keeps kings alive late game
    private static final int RESERVE_1K = 60;
    private static final int RESERVE_2K = 90; // kept for future; not used without a king-count API

    // Endgame conservation: only freeze spawns if bank is low
    private static final int ENDGAME_ROUND = 900;
    private static final int ENDGAME_MIN_BANK_TO_SPAWN = 200;

    public RatKing(RobotController rc) {
        super(rc);
    }

    @Override
    public void doAction() throws GameActionException {
        final MapLocation here = rc.getLocation();
        final int global = rc.getAllCheese();
        final int cost = rc.getCurrentRatCost();

        // 1) Write king beacon (cheap; KingBuilder uses it)
        rc.writeSharedArray(SA_KING_X, here.x);
        rc.writeSharedArray(SA_KING_Y, here.y);

        // 2) HARD FAILSAFE: if king starts boxed by dirt, dig an exit FIRST.
        // We define "boxed" as: no adjacent tile we can spawn onto.
        if (!hasAnySpawnTile()) {
            if (unboxByDigging()) return;
        }

        // 3) Pickup any adjacent cheese if possible (tiny radius, low bytecode)
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

        // 5) Promo banking window: do NOT spawn while trying to sit at/above 50
        boolean bankPromo = (global >= BANK_START && global < BANK_STOP);

        // 6) Endgame freeze ONLY if bank is low (prevents the “no babies with 800 cheese” bug)
        boolean endgameFreeze = (rc.getRoundNum() >= ENDGAME_ROUND && global < ENDGAME_MIN_BANK_TO_SPAWN);

        // 7) Lifetime spawn counter + recovery bypass
        int spawnedSoFar = rc.readSharedArray(SA_NUM_SPAWNED);

        // Reserve rule (lite): use RESERVE_1K without needing king-count API
        int reserve = RESERVE_1K;

        boolean spawnAllowed =
                !bankPromo &&
                !endgameFreeze &&
                // lifetime cap, but allow repopulation when swarm is small again (cost drops)
                (spawnedSoFar < MAX_SPAWNS_BEFORE_2K || cost <= 20) &&
                localBabies < LOCAL_BABY_CAP &&
                cost <= COST_CEILING &&
                global >= (cost + reserve);

        // 8) Spawn if allowed
        if (spawnAllowed) {
            for (MapLocation loc : close) {
                if (rc.canBuildRat(loc)) {
                    rc.buildRat(loc);
                    rc.writeSharedArray(SA_NUM_SPAWNED, spawnedSoFar + 1);
                    rc.setIndicatorString("KING spawn#" + (spawnedSoFar + 1)
                            + " g=" + global + " cost=" + cost + " local=" + localBabies);
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
        );
    }

    // ===== Boxed / Unbox Helpers =====

    // "Spawn tile" = empty adjacent location that can accept a baby rat.
    private boolean hasAnySpawnTile() throws GameActionException {
        MapLocation here = rc.getLocation();
        for (Direction dir : Direction.values()) {
            if (dir == Direction.CENTER) continue;
            MapLocation adj = here.add(dir);
            if (rc.canBuildRat(adj)) return true;
        }
        return false;
    }

    // Dig an exit if boxed. Prefer digging toward map center to find open space faster.
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

        // Fallback: dig any adjacent dirt
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