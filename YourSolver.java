package com.codenjoy.dojo.icancode.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import com.codenjoy.dojo.icancode.model.Elements;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.RandomDice;

/**
 * Your AI
 */
public class YourSolver extends AbstractSolver {

	/**
	 * @param dice DIP (SOLID) for Random dependency in your Solver realization
	 */
	public YourSolver(Dice dice) {
		super(dice);
	}

	/**
	 * @param board use it for find elements on board
	 * @return what hero should do in this tick (for this board)
	 */
	@Override
	public Command whatToDo(Board board) {

		return makeDecision4(board);
	}

	public int getPointsPerkTargets(Board b, Point me, Direction dir, int steps) {

		int pts = 0;

		// Explore right.
		Point p = me.copy();
		int rounds = 0;
		do {
			rounds++;
			p = nextPoint(p, dir, 1);
			if (isZombie(b, p)) {
				pts += 5;
			} else if (b.isAt(p, Elements.ROBO_OTHER)) {
				pts += 10;
			}
		} while (!b.isOutOfField(p.getX(), p.getY()) && rounds < steps);

		return pts;
	}

	public Direction getMostPromisedDirPerk(Board b, Point me, int steps) {
		int pr = getPointsPerkTargets(b, me, Direction.RIGHT, steps);
		int pl = getPointsPerkTargets(b, me, Direction.LEFT, steps);
		int pu = getPointsPerkTargets(b, me, Direction.UP, steps);
		int pd = getPointsPerkTargets(b, me, Direction.DOWN, steps);
		int pts[] = { pl, pu, pd };
		Direction[] dirs = { Direction.LEFT, Direction.UP, Direction.DOWN };
		Direction res = Direction.RIGHT;
		int max = pr;
		for (int i = 0; i < 3; i++) {
			if (pts[i] > max) {
				max = pts[i];
				res = dirs[i];
			}
		}
		return res;
	}

	private int numGolds;

	public boolean isPullAllowed(Board b, Point box, Direction nextDir) {
		Point pullTarget = nextPoint(box, nextDir, 1);
		Point prch = nextPoint(b.getMe(), Direction.RIGHT, 1); // right
		Point plch = nextPoint(b.getMe(), Direction.LEFT, 1); // left
		Point puch = nextPoint(b.getMe(), Direction.UP, 1); // up
		Point pdch = nextPoint(b.getMe(), Direction.DOWN, 1); // down
		if (b.isAt(prch, Elements.LASER_MACHINE_READY_LEFT) || b.isAt(plch, Elements.LASER_MACHINE_READY_RIGHT)
				|| b.isAt(puch, Elements.LASER_MACHINE_READY_DOWN) || b.isAt(pdch, Elements.LASER_MACHINE_READY_UP)) {
			return false;
		}
		if (!isBarrierAt(b, pullTarget) && !isLaserMachine(b, pullTarget) && !b.isAt(pullTarget, Elements.BOX)
				&& !b.isAt(pullTarget, Elements.GOLD, Elements.START, Elements.ROBO_OTHER)) {
			return true;
		}
		return false;
	}

	public boolean isLaserMachine(Board b, Point p) {
		return b.isAt(p, Elements.LASER_MACHINE_CHARGING_DOWN, Elements.LASER_MACHINE_CHARGING_LEFT,
				Elements.LASER_MACHINE_CHARGING_RIGHT, Elements.LASER_MACHINE_CHARGING_UP,
				Elements.LASER_MACHINE_READY_DOWN, Elements.LASER_MACHINE_READY_LEFT,
				Elements.LASER_MACHINE_READY_RIGHT, Elements.LASER_MACHINE_READY_UP);
	}

	public Command makeDecision2(Board b) {

		Command res = Command.reset();

		Point me = b.getMe();

		// First of all, safety (threats which can move).
		Direction zombieDir = getDirNear(b, me, 1, Elements.FEMALE_ZOMBIE, Elements.MALE_ZOMBIE);
		Direction opponentDir = getDirNear(b, me, 1, Elements.ROBO_OTHER);

		if (isBulletDanger(me, b, 1, null)) {
			System.out.println("isBulletDanger");
			return Command.jump();
		}

		if (opponentDir != null) {
			System.out.println("is opponent near");
			return new Command("ACT(3),ACT(1)," + opponentDir);
		}
		if (zombieDir != null) {
			System.out.println("is zombie near");
			return new Command("ACT(3),ACT(1)," + zombieDir);

			// If no dangers for me.
		} else {
			List<Point> gold = b.getGold();
			List<Point> exits = b.getExits();
			// TODO add other goals

			// Sort out the targets to have the nearest one first.
			Collections.sort(gold, (p1, p2) -> Integer.compare(steps(b, me, p1), steps(b, me, p2)));
			Collections.sort(exits, (p1, p2) -> Integer.compare(steps(b, me, p1), steps(b, me, p2)));

			exits.addAll(gold); // Exit has the highest priority.

			if (!exits.isEmpty()) {
				Point t = exits.get(0);
				List<Point> path = bfs(b, me, t, null);
				if (path != null) {

				}
			} else {
				System.out.println("no targets");
			}
		}

		List<Point> gold = b.getGold();
		List<Point> exits = b.getExits();
		// TODO add other goals

		Collections.sort(gold, (p1, p2) -> Integer.compare(steps(b, me, p1), steps(b, me, p2)));
		Collections.sort(exits, (p1, p2) -> Integer.compare(steps(b, me, p1), steps(b, me, p2)));

		exits.addAll(gold);

		// System.out.println("path to exit: " + bfs(b, me, b.getExits().get(0)));

		if (!exits.isEmpty()) {
			Point t = exits.get(0);
			List<Point> path = bfs(b, me, t, null);
			if (path != null) {
				System.out.println("path: " + path);
				Point next = path.get(1);

				Direction nextDir = getDir(me, next);

				if (b.isAt(next, Elements.BOX, Elements.HOLE)) {
					Point jumpTarget = nextPoint(next, nextDir, 2);
					if (isOpponentDanger(jumpTarget, b, 4)) {

						res = Command.doNothing();
					} else {
						if (isJumpAllowed(b, jumpTarget)) {
							res = Command.jump(nextDir);
						} else {
							// ?
						}
					}
				} else if (isBulletDanger(next, b, 1, null)) {
					res = Command.doNothing();
				} else if (isZombieDanger(next, b, 1)) {
					Point jumpTarget = nextPoint(next, nextDir, 1);
					if (!isBarrierAt(b, jumpTarget) && !b.isAt(jumpTarget, Elements.HOLE, Elements.BOX)) {

						// Special case when bullet danger after 4 steps following the direction of
						// jump.
						// 2 steps to bullet from jump target point.
						if (isBulletDanger(jumpTarget, b, 2, null)) {
							res = Command.jump();
						} else {
							res = Command.jump(nextDir);
						}
					} else {
						res = Command.doNothing(); // ? maybe go back?
					}
				} else if (isOpponentDanger(next, b, 1)) {

					res = Command.jump(nextDir);
				} else {
					res = Command.go(nextDir);
				}

			} else {
				System.out.println("Path is null");
			}
		} else {
			System.out.println("gold is empty");
		}

		return res;
	}

