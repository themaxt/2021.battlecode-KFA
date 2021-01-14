package muckrakertest;

import battlecode.common.*;

public class Slanderer extends Robot {

	public Slanderer(RobotController rc) throws GameActionException {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		nav.tryMove(nav.randomDirection());
	}

}