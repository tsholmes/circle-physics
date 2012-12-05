package phsyics;

import phsyics.ObjectPool.PoolObject;

public class DistanceConstraint extends ObjectPool.PoolObject {
	public static final int INITIAL_POOL_SIZE = 100;
	public static final int MAX_POOL_SIZE = INITIAL_POOL_SIZE << 6;

	public Circle c1;
	public Circle c2;
	public double mindist;
	public double maxdist;

	private World world;

	private DistanceConstraint() {
	}

	public DistanceConstraint set(DistanceConstraintDef def) {
		c1 = def.c1;
		c2 = def.c2;
		mindist = def.mindist;
		maxdist = def.maxdist;
		return this;
	}

	@Override
	public void destroy() {
		world = null;
		super.destroy();
	}

	public boolean destroyed() {
		return world == null;
	}

	public ContactPoint contact() {
		ContactPoint contact = ContactPoint.create();
		contact.c1 = c1;
		contact.c2 = c2;
		contact.normal.set(c2.position);
		contact.normal.subtractLocal(c1.position);
		contact.restitution = Math.max(c1.restitution, c2.restitution);
		double distance = contact.normal.length();
		contact.normal.normalizeLocal();
		if (distance > this.maxdist) {
			contact.normal.scaleLocal(-1.0);
			contact.penetration = distance - this.maxdist;
		} else {
			contact.penetration = this.mindist - distance;
		}
		contact.penetration *= -1;
		return contact;
	}

	private static ObjectPool pool;

	static {
		pool = new ObjectPool(INITIAL_POOL_SIZE, MAX_POOL_SIZE,
				new ObjectPool.PoolObjectFactory() {
					@Override
					public PoolObject construct() {
						return new DistanceConstraint();
					}
				});
	}

	// Static constructor
	public static DistanceConstraint create(DistanceConstraintDef def,
			World world) {
		DistanceConstraint ret = (DistanceConstraint) pool.pull();
		ret.world = world;
		return ret.set(def);
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