	public boolean isBullet(Board b, Point p) {
		return b.isAt(p, Elements.LASER_DOWN, Elements.LASER_LEFT, Elements.LASER_RIGHT, Elements.LASER_UP);
	}

	public boolean isZombie(Board b, Point p) {
		return b.isAt(p, Elements.FEMALE_ZOMBIE, Elements.MALE_ZOMBIE);
	}

	public Direction getBulletDirection(Elements el) {
		if (el == Elements.LASER_DOWN) {
			return Direction.DOWN;
		} else if (el == Elements.LASER_LEFT) {
			return Direction.LEFT;
		} else if (el == Elements.LASER_RIGHT) {
			return Direction.RIGHT;
		} else if (el == Elements.LASER_UP) {
			return Direction.UP;
		}
		return null;
	}

	// public Point nextPoint(Board b, Point p, Direction dir, int steps) {
	//
	// for (int i = 0; i < steps; i ++) {
	// p = nextPoint(p, dir, 1);
	// }
	// return p;
	// }

	public Direction getDirNear(Board b, Point p, int steps, Elements... el) {
		Point pr = nextPoint(p, Direction.RIGHT, steps); // right
		Point pl = nextPoint(p, Direction.LEFT, steps); // left
		Point pu = nextPoint(p, Direction.UP, steps); // up
		Point pd = nextPoint(p, Direction.DOWN, steps); // down
		if (b.isAt(pr, el)) {
			return Direction.RIGHT;
		} else if (b.isAt(pl, el)) {
			return Direction.LEFT;
		} else if (b.isAt(pu, el)) {
			return Direction.UP;
		} else if (b.isAt(pd, el)) {
			return Direction.DOWN;
		}
		return null;
	}

	///////////// ============ NEW METHODS ==============//////////////////////

	// public boolean allowedPull(Board b, Point p) {
	// return !isBarrierAt(b, p) && !b.getLaserMachines().contains(p);
	// }

	public int stepsToNearest(Board b, Point p) {
		int res = 0;
		// res += stepsToNearest(p, b, b.getGold());
		// res += stepsToNearest(p, b, b.getExits());
		// res += stepsToNearest(p, b, b.getPerks());
		// res += stepsToNearest(p, b, b.getOtherHeroes());
		// res += stepsToNearest(p, b, b.getZombies());

		return res;
	}

	public Point nextPoint(Point p, Direction dir, int steps) {
		Point np = p.copy();
		for (int i = 0; i < steps; i++) {
			np.change(dir);
		}
		return np;
	}

	public boolean isBulletDanger(Point p, Board b, int steps, Direction dir) {

		Point prch = nextPoint(p, Direction.RIGHT, 1); // right
		Point plch = nextPoint(p, Direction.LEFT, 1); // left
		Point puch = nextPoint(p, Direction.UP, 1); // up
		Point pdch = nextPoint(p, Direction.DOWN, 1); // down

		if (dir == null) { // If just danger, no matter the direction.
			Point pr = nextPoint(p, Direction.RIGHT, steps); // right
			Point pl = nextPoint(p, Direction.LEFT, steps); // left
			Point pu = nextPoint(p, Direction.UP, steps); // up
			Point pd = nextPoint(p, Direction.DOWN, steps); // down
			if (b.isAt(pr, Elements.LASER_LEFT, Elements.LASER_MACHINE_READY_LEFT)
					|| b.isAt(pl, Elements.LASER_RIGHT, Elements.LASER_MACHINE_READY_RIGHT)
					|| b.isAt(pu, Elements.LASER_DOWN, Elements.LASER_MACHINE_READY_DOWN)
					|| b.isAt(pd, Elements.LASER_UP, Elements.LASER_MACHINE_READY_UP)) {
				return true;
			}
			if (steps == 2) {
				// handle laser machine charging
				if (b.isAt(prch, Elements.LASER_MACHINE_CHARGING_LEFT)
						|| b.isAt(plch, Elements.LASER_MACHINE_CHARGING_RIGHT)
						|| b.isAt(puch, Elements.LASER_MACHINE_CHARGING_DOWN)
						|| b.isAt(pdch, Elements.LASER_MACHINE_CHARGING_UP)) {
					return true;
				}
			}
		} else {
			if (dir == Direction.RIGHT) {
				Point pr = nextPoint(p, Direction.RIGHT, steps); // right
				if (b.isAt(pr, Elements.LASER_LEFT, Elements.LASER_MACHINE_READY_LEFT)) {
					return true;
				}
			} else if (dir == Direction.LEFT) {
				Point pl = nextPoint(p, Direction.LEFT, steps); // left
				if (b.isAt(pl, Elements.LASER_RIGHT, Elements.LASER_MACHINE_READY_RIGHT)) {
					return true;
				}
			} else if (dir == Direction.UP) {
				Point pu = nextPoint(p, Direction.UP, steps); // up
				if (b.isAt(pu, Elements.LASER_DOWN, Elements.LASER_MACHINE_READY_DOWN)) {
					return true;
				}
			} else { // Down.
				Point pd = nextPoint(p, Direction.DOWN, steps); // down
				if (b.isAt(pd, Elements.LASER_UP, Elements.LASER_MACHINE_READY_UP)) {
					return true;
				}
			}
		}
		return false;
	}

