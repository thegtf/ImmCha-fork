package mastersplinter;

import battlecode.common.*;
import java.util.ArrayList;
import java.util.Random;

public class RobotPlayer {

    public static enum SqueakType {
        INVALID,
        ENEMY_RAT_KING,
        ENEMY_COUNT,
        CHEESE_MINE,
        CAT_FOUND,
    }

    public static SqueakType[] squeakTypes = SqueakType.values();
    public static Direction[] directions = Direction.values();
    public static MapLocation mineLoc = null;
    public static int numMines = 0;
    public static ArrayList<Integer> mineLocs = new ArrayList<>();
    static final Random rand = new Random(6147);
    static Direction d = null;

static MapLocation kingLoc = null;

    // RatKing: where we want to camp (prefer cheese mines)
    static MapLocation kingTargetMine = null;

    static boolean secondKingBuilt = false;

    // When team cheese is at/above this value, rally near the king to enable promotions
    public static final int PROMO_RALLY_CHEESE = 50;

    // Start rallying a bit early so units are nearby when we hit 50
    public static final int PROMO_ASSIST_CHEESE = 45;
    // Only assist rally if within this range of the king (prevents map-wide freeze)
    public static final int PROMO_ASSIST_RADIUS2 = 64; // ~8 tiles

    // A subset of non-builders help form the 7-pack near the king when we're close to promotion.
    public static boolean isPromoAssistant(RobotController rc) {
        // 1 out of 2 non-builders assist (tuneable)
        return (rc.getID() % 2) == 0;
    }

    // Only a small subset of babies should rally/hold for promotion.
    // This prevents the entire swarm from freezing around the king.
    public static boolean isKingBuilder(RobotController rc) {
        // 1 out of 5 babies becomes a builder (tuneable)
        return (rc.getID() % 5) == 0;
    }



