package Robos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;
//https://streamable.com/t8z0n
public class OdometerSB extends AdvancedRobot {
	private class Position<T> {
		T x, y;

		Position(T x, T y, T distance, T angle) {
			this.x = x;
			this.y = y;
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
		/* Transform our coordinates into a vector */
		x -= getX();
		y -= getY();
	 
		/* Calculate the angle to the target position */
		double angleToTarget = Math.atan2(x, y);
	 
		/* Calculate the turn required get there */
		double targetAngle = Utils.normalRelativeAngle(angleToTarget - getHeadingRadians());
	 
		/* 
		 * The Java Hypot method is a quick way of getting the length
		 * of a vector. Which in this case is also the distance between
		 * our robot and the target location.
		 */
		double distance = Math.hypot(x, y);
	 
		/* This is a simple method of performing set front as back */
		double turnAngle = Math.atan(Math.tan(targetAngle));
		setTurnRightRadians(turnAngle);
		if(targetAngle == turnAngle) {
			setAhead(distance);
		} else {
			setBack(distance);
		}
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
	
	@SuppressWarnings("rawtypes")
	public void move(int quadrant) {
		Position enemyPos = null;
		for (Position<?> p : this.positions) {
			if (getQuadrant((double) p.x, (double) p.y) == quadrant) {
				enemyPos = p;
			}
		}
		if(targetX == 0 && targetY == 0 && enemyPos != null) {
			double hipotenusa = Math.sqrt((getWidth() * getWidth())*2);
		switch (quadrant) {
		case 2:
			targetX = (double) enemyPos.x - hipotenusa;
			targetY = (double) enemyPos.y + hipotenusa;
			break;
		case 1:
			targetX = (double) enemyPos.x + hipotenusa;
			targetY = (double) enemyPos.y + hipotenusa;
			break;
		case 4:
			targetX = (double) enemyPos.x + hipotenusa;
			targetY = (double) enemyPos.y - getWidth();
			break;
		default:
			break;
		}
		}
		goTo(targetX, targetY);
	}

	private void executeStep(int quadrant) {
		this.positions = new ArrayList<Position<Double>>();
		// scan for robots
		turnRadarRight(360);
		move(quadrant);
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
				executeStep(2);
				if ((curX - targetX) * (curX - targetX) < 1 && (curY - targetY) * (curY - targetY) < 1) {
					firstStep = false;
					goTo(targetX + getWidth(), targetY);
					secondStep = true;
					targetX = targetY = 0;
				}
			}
			else if (secondStep) {
				executeStep(1);
				if ((curX - targetX) * (curX - targetX) < 1 && (curY - targetY) * (curY - targetY) < 1) {
					secondStep = false;
					thirdStep = true;
					targetX = targetY = 0;
				}
			}
			else if (thirdStep) {
				executeStep(4);
				if ((curX - targetX) * (curX - targetX) < 1 && (curY - targetY) * (curY - targetY) < 1) {
					thirdStep = false;
					goTo(targetX, targetY - getWidth());
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