	public Direction getOppositeDir(Direction dir) {
		if (dir == Direction.LEFT) {
			return Direction.RIGHT;
		} else if (dir == Direction.RIGHT) {
			return Direction.LEFT;
		} else if (dir == Direction.UP) {
			return Direction.DOWN;
		}
		return Direction.UP;
	}

	// TODO here I used only ROBO_LASER, not flying, not falling (check result!).
	public boolean isOpponentDanger(Point p, Board b, int steps) {

		Point pr = nextPoint(p, Direction.RIGHT, steps); // right
		Point pl = nextPoint(p, Direction.LEFT, steps); // left
		Point pu = nextPoint(p, Direction.UP, steps); // up
		Point pd = nextPoint(p, Direction.DOWN, steps); // down

		if (b.isAt(pr, Elements.ROBO_OTHER) || b.isAt(pl, Elements.ROBO_OTHER) || b.isAt(pu, Elements.ROBO_OTHER)
				|| b.isAt(pd, Elements.ROBO_OTHER)) {
			return true;
		}
		return false;
	}

	public boolean isZombieDanger(Point p, Board b, int steps) {

		Point pr = nextPoint(p, Direction.RIGHT, steps); // right
		Point pl = nextPoint(p, Direction.LEFT, steps); // left
		Point pu = nextPoint(p, Direction.UP, steps); // up
		Point pd = nextPoint(p, Direction.DOWN, steps); // down

		if (b.isAt(pr, Elements.FEMALE_ZOMBIE, Elements.MALE_ZOMBIE)
				|| b.isAt(pl, Elements.FEMALE_ZOMBIE, Elements.MALE_ZOMBIE)
				|| b.isAt(pu, Elements.FEMALE_ZOMBIE, Elements.MALE_ZOMBIE)
				|| b.isAt(pd, Elements.FEMALE_ZOMBIE, Elements.MALE_ZOMBIE)) {
			return true;
		}
		return false;
	}

	// New.
	// Gold, zombies, other players etc.
	public int stepsToNearest(Point p, Board b, List<Point> targets) {
		int steps = Integer.MAX_VALUE;
		Point np = null;
		List<Point> pth = null;
		for (Point t : targets) {
			List<Point> path = bfs(b, p, t, null);
			if (path != null && path.size() < steps) {
				steps = path.size();
				np = t;
				pth = path;
			}
		}

		return steps;
	}

	public int steps(Board b, Point s, Point t) {

		List<Point> path = bfs(b, s, t, null);
		if (path == null) {
			return Integer.MAX_VALUE;
		}
		return path.size();
	}

	// Returns direction to reach p from me.
	public Direction getDir(Point me, Point p) {
		Direction res;
		int diffX = me.getX() - p.getX();
		int diffY = me.getY() - p.getY();
		if (diffX != 0) {
			if (diffX > 0) {
				res = Direction.LEFT;
			} else {
				res = Direction.RIGHT;
			}
		} else {
			if (diffY > 0) {
				res = Direction.DOWN;
			} else {
				res = Direction.UP;
			}
		}
		return res;
	}

	public int getPriority(Board b, Point p) {
		int res = 0;
		if (b.isAt(p, Elements.GOLD)) {
			res = 10;
		} else if (b.isAt(p, Elements.EXIT)) {
			res = 9;
		} else if (b.isAt(p, Elements.EMPTY)) {
			res = 8;
		}
		return res;
	}

	// Updated.
	public List<Point> bfs(Board board, Point s, Point g, List<Point> barriers) {
		List<Point> res = null;
		if (isBarrierAt(board, s, null) || board.getMe().equals(g)) {
			return res;
		}
		// Queue<Point> q = new PriorityQueue<>((p1, p2) ->
		// Integer.compare(getPriority(board, p2), getPriority(board, p1)));
		Queue<Point> q = new LinkedList<>();

		Set<Point> discovered = new HashSet<>();
		Map<Point, Point> prevs = new HashMap<>();
		discovered.add(s);
		q.add(s);
		boolean found = false;
		while (!q.isEmpty()) {
			Point v = q.poll();
			if (v.equals(g)) {
				found = true;
				break;
			}

			for (Point w : neighbours(board, v, barriers)) {
				if (!discovered.contains(w)) {

					prevs.put(w, v);
					discovered.add(w);
					q.add(w);
				}
			}
		}
		if (found) {
			res = new ArrayList<>();
			for (Point next = g; next != null && next != s; next = prevs.get(next)) {
				res.add(next);
			}
			res.add(s);
			Collections.reverse(res);
		}
		return res;
	}

	public List<Point> neighbours(Board b, Point p, List<Point> barriers) {
		List<Point> res = new ArrayList<>();

		Point[] pts = { nextPoint(p, Direction.RIGHT, 1), nextPoint(p, Direction.LEFT, 1),
				nextPoint(p, Direction.UP, 1), nextPoint(p, Direction.DOWN, 1) };

		for (Point n : pts) {
			if (!isBarrierAt(b, n, p)) {
				if (barriers != null) {
					if (!barriers.contains(n)) {
						if (!b.isNear(n, Elements.ROBO_OTHER)) {
							res.add(n);
						}
					}
				} else {
					if (!b.isNear(n, Elements.ROBO_OTHER)) {
						res.add(n);
					}
				}
			}
		}

		// Collections.sort(res, (p1, p2) -> Integer.compare(getPriority(b, p2),
		// getPriority(b, p1)));

		return res;
	}

