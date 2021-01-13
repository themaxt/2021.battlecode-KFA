package muckrakertest;

import battlecode.common.*;

public class Muckraker extends Robot {
	/**
	 * Constants
	 */
	public final int ROUND_TO_START_HARASS = 500;
	public final int EXPLORER_MUCKRAKER = 1;
	public final int HARASS_MUCKRAKER = 2;
	public final int BOUNCE_EXPLORER = 1;
	public final int EDGE_EXPLORER = 2;

	/**
	 * Muckraker's attributes
	 */
	public int muckrakerType;
	RobotInfo[] robotsInExpose;
	RobotInfo[] robotsInSense;
	// Explorer Muckraker
	public MapLocation target;
	public int explorerType;
	// Harass Muckraker
	public MapLocation enemyEC;
	public MapLocation[] enemyECCircle;
	public int enemyECCircleIndex;

	/**
	 * Constructor Set to a Regular Muckraker at creation Set its heading based on
	 * whichever side of EC it is spawned Initialize any other variables
	 * 
	 * @param rc
	 */
	public Muckraker(RobotController rc) {
		super(rc);
		robotsInExpose = rc.senseNearbyRobots(12, opponentTeam);
		robotsInSense = rc.senseNearbyRobots(30, opponentTeam);
		if (roundNum < ROUND_TO_START_HARASS) {
			muckrakerType = EXPLORER_MUCKRAKER;
			if (coinFlip()) {
				explorerType = EDGE_EXPLORER;
			} else {
				explorerType = BOUNCE_EXPLORER;
			}
		} else {
			muckrakerType = HARASS_MUCKRAKER;
		}
		if (explorerType == EDGE_EXPLORER || explorerType == BOUNCE_EXPLORER) {
			target = getTargetRelativeEC();
		}
	}

	/**
	 * Muckraker's take turn method Switch statement to execute different actions
	 * for different types of muckrakers
	 */
	public void takeTurn() throws GameActionException {
		super.takeTurn();
		robotsInExpose = rc.senseNearbyRobots(12, opponentTeam);
		robotsInSense = rc.senseNearbyRobots(30, opponentTeam);
		exposeOnSight();
		if (enemyEC == null) {
			enemyEC = findEnemyEC();
		}
		switch (muckrakerType) {
			case EXPLORER_MUCKRAKER:
				explorerMuckraker();
				break;
			case HARASS_MUCKRAKER:
				harassMuckraker();
				break;
		}
	}

	/**
	 * Muckraker that looks for map edges and neutral ECs
	 * 
	 * @throws GameActionException
	 */
	public void explorerMuckraker() throws GameActionException {
		// if edge is detected report location to EC if EC does not know map corners
		// yet, and change target
		int[] edges = nav.lookForEdges();
		if (edges != null) {
			// raise flag telling it found edge and coordinates of edge
			updateTargetAtEdge(edges);
		}
		nav.tryMoveToTarget(target);
	}

	/**
	 * Muckraker that sits near enemy EC killing any slanders
	 * 
	 * @throws GameActionException
	 */
	public void harassMuckraker() throws GameActionException {
		if (currLoc.equals(enemyECCircle[enemyECCircleIndex])) {
			return;
		} else if (rc.canSenseLocation(enemyECCircle[enemyECCircleIndex])
				&& rc.senseRobotAtLocation(enemyECCircle[enemyECCircleIndex]) == null) {
			nav.tryMoveToTarget(enemyECCircle[enemyECCircleIndex]);
		} else {
			enemyECCircleIndex++;
		}
	}

	/**
	 * Method for all muckrakers to expose any slanders if detected
	 * 
	 * @throws GameActionException
	 */
	public void exposeOnSight() throws GameActionException {
		if (robotsInExpose.length > 0) {
			exposeMaxConv();
			// send message to EC
		} else if (robotsInSense.length > 0) {
			MapLocation robotLoc = findMaxConv(true);
			nav.tryMoveToTarget(robotLoc);
		}
	}

	/**
	 * Finds the robot with the max conviction given an array of robots and exposes
	 * it if it can
	 * 
	 * @param robotsInSight
	 * @throws GameActionException
	 */
	public void exposeMaxConv() throws GameActionException {
		MapLocation exposeLoc = findMaxConv(false);
		if (rc.canExpose(exposeLoc)) {
			rc.expose(exposeLoc);
		}
	}

	/**
	 * Finds the robot with the most conviction
	 * 
	 * @param robots
	 * @return
	 */
	public MapLocation findMaxConv(boolean checkMaxRadius) {
		if (checkMaxRadius) {
			int indexOfMaxConv = 0;
			for (int i = 0; i < robotsInSense.length; i++) {
				RobotInfo ri = robotsInSense[i];
				if (ri.getType().equals(RobotType.SLANDERER)) {
					if (ri.getConviction() > robotsInSense[indexOfMaxConv].getConviction()) {
						indexOfMaxConv = i;
					}
				}
			}
			return robotsInSense[indexOfMaxConv].getLocation();
		} else {
			int indexOfMaxConv = 0;
			for (int i = 0; i < robotsInExpose.length; i++) {
				RobotInfo ri = robotsInExpose[i];
				if (ri.getType().equals(RobotType.SLANDERER)) {
					if (ri.getConviction() > robotsInExpose[indexOfMaxConv].getConviction()) {
						indexOfMaxConv = i;
					}
				}
			}
			return robotsInExpose[indexOfMaxConv].getLocation();
		}
	}

