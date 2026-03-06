package pathfinder;

import battlecode.common.*;

class MockRobotController implements RobotController {
        @Override
        public int getMapWidth() {
            return 100;
        }

        @Override
        public int getMapHeight() {
            return 100;
        }

        @Override
        public int getRoundNum() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getRoundNum'");
        }

        @Override
        public boolean isCooperation() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isCooperation'");
        }

        @Override
        public Team getBackstabbingTeam() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getBackstabbingTeam'");
        }

        @Override
        public int getNumberCatTraps() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getNumberCatTraps'");
        }

        @Override
        public int getNumberRatTraps() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getNumberRatTraps'");
        }

        @Override
        public int getID() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getID'");
        }

        @Override
        public Team getTeam() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getTeam'");
        }

        @Override
        public MapLocation getLocation() {
            return new MapLocation(10, 9);
        }

        @Override
        public MapLocation[] getAllPartLocations() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getAllPartLocations'");
        }

        @Override
        public Direction getDirection() {
            return Direction.CENTER;
        }

        @Override
        public int getHealth() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getHealth'");
        }

        @Override
        public int getRawCheese() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getRawCheese'");
        }

        @Override
        public int getGlobalCheese() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getGlobalCheese'");
        }

        @Override
        public int getAllCheese() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getAllCheese'");
        }

        @Override
        public int getDirt() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getDirt'");
        }

        @Override
        public UnitType getType() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getType'");
        }

        @Override
        public RobotInfo getCarrying() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getCarrying'");
        }

        @Override
        public boolean isBeingThrown() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isBeingThrown'");
        }

        @Override
        public boolean isBeingCarried() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isBeingCarried'");
        }

        @Override
        public boolean onTheMap(MapLocation loc) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'onTheMap'");
        }

        @Override
        public boolean canSenseLocation(MapLocation loc) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canSenseLocation'");
        }

        @Override
        public boolean isLocationOccupied(MapLocation loc) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isLocationOccupied'");
        }

        @Override
        public boolean canSenseRobotAtLocation(MapLocation loc) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canSenseRobotAtLocation'");
        }

        @Override
        public RobotInfo senseRobotAtLocation(MapLocation loc) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'senseRobotAtLocation'");
        }

        @Override
        public boolean canSenseRobot(int id) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canSenseRobot'");
        }

        @Override
        public RobotInfo senseRobot(int id) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'senseRobot'");
        }

        @Override
        public RobotInfo[] senseNearbyRobots() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'senseNearbyRobots'");
        }

        @Override
        public RobotInfo[] senseNearbyRobots(int radiusSquared) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'senseNearbyRobots'");
        }

        @Override
        public RobotInfo[] senseNearbyRobots(int radiusSquared, Team team) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'senseNearbyRobots'");
        }

        @Override
        public RobotInfo[] senseNearbyRobots(MapLocation center, int radiusSquared, Team team)
                throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'senseNearbyRobots'");
        }

        @Override
        public boolean sensePassability(MapLocation loc) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'sensePassability'");
        }

        @Override
        public MapInfo senseMapInfo(MapLocation loc) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'senseMapInfo'");
        }

        @Override
        public MapInfo[] senseNearbyMapInfos() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'senseNearbyMapInfos'");
        }

        @Override
        public MapInfo[] senseNearbyMapInfos(int radiusSquared) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'senseNearbyMapInfos'");
        }

        @Override
        public MapInfo[] senseNearbyMapInfos(MapLocation center) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'senseNearbyMapInfos'");
        }

        @Override
        public MapInfo[] senseNearbyMapInfos(MapLocation center, int radiusSquared) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'senseNearbyMapInfos'");
        }

        @Override
        public MapLocation adjacentLocation(Direction dir) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'adjacentLocation'");
        }

        @Override
        public MapLocation[] getAllLocationsWithinRadiusSquared(MapLocation center, int radiusSquared)
                throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getAllLocationsWithinRadiusSquared'");
        }

        @Override
        public boolean isActionReady() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isActionReady'");
        }

        @Override
        public int getActionCooldownTurns() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getActionCooldownTurns'");
        }

        @Override
        public boolean isMovementReady() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isMovementReady'");
        }

        @Override
        public boolean isTurningReady() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'isTurningReady'");
        }

        @Override
        public int getMovementCooldownTurns() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getMovementCooldownTurns'");
        }

        @Override
        public int getTurningCooldownTurns() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getTurningCooldownTurns'");
        }

        @Override
        public boolean canMoveForward() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canMoveForward'");
        }

        @Override
        public boolean canMove(Direction d) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canMove'");
        }

        @Override
        public void moveForward() throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'moveForward'");
        }

        @Override
        public void move(Direction d) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'move'");
        }

        @Override
        public boolean canTurn() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canTurn'");
        }

        @Override
        public boolean canTurn(Direction d) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canTurn'");
        }

        @Override
        public void turn(Direction d) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'turn'");
        }

        @Override
        public int getCurrentRatCost() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getCurrentRatCost'");
        }

        @Override
        public boolean canBuildRat(MapLocation loc) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canBuildRat'");
        }

        @Override
        public void buildRat(MapLocation loc) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'buildRat'");
        }

        @Override
        public boolean canBecomeRatKing() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canBecomeRatKing'");
        }

        @Override
        public void becomeRatKing() throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'becomeRatKing'");
        }

        @Override
        public boolean canPlaceDirt(MapLocation loc) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canPlaceDirt'");
        }

        @Override
        public void placeDirt(MapLocation loc) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'placeDirt'");
        }

        @Override
        public boolean canRemoveDirt(MapLocation loc) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canRemoveDirt'");
        }

        @Override
        public void removeDirt(MapLocation loc) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'removeDirt'");
        }

        @Override
        public boolean canPlaceRatTrap(MapLocation loc) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canPlaceRatTrap'");
        }

        @Override
        public void placeRatTrap(MapLocation loc) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'placeRatTrap'");
        }

        @Override
        public boolean canRemoveRatTrap(MapLocation loc) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canRemoveRatTrap'");
        }

        @Override
        public void removeRatTrap(MapLocation loc) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'removeRatTrap'");
        }

        @Override
        public boolean canPlaceCatTrap(MapLocation loc) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canPlaceCatTrap'");
        }

        @Override
        public void placeCatTrap(MapLocation loc) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'placeCatTrap'");
        }

        @Override
        public boolean canRemoveCatTrap(MapLocation loc) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canRemoveCatTrap'");
        }

        @Override
        public void removeCatTrap(MapLocation loc) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'removeCatTrap'");
        }

        @Override
        public boolean canPickUpCheese(MapLocation loc) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canPickUpCheese'");
        }

        @Override
        public void pickUpCheese(MapLocation loc) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'pickUpCheese'");
        }

        @Override
        public void pickUpCheese(MapLocation loc, int pickUpAmount) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'pickUpCheese'");
        }

        @Override
        public boolean canAttack(MapLocation loc) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canAttack'");
        }

        @Override
        public boolean canAttack(MapLocation loc, int cheeseAmount) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canAttack'");
        }

        @Override
        public void attack(MapLocation loc) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'attack'");
        }

        @Override
        public void attack(MapLocation loc, int cheeseAmount) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'attack'");
        }

        @Override
        public boolean squeak(int messageContent) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'squeak'");
        }

        @Override
        public Message[] readSqueaks(int roundNum) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'readSqueaks'");
        }

        @Override
        public void writeSharedArray(int index, int value) throws GameActionException {
            // TODO Auto-generated method stub
        }

        @Override
        public int readSharedArray(int index) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'readSharedArray'");
        }

        @Override
        public boolean canTransferCheese(MapLocation loc, int amount) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canTransferCheese'");
        }

        @Override
        public void transferCheese(MapLocation loc, int amount) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'transferCheese'");
        }

        @Override
        public void throwRat() throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'throwRat'");
        }

        @Override
        public boolean canThrowRat() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canThrowRat'");
        }

        @Override
        public void dropRat(Direction dir) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'dropRat'");
        }

        @Override
        public boolean canDropRat(Direction dir) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canDropRat'");
        }

        @Override
        public boolean canCarryRat(MapLocation loc) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'canCarryRat'");
        }

        @Override
        public void carryRat(MapLocation loc) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'carryRat'");
        }

        @Override
        public void disintegrate() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'disintegrate'");
        }

        @Override
        public void resign() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'resign'");
        }

        @Override
        public void setIndicatorString(String string) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setIndicatorString'");
        }

        @Override
        public void setIndicatorDot(MapLocation loc, int red, int green, int blue) throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setIndicatorDot'");
        }

        @Override
        public void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int red, int green, int blue)
                throws GameActionException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setIndicatorLine'");
        }

        @Override
        public void setTimelineMarker(String label, int red, int green, int blue) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'setTimelineMarker'");
        }
    }