	public boolean isBarrierAt(Board b, Point p, Point prev) {
		if (prev != null) {

			Direction nextDir = getDir(prev, p);
			Point next = nextPoint(p, nextDir, 1);
			if (b.isAt(p, Elements.BOX) && b.isAt(next, Elements.BOX)
					|| b.isAt(p, Elements.BOX) && b.isAt(next, Elements.HOLE)
					|| b.isAt(p, Elements.HOLE) && b.isAt(next, Elements.BOX)
					|| b.isAt(p, Elements.HOLE) && b.isAt(next, Elements.HOLE)
					|| b.isAt(p, Elements.BOX) && isBarrierAt(b, next)
					|| b.isAt(p, Elements.HOLE) && isBarrierAt(b, next) ||
					// And the same check for prev->p
					b.isAt(prev, Elements.BOX) && b.isAt(p, Elements.BOX)
					|| b.isAt(prev, Elements.BOX) && b.isAt(p, Elements.HOLE)
					|| b.isAt(prev, Elements.HOLE) && b.isAt(p, Elements.BOX)
					|| b.isAt(prev, Elements.HOLE) && b.isAt(p, Elements.HOLE)
					|| b.isAt(prev, Elements.BOX) && isBarrierAt(b, p)
					|| b.isAt(prev, Elements.HOLE) && isBarrierAt(b, p)) {
				return true;
			}
		}
		return isBarrierAt(b, p);
	}

	public boolean isBarrierAt(Board b, Point p) {
		return b.isAt(p,

				// Walls.
				Elements.ANGLE_IN_LEFT, Elements.WALL_FRONT, Elements.ANGLE_IN_RIGHT, Elements.WALL_RIGHT,
				Elements.ANGLE_BACK_RIGHT, Elements.WALL_BACK, Elements.ANGLE_BACK_LEFT, Elements.WALL_LEFT,
				Elements.WALL_BACK_ANGLE_LEFT, Elements.WALL_BACK_ANGLE_RIGHT, Elements.ANGLE_OUT_RIGHT,
				Elements.ANGLE_OUT_LEFT, Elements.SPACE,

				// Laser machine is barrier as well.
				Elements.LASER_MACHINE_CHARGING_DOWN, Elements.LASER_MACHINE_CHARGING_LEFT,
				Elements.LASER_MACHINE_CHARGING_RIGHT, Elements.LASER_MACHINE_CHARGING_UP,
				Elements.LASER_MACHINE_READY_DOWN, Elements.LASER_MACHINE_READY_LEFT,
				Elements.LASER_MACHINE_READY_RIGHT, Elements.LASER_MACHINE_READY_UP

		// ? Maybe need to add more, don't know so far // FIXME
		) || b.isOutOfField(p.getX(), p.getY());
	}

	public boolean isWallAt(Board b, Point p) {
		return b.isAt(p, Elements.ANGLE_IN_LEFT, Elements.WALL_FRONT, Elements.ANGLE_IN_RIGHT, Elements.WALL_RIGHT,
				Elements.ANGLE_BACK_RIGHT, Elements.WALL_BACK, Elements.ANGLE_BACK_LEFT, Elements.WALL_LEFT,
				Elements.WALL_BACK_ANGLE_LEFT, Elements.WALL_BACK_ANGLE_RIGHT, Elements.ANGLE_OUT_RIGHT,
				Elements.ANGLE_OUT_LEFT, Elements.SPACE) || b.isOutOfField(p.getX(), p.getY());
	}

	public List<Point> neighbours(Point v, Board board, Point g) {
		List<Point> res = new ArrayList<>();

		tryToAddNeighour(v, board, res, Direction.LEFT, g);
		tryToAddNeighour(v, board, res, Direction.RIGHT, g);
		tryToAddNeighour(v, board, res, Direction.UP, g);
		tryToAddNeighour(v, board, res, Direction.DOWN, g);

		return res;
	}

	public void tryToAddNeighour(Point v, Board board, List<Point> res, Direction dir, Point g) {
		Point c = v.copy();
		c.change(dir);
		if (isValid(board, c, g)) {
			res.add(c);
		} else {
			c.change(dir);
			if (isValid(board, c, g)) {
				res.add(c);
			}
		}
	}

	public boolean isValid(Board board, Point c, Point g) {

		if (c.equals(g)) {
			return true;
		}

		// FIXME need to remove 1 point (back direction).
		Point c1 = c.copy();
		Point c2 = c.copy();
		Point c3 = c.copy();
		Point c4 = c.copy();
		c1.change(Direction.RIGHT);
		c2.change(Direction.LEFT);
		c3.change(Direction.UP);
		c4.change(Direction.DOWN);

		return !board.isBarrierAt(c.getX(), c.getY()) && !board.isHoleAt(c.getX(), c.getY())
				&& !board.isAt(c, Elements.LASER_DOWN, Elements.LASER_UP, Elements.LASER_LEFT, Elements.LASER_RIGHT)
				&& !board.isAt(c1, Elements.LASER_DOWN, Elements.LASER_UP, Elements.LASER_LEFT, Elements.LASER_RIGHT,
						Elements.LASER_MACHINE_READY_DOWN, Elements.LASER_MACHINE_READY_LEFT,
						Elements.LASER_MACHINE_READY_RIGHT, Elements.LASER_MACHINE_READY_UP)
				&& !board.isAt(c2, Elements.LASER_DOWN, Elements.LASER_UP, Elements.LASER_LEFT, Elements.LASER_RIGHT,
						Elements.LASER_MACHINE_READY_DOWN, Elements.LASER_MACHINE_READY_LEFT,
						Elements.LASER_MACHINE_READY_RIGHT, Elements.LASER_MACHINE_READY_UP)
				&& !board.isAt(c3, Elements.LASER_DOWN, Elements.LASER_UP, Elements.LASER_LEFT, Elements.LASER_RIGHT,
						Elements.LASER_MACHINE_READY_DOWN, Elements.LASER_MACHINE_READY_LEFT,
						Elements.LASER_MACHINE_READY_RIGHT, Elements.LASER_MACHINE_READY_UP)
				&& !board.isAt(c4, Elements.LASER_DOWN, Elements.LASER_UP, Elements.LASER_LEFT, Elements.LASER_RIGHT,
						Elements.LASER_MACHINE_READY_DOWN, Elements.LASER_MACHINE_READY_LEFT,
						Elements.LASER_MACHINE_READY_RIGHT, Elements.LASER_MACHINE_READY_UP);
	}