	/**
	 * checks for the enemy EC
	 * 
	 * @param robots
	 * @return
	 */
	public MapLocation findEnemyEC() {
		for (int i = 0; i < robotsInSense.length; i++) {
			RobotInfo ri = robotsInSense[i];
			if (ri.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
				return ri.getLocation();
			}
		}
		// check comms for enemyEC
		return null;
	}

	/**
	 * Finds a target location based on direction relative to the EC
	 * 
	 * @return
	 */
	public MapLocation getTargetRelativeEC() {
		int dx = 0;
		int dy = 0;
		RobotInfo[] robots = rc.senseNearbyRobots(2, myTeam);
		for (int i = 0; i < robots.length; i++) {
			RobotInfo ri = robots[i];
			if (ri.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
				dx = currLoc.x - ri.getLocation().x;
				dy = currLoc.y - ri.getLocation().y;
			}
		}
		return new MapLocation(currLoc.x + dx * 64, currLoc.y + dy * 64);
	}

	/**
	 * Figure out the new target location based on a random heading when
	 * encountering an edge
	 * 
	 * @param edges
	 */
	public void updateTargetAtEdge(int[] edges) {
		int edgeType = edges[0];
		MapLocation cornerEdgeLoc = new MapLocation(edges[1], edges[2]);
		Direction directionToCornerEdge = currLoc.directionTo(cornerEdgeLoc);
		Direction heading;
		if (explorerType == BOUNCE_EXPLORER) {
			heading = randomBounceDirectionAtEdge(cornerEdgeLoc, edgeType, directionToCornerEdge);
		} else {
			if (edgeType == nav.NORTHEAST_INT || edgeType == nav.NORTHWEST_INT || edgeType == nav.SOUTHEAST_INT
					|| edgeType == nav.SOUTHWEST_INT) {
				if (coinFlip()) {
					heading = directionToCornerEdge.rotateRight();
				} else {
					heading = directionToCornerEdge.rotateLeft();
				}
			} else {
				if (coinFlip()) {
					heading = directionToCornerEdge.rotateRight().rotateRight();
				} else {
					heading = directionToCornerEdge.rotateLeft().rotateLeft();
				}
			}
		}
		target = new MapLocation(currLoc.x + (heading.dx * 64), currLoc.y + (heading.dy * 64));
	}

	public Direction randomBounceDirectionAtEdge(MapLocation cornerEdgeLoc, int edgeType,
			Direction directionToCornerEdge) {
		Direction iterDirection = directionToCornerEdge.rotateRight().rotateRight();
		Direction[] possibleDirections;
		// check if it is a corner or edge and set variables accordingly
		if (edgeType == nav.NORTHEAST_INT || edgeType == nav.NORTHWEST_INT || edgeType == nav.SOUTHEAST_INT
				|| edgeType == nav.SOUTHWEST_INT) {
			possibleDirections = new Direction[2];
			iterDirection = iterDirection.rotateRight();
		} else {
			possibleDirections = new Direction[4];
		}
		// iterate through possible directions, ignore if it is the direction it just
		// came from
		int j = 0;
		for (int i = 0; i < possibleDirections.length + 1; i++) {
			if (iterDirection.equals(directionToCornerEdge.opposite())) {
				continue;
			}
			possibleDirections[j] = iterDirection;
			j++;
		}

		// pick one of the random directions and calculate target in that direction
		return possibleDirections[(int) (Math.random() * possibleDirections.length)];
	}

	public void generateCircle() {
		enemyECCircle = new MapLocation[20];
		int enemyECX = enemyEC.x;
		int enemyECY = enemyEC.y;
		enemyECCircle[0] = new MapLocation(enemyECX, enemyECY + 4);
		enemyECCircle[1] = new MapLocation(enemyECX + 1, enemyECY + 4);
		enemyECCircle[2] = new MapLocation(enemyECX + 2, enemyECY + 3);
		enemyECCircle[3] = new MapLocation(enemyECX + 3, enemyECY + 2);
		enemyECCircle[4] = new MapLocation(enemyECX + 4, enemyECY + 1);
		enemyECCircle[5] = new MapLocation(enemyECX + 4, enemyECY);
		enemyECCircle[6] = new MapLocation(enemyECX + 4, enemyECY - 1);
		enemyECCircle[7] = new MapLocation(enemyECX + 3, enemyECY - 2);
		enemyECCircle[8] = new MapLocation(enemyECX + 2, enemyECY - 3);
		enemyECCircle[9] = new MapLocation(enemyECX + 1, enemyECY - 4);
		enemyECCircle[10] = new MapLocation(enemyECX, enemyECY - 4);
		enemyECCircle[11] = new MapLocation(enemyECX - 1, enemyECY - 4);
		enemyECCircle[12] = new MapLocation(enemyECX - 2, enemyECY - 3);
		enemyECCircle[13] = new MapLocation(enemyECX - 3, enemyECY - 2);
		enemyECCircle[14] = new MapLocation(enemyECX - 4, enemyECY - 1);
		enemyECCircle[15] = new MapLocation(enemyECX - 4, enemyECY);
		enemyECCircle[16] = new MapLocation(enemyECX - 4, enemyECY + 1);
		enemyECCircle[17] = new MapLocation(enemyECX - 3, enemyECY + 2);
		enemyECCircle[18] = new MapLocation(enemyECX - 2, enemyECY + 3);
		enemyECCircle[19] = new MapLocation(enemyECX - 1, enemyECY + 4);
	}
}