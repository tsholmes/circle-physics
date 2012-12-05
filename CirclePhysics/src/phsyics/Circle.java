package phsyics;

import phsyics.ObjectPool.PoolObject;

public class Circle extends ObjectPool.PoolObject {
	public static final int INITIAL_POOL_SIZE = 100;
	public static final int MAX_POOL_SIZE = INITIAL_POOL_SIZE << 6;

	public final Vector position;
	public final Vector velocity;
	public final Vector force;

	public double radius;
	public double density;
	public double friction;
	public double restitution;

	private double lastrad = 0.0;
	private double lastdensity = 0.0;

	public double mass;
	public double invMass;

	private World world;

	private Circle() {
		position = Vector.create();
		velocity = Vector.create();
		force = Vector.create();
	}

	public Circle set(CircleDef def) {
		position.set(def.x, def.y);
		velocity.set(0, 0);
		radius = def.radius;
		density = def.density;
		friction = def.friction;
		restitution = def.restitution;
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

	public double penetration(Circle other) {
		Vector off = other.position.subtract(position);
		double res = radius + other.radius - off.length();
		off.destroy();
		return res;
	}

	public void prepareMass() {
		if (radius == lastrad && density == lastdensity) {
			return;
		}

		mass = Math.PI * radius * radius * density;
		if (mass == 0) {
			invMass = 0;
		} else {
			invMass = 1. / mass;
		}

		lastrad = radius;
		lastdensity = density;
	}

	public void step(double dt) {
		prepareMass();

		force.scaleLocal(invMass);
		velocity.addLocal(force);

		Vector off = velocity.scale(dt);
		position.addLocal(off);
		off.destroy();

		force.scaleLocal(0.0);
	}

	public ContactPoint contact(Circle other) {
		double penetration = penetration(other);
		if (penetration >= 0) {
			ContactPoint contact = ContactPoint.create();
			contact.penetration = penetration;
			contact.c1 = this;
			contact.c2 = other;
			contact.normal.set(other.position);
			contact.normal.subtractLocal(position);
			contact.normal.normalizeLocal();
			contact.restitution = Math.max(restitution, other.restitution);
			return contact;
		} else {
			return null;
		}
	}

	private static ObjectPool pool;

	static {
		pool = new ObjectPool(INITIAL_POOL_SIZE, MAX_POOL_SIZE, new ObjectPool.PoolObjectFactory() {
			@Override
			public PoolObject construct() {
				return new Circle();
			}
		});
	}

	// Static constructor
	public static Circle create(CircleDef def, World world) {
		Circle ret = (Circle)pool.pull();
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