	/////////////////// DECISION 4
	/////////////////// ////////////////////////////////////////////////////////////////////////////////

	/**
	 * Run this method for connect to Server
	 */
	public static void main(String[] args) {
		connectClient(
				// paste here board page url from browser after registration
				"https://epam-botchallenge.com/codenjoy-contest/board/player/1vhpf4o0042kaihd6bsf?code=9201433781672485536",
				// and solver here
				new YourSolver(new RandomDice()));
	}

	private boolean goldMatters = true;

	private boolean wallRightReached;
	private boolean wallLeftReached;
	private boolean wallUpReached;
	private boolean wallDownReached;

	public void resetWallReaches() {
		wallDownReached = false;
		wallLeftReached = false;
		wallRightReached = false;
		wallUpReached = false;
	}

	public boolean isLeftWall(List<Point> sides) {
		for (Point p : sides) {
			if (p.getX() == 0) {
				return false;
			}
		}
		return true;
	}

	public boolean isRightWall(List<Point> sides) {
		for (Point p : sides) {
			if (p.getX() == 19) {
				return false;
			}
		}
		return true;
	}

	public boolean isUpWall(List<Point> sides) {
		for (Point p : sides) {
			if (p.getY() == 19) {
				return false;
			}
		}
		return true;
	}

	public boolean isDownWall(List<Point> sides) {
		for (Point p : sides) {
			if (p.getY() == 0) {
				return false;
			}
		}
		return true;
	}

	public boolean allSidesDiscovered() {
		return wallDownReached && wallLeftReached && wallRightReached && wallUpReached;
	}

	private int shoot;

	private int numGold;

	private int maxGold = 5;