    public static void run(RobotController rc) {

        while (true) {
        try {
            if (rc.getType().isRatKingType()) {
                runRatKing(rc);
            } else {
                // Read king beacon from shared array each turn (written by the Rat King)
                MapLocation beacon = kingLoc(rc);
                if (beacon != null) {
                    kingLoc = beacon;
                }

                // Try to promote this baby into a new king before doing anything else
                if (tryBuildKing(rc)) {
                    continue; // this robot is now a king; next loop will runRatKing()
                }
               
                
                // Per-robot decision (DO NOT use shared global state)
                int raw = rc.getRawCheese();

                // Once we have a second king, economy mode: ONLY return when carrying cheese.
                if (secondKingBuilt) {
                    if (raw > 0) {
                        runReturnToKing(rc);
                    } else {
                        runFindCheese(rc);
                    }
                    continue;
                }

                // Before second king: use rally/assist to create the 7-pack + 50-cheese condition.
                boolean builder = isKingBuilder(rc);
                boolean rallyBuilder = (builder && kingLoc != null && rc.getAllCheese() >= PROMO_RALLY_CHEESE);

                boolean assistRally = false;
                if (!builder && raw == 0 && kingLoc != null && rc.getAllCheese() >= PROMO_ASSIST_CHEESE && isPromoAssistant(rc)) {
                    int d2 = rc.getLocation().distanceSquaredTo(kingLoc);
                    assistRally = (d2 <= PROMO_ASSIST_RADIUS2);
                }

                if (raw > 0 || rallyBuilder || assistRally) {
                    runReturnToKing(rc);
                } else {
                    runFindCheese(rc);
                }
                }
            } catch (GameActionException e) {
                System.out.println("GameActionException in RobotPlayer:");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception in RobotPlayer:");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
            if (rc.getAllCheese() >= 45) {
    String role = null;
    System.out.println(
        "PROMO? r=" + rc.getRoundNum()
        + " id=" + rc.getID()
        + " role=" + role
        + " cheese=" + rc.getAllCheese()
        + " can=" + rc.canBecomeRatKing()
        + " action=" + rc.isActionReady()
       // + " d2King=" + (kingLoc(rc) == null ? -1 : rc.getLocation().distanceSquaredTo(kingLoc(rc)))
    );
}
        }
    }

    private static MapLocation kingLoc(RobotController rc) throws GameActionException {
        int kingX = rc.readSharedArray(0);
        int kingY = rc.readSharedArray(1);
        // If the king hasn't written yet, return null
        if (kingX == 0 && kingY == 0) return null;
        return new MapLocation(kingX, kingY);
    }

    // Read the i-th mine from shared array (written by the Rat King when it receives mine squeaks)
    private static MapLocation readMineFromShared(RobotController rc, int i) throws GameActionException {
        int x = rc.readSharedArray(2 * i + 2);
        int y = rc.readSharedArray(2 * i + 3);
        // Basic guard: if unwritten, treat as invalid
        if (x == 0 && y == 0) return null;
        return new MapLocation(x, y);
    }

    // Choose nearest known mine to the current location (from shared array list)
    private static MapLocation chooseNearestKnownMine(RobotController rc) throws GameActionException {
        MapLocation here = rc.getLocation();
        MapLocation best = null;
        int bestD2 = Integer.MAX_VALUE;

        int limit = Math.min(numMines, 20); // cap work to keep bytecode stable
        for (int i = 0; i < limit; i++) {
            MapLocation m = readMineFromShared(rc, i);
            if (m == null) continue;
            int d2 = here.distanceSquaredTo(m);
            if (d2 < bestD2) {
                bestD2 = d2;
                best = m;
            }
        }
        return best;
    }

    // Simple greedy movement with dig-out support (works for kings and babies)
    private static void moveTowardWithDig(RobotController rc, MapLocation target) throws GameActionException {
        if (target == null) return;
        Direction dir = rc.getLocation().directionTo(target);
        MapLocation next = rc.getLocation().add(dir);

        if (rc.canTurn(dir)) rc.turn(dir);

        // If blocked, try to dig the next tile
        if (!rc.canMove(dir) && rc.canRemoveDirt(next)) {
            rc.removeDirt(next);
            return;
        }

        if (rc.canMove(dir)) {
            rc.move(dir);
            return;
        }

        // Fallback: try forward
        if (rc.canMoveForward()) {
            rc.moveForward();
        }
    }

    public static void runRatKing(RobotController rc) throws GameActionException {
        int currentCost = rc.getCurrentRatCost();

        MapLocation[] potentialSpawnLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);
        boolean spawn = currentCost <= 10 || rc.getAllCheese() > currentCost + 1900;

        // BANKING: only do this before the second king exists (promotion phase).
        if (!secondKingBuilt) {
            int bank = rc.getAllCheese();
            if (bank >= PROMO_ASSIST_CHEESE && bank < PROMO_RALLY_CHEESE) {
                spawn = false;
                rc.setIndicatorString("KING BANKING to 50 | cheese=" + bank + " cost=" + currentCost);
            }
        }

        for (MapLocation loc : potentialSpawnLocations) {
            if (spawn && rc.canBuildRat(loc)) {
                rc.buildRat(loc);
                break;
            }

            if (rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
                break;
            }
        }

        Message[] squeaks = rc.readSqueaks(rc.getRoundNum());

        for (Message msg : squeaks) {
            int rawSqueak = msg.getBytes();

            if (getSqueakType(rawSqueak) != SqueakType.CHEESE_MINE) {
                continue;
            }

            int encodedLoc = getSqueakValue(rawSqueak);

            if (mineLocs.contains(encodedLoc)) {
                continue;
            }

            mineLocs.add(encodedLoc);
            int firstInt = getFirstInt(encodedLoc);
            int lastInt = getLastInt(encodedLoc);

            rc.writeSharedArray(2 * numMines + 2, firstInt);
            rc.writeSharedArray(2 * numMines + 3, lastInt);
            System.out.println("Writing to shared array: " + firstInt + ", " + lastInt);
            System.out.println("Cheese mine located at: " + getX(encodedLoc) + ", " + getY(encodedLoc));

            numMines++;
        }

        // If we can directly sense a mine, bias our target toward it.
        MapInfo[] kingInfos = rc.senseNearbyMapInfos();
        for (MapInfo info : kingInfos) {
            if (info.hasCheeseMine()) {
                kingTargetMine = info.getMapLocation();
                break;
            }
        }

        // Prefer to camp near cheese mines so deliveries are shorter and kings don't get boxed.
        // Refresh target occasionally or if we reached it.
        if (kingTargetMine == null || rc.getLocation().distanceSquaredTo(kingTargetMine) <= 2 || (rc.getRoundNum() % 20 == 0)) {
            kingTargetMine = chooseNearestKnownMine(rc);
        }

        if (kingTargetMine != null) {
            rc.setIndicatorString("KING->MINE " + kingTargetMine);
            moveTowardWithDig(rc, kingTargetMine);
        } else {
            // No mine known yet: explore a bit to find one / avoid being trapped.
            rc.setIndicatorString("KING wandering (no mine)");
            moveRandom(rc);
        }

        // TODO make more efficient and expand communication in the communication lecture
        rc.writeSharedArray(0, rc.getLocation().x);
        rc.writeSharedArray(1, rc.getLocation().y);
    }

