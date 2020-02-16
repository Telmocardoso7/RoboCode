package Robos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
import standardOdometer.*;

//https://streamable.com/t8z0n
public class OdometerSB extends AdvancedRobot {
	private class Position<T> {
		T x, y, distance, angle;

		Position(T x, T y, T distance, T angle) {
			this.x = x;
			this.y = y;
			this.distance = distance;
			this.angle = angle;
		}
	}

	private boolean initializing = true;
	private boolean waiting = false, firstStep = false, secondStep = false, thirdStep = false, goingHome = false;

	private RobotStatus status;
	private List<Position<Double>> positions;
	private double targetX = 0, targetY = 0;
	private long startingTime;
	
	double lastX = 0, lastY = 0;
	long totalDistance = 0;
	
	private void goTo(double x, double y) {
	    /* Calculate the difference bettwen the current position and the target position. */
	    x = x - getX();
	    y = y - getY();
	 
	    /* Calculate the angle relative to the current heading. */
	    double goAngle = Utils.normalRelativeAngle(Math.atan2(x, y) - getHeadingRadians());
	 
	    /*
	     * Apply a tangent to the turn this is a cheap way of achieving back to front turn angle as tangents period is PI.
	     * The output is very close to doing it correctly under most inputs. Applying the arctan will reverse the function
	     * back into a normal value, correcting the value. The arctan is not needed if code size is required, the error from
	     * tangent evening out over multiple turns.
	     */
	    setTurnRightRadians(Math.atan(Math.tan(goAngle)));
	 
	    /* 
	     * The cosine call reduces the amount moved more the more perpendicular it is to the desired angle of travel. The
	     * hypot is a quick way of calculating the distance to move as it calculates the length of the given coordinates
	     * from 0.
	     */
	    setAhead(Math.cos(goAngle) * Math.hypot(x, y));
	}

	public void onStatus(StatusEvent event) {
		this.status = event.getStatus(); // saving the current robot status, in a attribute global variable.
		double currentX = status.getX();
		double currentY = status.getY();
		
		double distance = distanceBetween2Points(this.lastX, this.lastY, currentX, currentY); 
		
		if(firstStep || secondStep || thirdStep || goingHome){
			this.totalDistance += distance;
			this.lastX = currentX;
			this.lastY = currentY;
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {

		double angleToEnemy = e.getBearing();
		double angleToEnemyRadians = e.getBearingRadians();

		// Calculate the angle to the scanned robot
		double angle = Math.toRadians((this.status.getHeading() + angleToEnemy) % 360);

		// Calculate the coordinates of the robot
		double enemyX = (this.status.getX() + Math.sin(angle) * e.getDistance());
		double enemyY = (this.status.getY() + Math.cos(angle) * e.getDistance());
		double enemyDistance = e.getDistance();

		// Saving enemy position variables in a list of object class.
		if(this.positions != null) {			
			this.positions.add(new Position<Double>(enemyX, enemyY, enemyDistance, angleToEnemyRadians));
		}
	}

	private int getQuadrant(double x, double y) {
		double height = getBattleFieldHeight() / 2;
		double width = getBattleFieldWidth() / 2;

		if ((x > width) && (y > height)) {
			return 1;
		} else if ((x > width) && (y < height)) {
			return 4;
		} else if ((x < width) && (y < height)) {
			return 3;
		} else {
			return 2;
		}
	}
	
	public double distanceBetween2Points(double x1,double y1,double x2, double y2){
		return Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
	}

	private void firstStep() {
		this.positions = new ArrayList<Position<Double>>();
		// scan for robots
		turnRadarRight(360);
		// get robot thats on top left (2nd quadrant)
		Position enemyPos = null;
		for (Position<?> p : this.positions) {
			if (getQuadrant((double) p.x, (double) p.y) == 2) {
				enemyPos = p;
			}
		}
		// move to that robot
		if(targetX == 0 && targetY == 0 && enemyPos != null) {			
			targetX = (double) enemyPos.x - 50;
			targetY = (double) enemyPos.y + 50;
		}
		goTo(targetX, targetY);
		execute();
	}
	
	private void secondStep() {
		this.positions = new ArrayList<Position<Double>>();
		// scan for robots
		turnRadarRight(360);
		Position enemyPos = null;
		for (Position p : this.positions) {
			if (getQuadrant((double) p.x, (double) p.y) == 1) {
				enemyPos = p;
			}
		}
		// move to that robot
		if(targetX == 0 && targetY == 0 && enemyPos != null) {		
			targetX = (double) enemyPos.x + 50;
			targetY = (double) enemyPos.y + 50;
		}
		goTo(targetX, targetY);
		execute();
	}
	
	private void thirdStep() {
		this.positions = new ArrayList<Position<Double>>();
		// scan for robots
		turnRadarRight(360);
		Position enemyPos = null;
		for (Position p : this.positions) {
			if (getQuadrant((double) p.x, (double) p.y) == 4) {
				enemyPos = p;
			}
		}
		// move to that robot
		if(targetX == 0 && targetY == 0 && enemyPos != null) {			
			targetX = (double) enemyPos.x + 50;
			targetY = (double) enemyPos.y - 50;
		}
		goTo(targetX, targetY);
		execute();
	}

	public void run() {
		while (true) {
			double curX, curY;
			int tx = 18;
			int ty = 18;
			curX = status.getX();
			curY = status.getY();
			if (initializing) {
				goTo(tx, ty);
				if (curX == tx && curY == ty) {
					initializing = false;
					waiting = true;
					startingTime = (new Date().getTime())/1000; 
					out.println(curX + "x " + curY + "y");
				}
			}
			// wait 10 seconds
			else if (waiting) {
				long currentTime = (new Date().getTime()) / 1000; // getting the current time from when this method has been called.
				if ((currentTime - this.startingTime) > 2) { // Starts a wait time after robot hits (18,18)
					waiting = false;
					firstStep = true;
				}
			}
			// move to top left robot
			else if (firstStep) {
				firstStep();
				if ((curX - targetX) * (curX - targetX) < 1 && (curY - targetY) * (curY - targetY) < 1) {
					firstStep = false;
					secondStep = true;
					targetX = targetY = 0;
				}
			}
			else if (secondStep) {
				secondStep();
				if ((curX - targetX) * (curX - targetX) < 1 && (curY - targetY) * (curY - targetY) < 1) {
					secondStep = false;
					thirdStep = true;
					targetX = targetY = 0;
				}
			}
			else if (thirdStep) {
				thirdStep();
				if ((curX - targetX) * (curX - targetX) < 1 && (curY - targetY) * (curY - targetY) < 1) {
					thirdStep = false;
					goingHome = true;
					out.println("go home");
					targetX = targetY = 0;
				}
			}
			else if(goingHome) {	
				//at the end, goes back to the start
				goTo(18,18);
				if(curX < 19 && curY < 19) {
					goingHome = false;
					out.println("Moved " + this.totalDistance + " pixels during this race");
				}
			}
			execute();
		}
	}
}