	public Command makeDecision4(Board b) {
		if (!b.isMeAlive()) {
			resetWallReaches();
			shoot = 0;
			return Command.doNothing();
		}
		if (shoot > 0)
			shoot--;
		System.out.println("Shoot is allowed: " + (shoot == 0));
		Command res = Command.reset();
		Point me = b.getMe();
		List<Point> gold = b.getGold();
		List<Point> exits = b.getExits();
		List<Point> sides = getSides(b);
		List<Point> opps = b.getOtherHeroes();
		Collections.sort(opps, (p1, p2) -> Double.compare(me.distance(p1), me.distance(p2)));
		List<Point> perks = b.getPerks();
		Collections.sort(perks, (p1, p2) -> Double.compare(me.distance(p1), me.distance(p2)));
		List<Point> zombies = b.getZombies();
		Collections.sort(zombies, (p1, p2) -> Double.compare(me.distance(p1), me.distance(p2)));
		List<Point> starts = b.getStarts();
		Collections.sort(starts, (p1, p2) -> Double.compare(me.distance(p1), me.distance(p2)));

		Collections.shuffle(sides);

//		if (!wallUpReached) {
		Collections.sort(sides, new Comparator<Point>() {

			@Override
			public int compare(Point p1, Point p2) {
				int c = Integer.compare(p2.getY(), p1.getY());
				return c == 0 ? Double.compare(p1.getX(), p2.getX()) : c;
			}
		});
		Collections.sort(sides, (p1, p2) -> Integer.compare(p2.getY(), p1.getY()));
//		} else if (!wallLeftReached) {
//			Collections.sort(sides, (p1, p2) -> Integer.compare(p1.getX(), p2.getX()));
//		} else if (!wallRightReached) {
//			Collections.sort(sides, (p1, p2) -> Integer.compare(p2.getX(), p1.getX()));
//		} else {
//			Collections.sort(sides, (p1, p2) -> Integer.compare(p1.getY(), p2.getY()));
//		}

		if (isLeftWall(sides)) {
			wallLeftReached = true;
		}
		if (isRightWall(sides)) {
			wallRightReached = true;
		}
		if (isUpWall(sides)) {
			wallUpReached = true;
		}
		if (isDownWall(sides)) {
			wallDownReached = true;
		}

		System.out.println("Sides: " + sides);

		List<Point> path = new ArrayList<>();
		if (numGold < maxGold) {
			if (!gold.isEmpty()) {
				path = a_search(b, me, gold);
			}
		} else {
			// if (exits.isEmpty()) {
			// path = a_search(b, me, gold);
			// }
			goldMatters = false;
		}
		if (path.isEmpty() && !exits.isEmpty()) {
			path = a_search(b, me, exits);
		}

		if (path.isEmpty() && !sides.isEmpty()) {
			for (Point p : sides) {
				path = a_search(b, me, Arrays.asList(p));
				if (!path.isEmpty()) {
					break;
				}
			}

		}
		// if (path.isEmpty() && !zombies.isEmpty()) {
		// path = a_search(b, me, zombies);
		// }
		//
		// if (path.isEmpty() && !perks.isEmpty()) {
		// path = a_search(b, me, perks);
		// }
		// if (path.isEmpty() && !opps.isEmpty()) {
		// path = a_search(b, me, opps);
		// }
		// if (path.isEmpty() && !starts.isEmpty()) {
		// path = a_search(b, me, starts);
		// }
		System.out.println("Path: " + path);
		if (!path.isEmpty()) {
			Point next = path.get(1);
			Direction nextDir = getDir(me, next);
			Point jumpTarget = nextPoint(next, nextDir, 1);
			Direction oppDir1 = getDirNear(b, me, 1, Elements.ROBO_OTHER, Elements.FEMALE_ZOMBIE, Elements.MALE_ZOMBIE);
			Direction oppDir2 = getDirNear(b, me, 2, Elements.ROBO_OTHER, Elements.FEMALE_ZOMBIE, Elements.MALE_ZOMBIE);

			if (d(me, next) == 2) {

				// Handle case when there is opponent in front of me.
				Point afterMeOnPathJump = nextPoint(me, nextDir, 1);
				if (b.isAt(afterMeOnPathJump, Elements.ROBO_OTHER)) {
					if (!isOpponentDanger(me, b, 2)) {
						res = new Command("ACT(3),ACT(1)," + nextDir);
						shoot = 3;
					} else {
						res = Command.fire(nextDir);
						shoot = 3;
					}
				} else {
					res = Command.jump(nextDir);
				}
			} else if (isBulletDanger(me, b, 1, nextDir)) { // If bullet 1 step towards me.
				if (!isBulletDanger(me, b, 2, null)) {
					res = new Command("ACT(3),ACT(1)," + getOppositeDir(nextDir)); // Jump in place. If A* decides to
																					// jump 2 steps,
					// it would tell me d(me,next) = 2.
				} else { // Case when 2 bullets 1 and 2 steps from me (not necessary from one direction).
					System.out.println("2 bullets: exit");
				}
			} else if (b.isAt(next, Elements.BOX)) {
				if (isPullAllowed(b, next, nextDir)) {
					res = Command.pull(nextDir);
				} else {
					if (isJumpAllowed(b, jumpTarget)) {
						res = Command.jump(nextDir);
					} else {

						// Cas pull back.
						res = Command.pull(getOppositeDir(nextDir));
					}
				}
			} else if (isZombie(b, next) && !isBulletDanger(me, b, 2, null)) {
				res = new Command("ACT(3),ACT(1)," + nextDir);
				shoot = 3;
			} else if (next.equals(me)) { // New case when gold under me appeared.
				res = Command.jump(); // Jump on place to get gold.
			} else if (b.isAt(next, Elements.ROBO_OTHER) && !b.isAt(me, Elements.ROBO_FLYING)// Only if not flying.
			) {
				if (!isBulletDanger(me, b, 2, null)) {
					res = new Command("ACT(3),ACT(1)," + nextDir);
					shoot = 3;
				} else {
					System.out.println("bullet danger + opponent next");
				}
			} else if (oppDir1 != null && !isBulletDanger(me, b, 2, null)) {
				res = new Command("ACT(3),ACT(1)," + oppDir1);
				shoot = 3;
			}
			// else if (oppDir2 != null && !isBulletDanger(me, b, 1, null)) {
			// res = Command.fire(oppDir2);
			// afterShoot = true;
			// ndirAfterShoot = oppDir2;
			//
			// }
			else {
				res = Command.go(nextDir);
			}

			if (res.toString().equals(Command.go(nextDir).toString())
					&& b.isAt(nextPoint(me, nextDir, 1), Elements.GOLD)
					|| res.toString().equals(new Command("ACT(3),ACT(1)," + nextDir).toString())
							&& b.isAt(nextPoint(me, nextDir, 2), Elements.GOLD)
					|| res.toString().equals(Command.pull(nextDir).toString())
							&& b.isAt(nextPoint(me, nextDir, 1), Elements.GOLD)) {
				numGold++;
				if (numGold > maxGold) {
					goldMatters = false;
				}
			}
			if (res.toString().equals(Command.go(nextDir).toString())
					&& b.isAt(nextPoint(me, nextDir, 1), Elements.EXIT) ||

					res.toString().equals(Command.pull(nextDir).toString())
							&& b.isAt(nextPoint(me, nextDir, 1), Elements.EXIT)) {
				numGold = 0;
				goldMatters = true;

			}

		} else {
			System.out.println("path is empty");
		}

		if (res.toString().equals("ACT(0)") || res.toString().equals("")) {
			spawn = true;
			resetWallReaches();
			if (!path.isEmpty())
				numGold = 0;
			shoot = 0;

		}
		if (res.toString().equals(Command.reset().toString())) {
			res = new Command("ACT(3),ACT(1)," + Direction.random());
		}
		System.out.println("wu: " + wallUpReached);
		System.out.println("wl: " + wallLeftReached);
		System.out.println("wr: " + wallRightReached);
		System.out.println("wd: " + wallDownReached);

		System.out.println("num gold: " + numGold);
		System.out.println("gold matters: " + goldMatters);

		return res;
	}

	public boolean isOpponentDangerDiagonal(Board b, Point p, int steps) {
		Point me = p;
		Point dl = nextPoint(me, Direction.DOWN, steps);
		dl = nextPoint(dl, Direction.LEFT, steps);
		Point dr = nextPoint(me, Direction.DOWN, steps);
		dr = nextPoint(dr, Direction.RIGHT, steps);
		Point ul = nextPoint(me, Direction.UP, steps);
		ul = nextPoint(ul, Direction.LEFT, steps);
		Point ur = nextPoint(me, Direction.UP, steps);
		ur = nextPoint(ur, Direction.RIGHT, steps);
		if (b.isAt(dl, Elements.ROBO_OTHER) || b.isAt(dr, Elements.ROBO_OTHER) || b.isAt(ul, Elements.ROBO_OTHER)
				|| b.isAt(ur, Elements.ROBO_OTHER)) {
			return true;
		}
		return false;
	}

