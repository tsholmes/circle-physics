package physics;

public class ObjectPool {
	private final int initialSize;
	private final int maxSize;
	private final PoolObjectFactory factory;
	private PoolObject[] pool;
	private int pullPos = 0;
	private int pushPos = 0;
	private int instances = 0;
	
	public ObjectPool(int initialSize, int maxSize, PoolObjectFactory factory) {
		this.initialSize = initialSize;
		this.maxSize = maxSize;
		this.factory = factory;
		
		pool = new PoolObject[this.initialSize];
	}
	
	private void push(PoolObject object) {
		if (pullPos == pushPos && pool[pushPos] != null) {
			PoolObject[] newpool = new PoolObject[pool.length * 2];
			if (newpool.length > maxSize) {
				throw new RuntimeException("Pool Overflow");
			}
			System.arraycopy(pool, 0, newpool, 0, pool.length);
			pullPos = 0;
			pushPos = pool.length;
			pool = newpool;
		}
		pool[pushPos] = object;
		object.setInPool(true);
		pushPos = (pushPos + 1) % pool.length;
	}
	
	public PoolObject pull() {
		if (pullPos == pushPos && pool[pullPos] == null) {
			PoolObject object = factory.construct();
			instances++;
			object.setPool(this);
			return object;
		}
		
		PoolObject object = pool[pullPos];
		pool[pullPos] = null;
		pullPos = (pullPos + 1) % pool.length;
		return object;
	}
	
	public int instances() {
		return instances;
	}
	
	public int poolSize() {
		return pool.length;
	}
	
	public int inPool() {
		int count = pushPos - pullPos;
		if (count < 0) {
			count += pool.length;
		}
		else if (count == 0 && pool[pullPos] != null) {
			count = pool.length;
		}
		return count;
	}

	public static abstract class PoolObject {
		private ObjectPool objectPool;
		private boolean inPool = false;
		
		@Override
		protected void finalize() throws Throwable {
			if (inPool) {
				System.err.println(getClass().getName() + " pool lost.");
			} else {
				System.err.println(getClass().getName() + " leak.");
				destroy();
			}
		}
		
		private void setPool(ObjectPool objectPool) {
			this.objectPool = objectPool;
		}
		
		private void setInPool(boolean inPool) {
			this.inPool = inPool;
		}
		
		public void destroy() {
			objectPool.push(this);
		}
	}
	
	public static interface PoolObjectFactory {
		public PoolObject construct();
	}
}
