package physics;


public class World {
	private Circle[] circles;
	private Circle[] _circles;
	private int circleCount;
	
	private DistanceConstraint[] constraints;
	private DistanceConstraint[] _constraints;
	private int constraintCount;

	private ContactPoint[] contacts;
	private int contactCount;

	public final Vector gravity;
	public final double[] bounds = { 0, 0, 640, 480 };
	
	public final double[][] contactPositions = new double[100000][2];
	public int contactPosCount;

	public World() {
		circles = new Circle[Circle.MAX_POOL_SIZE];
		_circles = new Circle[Circle.MAX_POOL_SIZE];
		circleCount = 0;
		
		constraints = new DistanceConstraint[DistanceConstraint.MAX_POOL_SIZE];
		_constraints = new DistanceConstraint[DistanceConstraint.MAX_POOL_SIZE];
		constraintCount = 0;

		contacts = new ContactPoint[ContactPoint.MAX_POOL_SIZE];
		contactCount = 0;

		gravity = Vector.create();
	}

	public Circle createCircle(CircleDef def) {
		Circle circ = Circle.create(def, this);
		circles[circleCount++] = circ;
		return circ;
	}
	
	public DistanceConstraint createConstraint(DistanceConstraintDef def) {
		DistanceConstraint constraint = DistanceConstraint.create(def, this);
		constraints[constraintCount++] = constraint;
		return constraint;
	}

	public void step(double dt) {
		// destroy
		{
			int newCount = 0;
			for (int i = 0; i < circleCount; i++) {
				if (!circles[i].destroyed()) {
					_circles[newCount++] = circles[i];
				}
			}
			Circle[] tmp = circles;
			circles = _circles;
			_circles = tmp;
			circleCount = newCount;
		}
		{
			int newCount = 0;
			for (int i = 0; i < constraintCount; i++) {
				if (!constraints[i].destroyed()) {
					_constraints[newCount++] = constraints[i];
				}
			}
			DistanceConstraint[] tmp = constraints;
			constraints = _constraints;
			_constraints = tmp;
			constraintCount = newCount;
		}

		for (int i = 0; i < circleCount; i++) {
			circles[i].velocity.addLocalScaled(gravity, dt);
			circles[i].step(dt);
		}

		contactPosCount = 0;
		for (int pass = 0; pass < 10; pass++) {
			contactCount = ContactGenerator.generateContacts(_circles,
					circleCount, contacts, bounds);
			for (int i = 0; i < constraintCount; i++) {
				contacts[contactCount++] = constraints[i].contact();
			}
			for (int i = 0; i < contactCount; i++) {
				Vector pos = contacts[i].c1.position.scale(1.0).addLocalScaled(contacts[i].normal, contacts[i].c1.radius);
				contactPositions[contactPosCount][0] = pos.x;
				contactPositions[contactPosCount++][1] = pos.y;
				pos.destroy();
			}
			ContactGenerator.resolveContact(contacts, contactCount);
			for (int i = 0; i < contactCount; i++) {
				contacts[i].destroy();
			}
		}
		contactCount = 0;
		for (int i = 0; i < constraintCount; i++) {
			contacts[contactCount++] = constraints[i].contact();
		}
		ContactGenerator.resolveContact(contacts, contactCount);
		for (int i = 0; i < contactCount; i++) {
			contacts[i].destroy();
		}
	}
}