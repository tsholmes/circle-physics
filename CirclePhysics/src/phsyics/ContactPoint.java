package phsyics;

import phsyics.ObjectPool.PoolObject;

public class ContactPoint extends ObjectPool.PoolObject {
	public static final int INITIAL_POOL_SIZE = 1000;
	public static final int MAX_POOL_SIZE = INITIAL_POOL_SIZE << 6;
	
	public Circle c1;
	public Circle c2;
	public Vector normal;
	
	public double penetration;	
	public double restitution;

	private ContactPoint() {
		normal = Vector.create();
	}

	public void solve() {
		solvePosition();
		solveVelocity();
	}
	
	private void solvePosition() {
		if (penetration <= 0) return;
		
		double totalIMass = c1.invMass;
		if (c2 != null) totalIMass += c2.invMass;
		
		if (totalIMass <= 0) return;
		
		Vector mPerI = normal.scale(-penetration / totalIMass);
		
		c1.position.addLocalScaled(mPerI, c1.invMass);
		if (c2 != null) c2.position.addLocalScaled(mPerI, -c2.invMass);
		
		mPerI.destroy();
	}
	
	private void solveVelocity() {
		Vector sep = c1.velocity.scale(-1);
		if (c2 != null) sep.addLocal(c2.velocity);
		
		double dot = sep.dot(normal);
		sep.destroy();
		if (dot > 0) return;
		
		dot = -dot * (1 + restitution);
		
		double totalIMass = c1.invMass;
		if (c2 != null) totalIMass += c2.invMass;
		
		if (totalIMass <= 0) return;
		
		Vector iPerI = normal.scale(dot / totalIMass);
		
		c1.velocity.addLocalScaled(iPerI, -c1.invMass);
		if (c2 != null) c2.velocity.addLocalScaled(iPerI, c2.invMass);
		
		iPerI.destroy();
	}

	private static ObjectPool pool;

	static {
		pool = new ObjectPool(INITIAL_POOL_SIZE, MAX_POOL_SIZE, new ObjectPool.PoolObjectFactory() {
			@Override
			public PoolObject construct() {
				return new ContactPoint();
			}
		});
	}

	// Static constructor
	public static ContactPoint create() {
		return (ContactPoint)pool.pull();
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