	private boolean spawn = false;

	public Direction turnRight(Direction dir) {
		if (dir == Direction.RIGHT) {
			return Direction.DOWN;
		} else if (dir == Direction.DOWN) {
			return Direction.LEFT;
		} else if (dir == Direction.LEFT) {
			return Direction.UP;
		} else {
			return Direction.RIGHT;
		}
	}

	public Direction turnLeft(Direction dir) {
		if (dir == Direction.RIGHT) {
			return Direction.UP;
		} else if (dir == Direction.DOWN) {
			return Direction.RIGHT;
		} else if (dir == Direction.LEFT) {
			return Direction.DOWN;
		} else {
			return Direction.LEFT;
		}
	}

	public void addNeighbor(Board b, Point v, Direction dir, List<Point> res, int steps) {

		Point c = nextPoint(v, dir, steps);

		if (!isBarrierAt(b, c) && !b.isAt(c, Elements.HOLE, Elements.ZOMBIE_START)) { // Hole is also barrier (can
																						// overcome
			// because of jump, but jump is other neighbor).
			// Bullet danger is barrier as well.

			// Special case when v is me.
			if (b.getMe().equals(v)) {
				if (isBulletDanger(c, b, 1, null)
						// || isBulletDanger(v, b, 1, dir) // Can jump in place, or jump forward.
						|| isOpponentDanger(c, b, 1) || isZombieDanger(c, b, 1)) { // Opponent/zombie accross the corner
																					// is also barrier.
					return;
				}

				// Special case when it is jump and there is an opponent diagonally.
				if (steps == 2) {
					if (isOpponentDangerDiagonal(b, v, 2)) {
						return;
					}
					if (isOpponentDangerDiagonal(b, c, 1)) {
						return;
					}

					Point middle = nextPoint(v, dir, 1);
					Point ld = nextPoint(middle, turnLeft(dir), 1);
					Point rd = nextPoint(middle, turnRight(dir), 1);

					if (b.isAt(ld, Elements.ROBO_OTHER) || b.isAt(rd, Elements.ROBO_OTHER)) {
						return;
					}
				}
			}

			// Special case box that can be pulled.
			if (b.isAt(c, Elements.BOX)) {
				if (isPullAllowed(b, c, dir)) {
					res.add(c);
				} else { // check case when pulling back
					Direction oppDir = getOppositeDir(dir);
					Point oppCell = nextPoint(v, oppDir, 1);
					if (!isBarrierAt(b, oppCell) && !b.isAt(oppCell, Elements.BOX, Elements.HOLE)
							&& !b.getStarts().contains(v)) {

						// Special case when v is me and pull back is allowed.
						if (b.getMe().equals(v)) {
							if (!isBulletDanger(v, b, 2, oppDir) && !isOpponentDanger(oppCell, b, 1)
									&& !isZombieDanger(oppCell, b, 1)) {
								res.add(c);
							}
						} else {
							res.add(c);
						}
					}
				}
			} else {

				if (b.isAt(c, Elements.EXIT)) {
					// Special case when gold is not exhausted.
					if (goldMatters) {
						if (b.getGold().isEmpty()) {
							res.add(c);
						}
					} else {
						res.add(c);
					}
				} else {
					res.add(c);
				}
			}
		}
	}

	public List<Point> neighbours(Board b, Point v) {
		List<Point> res = new ArrayList<>();

		addNeighbor(b, v, Direction.RIGHT, res, 1);
		addNeighbor(b, v, Direction.LEFT, res, 1);
		addNeighbor(b, v, Direction.UP, res, 1);
		addNeighbor(b, v, Direction.DOWN, res, 1);

		// Then try to add next cells (accessible through jump).
		if (isJumpAllowed(b, nextPoint(v, Direction.RIGHT, 2))) {
			addNeighbor(b, v, Direction.RIGHT, res, 2);
		}
		if (isJumpAllowed(b, nextPoint(v, Direction.LEFT, 2))) {
			addNeighbor(b, v, Direction.LEFT, res, 2);
		}
		if (isJumpAllowed(b, nextPoint(v, Direction.UP, 2))) {
			addNeighbor(b, v, Direction.UP, res, 2);
		}
		if (isJumpAllowed(b, nextPoint(v, Direction.DOWN, 2))) {
			addNeighbor(b, v, Direction.DOWN, res, 2);
		}

		return res;
	}

	public List<Point> neighs(Board b, Point p) {
		List<Point> res = new ArrayList<>();
		Point pr = nextPoint(p, Direction.RIGHT, 1);
		Point pl = nextPoint(p, Direction.LEFT, 1);
		Point pu = nextPoint(p, Direction.UP, 1);
		Point pd = nextPoint(p, Direction.DOWN, 1);
		if (!isBarrierAt(b, pr)) {
			res.add(pr);
		}
		if (!isBarrierAt(b, pl)) {
			res.add(pl);
		}
		if (!isBarrierAt(b, pu)) {
			res.add(pu);
		}
		if (!isBarrierAt(b, pd)) {
			res.add(pd);
		}
		return res;
	}

	// Use BFS.
	public Set<Point> allPoints(Board b) {
		Point s = b.getMe();
		HashSet<Point> res = new HashSet<>();

		Queue<Point> q = new LinkedList<>();

		Set<Point> discovered = new HashSet<>();

		discovered.add(s);
		q.add(s);

		while (!q.isEmpty()) {
			Point v = q.poll();
			res.add(v);

			for (Point w : neighs(b, v)) {
				if (!discovered.contains(w)) {
					discovered.add(w);
					q.add(w);
				}
			}
		}
		return res;
	}

	public int d(Point v, Point n) {
		int dx = Math.abs(v.getX() - n.getX());
		int dy = Math.abs(v.getY() - n.getY());
		return dx > 1 || dy > 1 ? 2 : 1;
	}