    // Move in a straight line until we bump into something,
    // then turn to a new direction. If blocked by dirt, try to dig out.
    public static void moveRandom(RobotController rc) throws GameActionException {
        if (d == null) {
            d = directions[rand.nextInt(directions.length - 1)];
        }

        // Try to clear dirt in front if we're stuck (helps kings avoid being boxed in)
        MapLocation forward = rc.adjacentLocation(rc.getDirection());
        if (!rc.canMoveForward() && rc.canRemoveDirt(forward)) {
            rc.removeDirt(forward);
            return;
        }

        if (rc.canMoveForward()) {
            rc.moveForward();
            return;
        }

        // If still blocked, pick a new direction and turn.
        d = directions[rand.nextInt(directions.length - 1)];
        if (rc.canTurn()) {
            rc.turn(d);
        }
    }

    public static void runFindCheese(RobotController rc) throws GameActionException {
        // search for cheese
        MapInfo[] nearbyInfos = rc.senseNearbyMapInfos();

        MapLocation cheeseLoc = null;
        for (MapInfo info : nearbyInfos) {
            MapLocation loc = info.getMapLocation();
            if (info.getCheeseAmount() > 0) {
                Direction toCheese = rc.getLocation().directionTo(loc);

                if (rc.canTurn(toCheese)) {
                    rc.turn(toCheese);
                    cheeseLoc = info.getMapLocation();
                    break;
                }
            }
            if (info.hasCheeseMine()) {
                mineLoc = info.getMapLocation();
            }
        }

        if (rc.canMoveForward()) {
            rc.moveForward();
            rc.setIndicatorString("Finding cheese.");
        } else {
            d = directions[rand.nextInt(directions.length-1)];
            if (rc.canTurn()) {
                rc.turn(d);
            }
            rc.setIndicatorString("Blocked while finding cheese, turning " + d.toString());
            return;
        }

        if ((cheeseLoc != null) && rc.canPickUpCheese(cheeseLoc)) {
            rc.pickUpCheese(cheeseLoc);
            rc.setIndicatorString("Picked up cheese; will return");
        }
    }

