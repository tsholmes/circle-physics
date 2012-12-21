package physics;

import java.util.Arrays;
import java.util.Comparator;

public class ContactGenerator {
	public static int generateContacts(Circle[] circles, int circleCount,
			ContactPoint[] contacts, double[] bounds) {
		int contactCount = 0;
		Arrays.sort(circles, 0, circleCount, leftComparator);
		for (int i = 0; i < circleCount; i++) {
			Circle c = circles[i];
			if (c.position.x - c.radius <= bounds[0]) {
				ContactPoint contact = ContactPoint.create();
				contact.c1 = c;
				contact.c2 = null;
				contact.penetration = bounds[0] + c.radius - c.position.x;
				contact.restitution = c.restitution;
				contact.normal.set(-1, 0);
				contacts[contactCount++] = contact;
			}
			if (c.position.y - c.radius <= bounds[1]) {
				ContactPoint contact = ContactPoint.create();
				contact.c1 = c;
				contact.c2 = null;
				contact.penetration = bounds[1] + c.radius - c.position.y;
				contact.restitution = c.restitution;
				contact.normal.set(0, -1);
				contacts[contactCount++] = contact;
			}
			if (c.position.x + c.radius >= bounds[2]) {
				ContactPoint contact = ContactPoint.create();
				contact.c1 = c;
				contact.c2 = null;
				contact.penetration = c.position.x + c.radius - bounds[2];
				contact.restitution = c.restitution;
				contact.normal.set(1, 0);
				contacts[contactCount++] = contact;
			}
			if (c.position.y + c.radius >= bounds[3]) {
				ContactPoint contact = ContactPoint.create();
				contact.c1 = c;
				contact.c2 = null;
				contact.penetration = c.position.y + c.radius - bounds[3];
				contact.restitution = c.restitution;
				contact.normal.set(0, 1);
				contacts[contactCount++] = contact;
			}
		}
		for (int i = 0; i < circleCount; i++) {
			for (int j = i + 1; j < circleCount; j++) {
				if (circles[j].position.x - circles[j].radius > circles[i].position.x + circles[i].radius) {
					break;
				}
				ContactPoint contact = circles[i].contact(circles[j]);
				if (contact != null) {
					contacts[contactCount++] = contact;
				}
			}
		}
		return contactCount;
	}

	public static void resolveContact(ContactPoint[] contacts, int contactCount) {
		Arrays.sort(contacts, 0, contactCount, penetrationComparator);
		for (int i = 0; i < contactCount; i++) {
			contacts[i].solve();
		}
	}

	private static Comparator<ContactPoint> penetrationComparator = new Comparator<ContactPoint>() {
		@Override
		public int compare(ContactPoint o1, ContactPoint o2) {
			return -Double.compare(o1.penetration, o2.penetration);
		}
	};

	private static Comparator<Circle> leftComparator = new Comparator<Circle>() {

		@Override
		public int compare(Circle o1, Circle o2) {
			return Double.compare(o1.position.x - o1.radius, o2.position.x
					- o2.radius);
		}

	};
}