	Point bestGoal = null;

	public List<Point> a_search(Board b, Point s, List<Point> goals) {

		List<Point> res = new ArrayList<>();

		// if (goals.isEmpty()) {
		// return res;
		// }

		double bestCost = Double.POSITIVE_INFINITY;

		System.out.println("goals: " + goals);

		for (Point g : goals) {

			Map<Point, Double> fScore = new HashMap<>();
			Queue<Point> frontier = new PriorityQueue<>((p1, p2) -> Double.compare(fScore.get(p1), fScore.get(p2)));
			frontier.add(s);
			Map<Point, Point> came_from = new HashMap<>();
			Map<Point, Double> cost_so_far = new HashMap<>();
			fScore.put(s, 0.0);

			came_from.put(s, null);
			boolean found = false;
			cost_so_far.put(s, 0.0);
			while (!frontier.isEmpty()) {
				Point current = frontier.poll();
				if (current.equals(g)) {
					found = true;
					break;
				}
				for (Point next : neighbours(b, current)) {
					double new_cost = cost_so_far.get(current) + d(current, next);
					if (!cost_so_far.containsKey(next) || new_cost < cost_so_far.get(next)) {
						cost_so_far.put(next, new_cost);
						double priority = new_cost + h(b, next, g) + w(b, next);

						fScore.put(next, priority);
						frontier.add(next);
						came_from.put(next, current);
					}
				}
			}
			if (found) {
				double cost = 0;
				List<Point> path = new ArrayList<>();
				for (Point next = g; next != null && next != s; next = came_from.get(next)) {

					path.add(next);
					cost += fScore.get(next);

				}
				cost += fScore.get(s);

				if (cost < bestCost) {
					path.add(s);
					Collections.reverse(path);
					bestCost = cost;
					res = path;
					bestGoal = g;
				}
			}
			// });
		}

		// exec.shutdown();
		// try {
		// exec.awaitTermination(1, TimeUnit.DAYS);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

		System.out.println("best cost: " + bestCost);
		System.out.println("best goal: " + bestGoal);
		// System.out.println("Elements: " + b.getAt(bestGoal));

		return res;
	}

	public List<Point> dijkstra(Board b, Point s, List<Point> goals) {

		List<Point> res = new ArrayList<>();

		Set<Point> pts = allPoints(b);

		int bestCost = Integer.MAX_VALUE;

		for (Point g : goals) {

			System.out.println("goal: " + g);

			Map<Point, Integer> dists = new TreeMap<>();
			Queue<Point> q = new PriorityQueue<>((v1, v2) -> Double.compare(dists.get(v1), dists.get(v2)));

			// This map contains path between source and destination as
			// a current<-previous pairs. Therefore, to find the path from
			// source to destination, we need firstly to get destination, and
			// then previous -> previous ->...-> source.
			Map<Point, Point> prevs = new HashMap<>();

			Map<Point, Boolean> visited = new HashMap<>();

			for (Point v : pts) { // Loop through the all vertices.
				dists.put(v, Integer.MAX_VALUE); // Assume minimum distance as a maximum as of now.
				visited.put(v, false);
			}
			visited.put(s, true);
			dists.put(s, 0);
			q.add(s);

			boolean found = false;

			while (!q.isEmpty()) {
				Point u = q.poll();

				if (u.equals(g)) {
					found = true;
					break;
				}

				visited.put(u, true);

				for (Point v : neighbours(b, u)) {

					double alt = dists.get(u) + w(b, v) + d(u, v) + h(b, u, v);

					// If alt distance is less than found so far.
					if (alt < dists.get(v)) {
						dists.put(v, (int) alt); // Update minimum distance.
						prevs.put(v, u);
					}
					if (!visited.get(v)) { // Add neighbor if not visited yet.
						q.add(v);
					}
				}
			}

			if (found) {
				List<Point> path = new ArrayList<>();
				for (Point next = g; next != null && !next.equals(s); next = prevs.get(next)) {
					path.add(next);
				}
				path.add(s);
				Collections.reverse(path);
				int cost = 0;
				for (Point p : path) {
					cost += dists.get(p);
				}
				if (cost < bestCost) {
					bestCost = cost;
					res = path;
				}
			}

		} // goals

		System.out.println("best cost: " + bestCost);

		return res;
	}

	public double h(Board b, Point n, Point g) {
		return n.distance(g);
	}

	public double w(Board b, Point n) {
		double res = 0;

		if (isOpponentDanger(n, b, 1)) {
			res = 5;
		} else if (isZombieDanger(n, b, 1)) {
			res = 1;
		} else if (isBulletDanger(n, b, 1, null)) {
			res = 4;
		}
		return res;
	}

	public boolean isJumpAllowed(Board b, Point jumpTarget) {

		// First, explore target cell.
		if (!isBarrierAt(b, jumpTarget) && !b.isAt(jumpTarget, Elements.HOLE, Elements.BOX) && !isZombie(b, jumpTarget)
				&& !b.isAt(jumpTarget, Elements.ROBO_OTHER)) {

			// Next, explore dangers from bullet, zombie and opponent.
			if (!isBulletDanger(jumpTarget, b, 2, null) && !isZombieDanger(jumpTarget, b, 1)
					&& !isOpponentDanger(jumpTarget, b, 2) && !isOpponentDanger(jumpTarget, b, 1)) {
				// Case when laser machine charging
				return true; // FIXME maybe more?
			}
		}
		return false;
	}

	// Only edges are returned.
	public List<Point> getSides(Board b) {
		Set<Point> res = allPoints(b);
		res.removeIf(p -> isWallAt(b, p)
				|| p.getY() != 0 && p.getX() != 0 && p.getY() != b.size() - 1 && p.getX() != b.size() - 1);
		return new ArrayList<>(res);
	}

}