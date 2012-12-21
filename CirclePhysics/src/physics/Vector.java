package physics;

import physics.ObjectPool.PoolObject;

public class Vector extends ObjectPool.PoolObject {
	public static final int INITIAL_POOL_SIZE = 1000;
	public static final int MAX_POOL_SIZE = INITIAL_POOL_SIZE << 6;
	
	public double x;
	public double y;

	private Vector() {
	}

	public Vector set(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Vector set(Vector other) {
		x = other.x;
		y = other.y;
		return this;
	}

	public Vector add(Vector other) {
		return create(x + other.x, y + other.y);
	}

	public Vector addLocal(Vector other) {
		x += other.x;
		y += other.y;
		return this;
	}
	
	public Vector addLocalScaled(Vector other, double scale) {
		x += other.x * scale;
		y += other.y * scale;
		return this;
	}

	public Vector subtract(Vector other) {
		return create(x - other.x, y - other.y);
	}

	public Vector subtractLocal(Vector other) {
		x -= other.x;
		y -= other.y;
		return this;
	}

	public double dot(Vector other) {
		return x * other.x + y * other.y;
	}

	public double length() {
		return Math.sqrt(x * x + y * y);
	}

	public Vector scale(double amount) {
		return create(x * amount, y * amount);
	}

	public Vector scaleLocal(double amount) {
		x *= amount;
		y *= amount;
		return this;
	}

	public Vector normalize() {
		return scale(1. / length());
	}

	public Vector normalizeLocal() {
		return scaleLocal(1. / length());
	}

	public Vector project(Vector onto) {
		return onto.scale(dot(onto)/onto.dot(onto));
	}

	public Vector projectLocal(Vector onto) {
		onto = onto.normalize();
		set(onto.scaleLocal(dot(onto)));
		onto.destroy();
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector other = (Vector) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	private static ObjectPool pool;

	static {
		pool = new ObjectPool(INITIAL_POOL_SIZE, MAX_POOL_SIZE, new ObjectPool.PoolObjectFactory() {
			@Override
			public PoolObject construct() {
				return new Vector();
			}
		});
	}

	// Static constructors
	public static Vector create() {
		Vector ret = (Vector)pool.pull();
		return ret.set(0, 0);
	}

	public static Vector create(Vector other) {
		Vector ret = (Vector)pool.pull();
		return ret.set(other);
	}

	public static Vector create(double x, double y) {
		Vector ret = (Vector)pool.pull();
		return ret.set(x, y);
	}
	
	public static int inPool() {
		return pool.inPool();
	}
	
	public static int instances() {
		return pool.instances();
	}
	
	public static int poolSize() {
		return pool.poolSize();
	}
}