    public static void runReturnToKing(RobotController rc) throws GameActionException {
        if (kingLoc == null) {
            // No known king location yet; caller will keep exploring
            rc.setIndicatorString("No king beacon yet");
            return;
        }
        Direction toKing = rc.getLocation().directionTo(kingLoc);
        MapLocation nextLoc = rc.getLocation().add(toKing);
        int rawCheese = rc.getRawCheese();

        // Economy mode: once second king exists, never linger near kings while empty.
        if (secondKingBuilt && rawCheese == 0) {
            rc.setIndicatorString("2K econ: empty -> explore");
            return;
        }
        // UNSTICK: if we're empty near the king but can't promote, drift away to reshuffle.
        // Prevents permanent camping that blocks the dynamics needed for a 7-pack in 3x3.
        if (rawCheese == 0 && rc.getAllCheese() >= PROMO_RALLY_CHEESE) {
            int d2 = rc.getLocation().distanceSquaredTo(kingLoc);
            if (d2 <= 8 && !rc.canBecomeRatKing()) {
        Direction away = kingLoc.directionTo(rc.getLocation());
        if (rc.canMove(away)) {
            rc.move(away);
        } else {
            Direction j = directions[rand.nextInt(directions.length - 1)];
            if (rc.canTurn(j)) rc.turn(j);
            if (rc.canMoveForward()) rc.moveForward();
        }
        rc.setIndicatorString("Unsticking near king");
        return;
    }
}  

        // HOLD only BEFORE second king exists (promotion phase only)
        if (!secondKingBuilt && rawCheese == 0
        && rc.getAllCheese() >= PROMO_RALLY_CHEESE
        && isKingBuilder(rc)) {

        int d2 = rc.getLocation().distanceSquaredTo(kingLoc);
        if (d2 <= 2) {
        rc.setIndicatorString("Holding in 3x3 for promotion (builder)");
        return;
    }
}
        

        if (rc.canTurn(toKing)) {
            rc.turn(toKing);
        }

        if (rc.canSenseLocation(kingLoc) && (kingLoc.distanceSquaredTo(rc.getLocation()) <= 4 )) {

            RobotInfo[] kingLocations = rc.senseNearbyRobots(kingLoc, 8, rc.getTeam());

            for (RobotInfo robotInfo : kingLocations) {
                if (robotInfo.getType().isRatKingType()) {
                    MapLocation actualKingLoc = robotInfo.getLocation();
                    boolean result = rc.canTransferCheese(actualKingLoc, rawCheese);
                    rc.setIndicatorString("Can transfer " + rawCheese + " to king at " + actualKingLoc.toString() + "? " + result);
                    if (result) {
                        rc.transferCheese(actualKingLoc, rawCheese);

                        // After second king exists, do NOT linger. Get back out and keep throughput high.
                        if (secondKingBuilt) {
                            rc.setIndicatorString("Transferred; 2K econ -> explore");
                            return;
                        }

                        // Before second king: builders may hold briefly to enable promotion.
                        if (rc.getAllCheese() >= PROMO_RALLY_CHEESE && isKingBuilder(rc)) {
                            rc.setIndicatorString("Transferred; holding for promotion (builder)");
                            // fall through to HOLD/movement logic below
                        } else {
                            rc.setIndicatorString("Transferred; returning to explore");
                            return;
                        }
                    } else {
                        // Can't transfer; caller will decide behavior next turn
                        rc.setIndicatorString("Near king but cannot transfer");
                    }
                    break;
                }
            }

            if (mineLoc != null) {
                int msgBytes = getSqueak(SqueakType.CHEESE_MINE, toInteger(mineLoc));
                rc.squeak(msgBytes);
                mineLoc = null;
            }
        }

        if (rc.canRemoveDirt(nextLoc)) {
            rc.removeDirt(nextLoc);
        }

        // Move toward the king (simple greedy movement)
        if (rc.canMove(toKing)) {
            rc.move(toKing);
        } else if (rc.canMoveForward()) {
            // Fallback: keep moving forward if we can't move in the desired direction
            rc.moveForward();
        } else {
            // If totally blocked, pick a new direction to avoid deadlock
            Direction turnDir = directions[rand.nextInt(directions.length - 1)];
            if (rc.canTurn(turnDir)) {
                rc.turn(turnDir);
            }
        }
    }

    // Baby-side promotion: ANY baby can become a Rat King when the engine allows it.
    public static boolean tryBuildKing(RobotController rc) throws GameActionException {
    if (!rc.getType().isBabyRatType()) return false;
    if (!rc.isActionReady()) return false;
    if (rc.getAllCheese() < PROMO_RALLY_CHEESE) return false;
    if (!rc.canBecomeRatKing()) return false;

    rc.becomeRatKing();
    secondKingBuilt = true;
    System.out.println("NEW KING BUILT at round " + rc.getRoundNum() + " by rat " + rc.getID());
    return true;
}

    public static int getFirstInt(int loc) {
        // extract 10 smallest place value bits from toInteger(loc)
        return loc % 1024;
    }

    public static int getLastInt(int loc) {
        // extract bits with place values >= 2^10 from toInteger(loc)
        return loc >> 10;
    }

    public static int toInteger(MapLocation loc) {
        return (loc.x << 6) | loc.y;
    }

    public static int getX(int encodedLoc) {
        return encodedLoc >> 6;
    }

    public static int getY(int encodedLoc) {
        return encodedLoc % 64;
    }

    public static int getSqueak(SqueakType type, int value) {
        switch (type) {
            case ENEMY_RAT_KING:
                return (1 << 12) | value;
            case ENEMY_COUNT:
                return (2 << 12) | value;
            case CHEESE_MINE:
                return (3 << 12) | value;
            case CAT_FOUND:
                return (4 << 12) | value;
            default:
                return value;
        }
    }
    
    public static SqueakType getSqueakType(int rawSqueak) {
        return squeakTypes[rawSqueak >> 12];
    }

    public static int getSqueakValue(int rawSqueak) {
        // Only uses lower 12 bits
        return rawSqueak % 4096;
    }
    
}
