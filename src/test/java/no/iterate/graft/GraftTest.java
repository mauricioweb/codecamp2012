package no.iterate.graft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import no.iterate.graft.Graft;

import org.junit.Test;

public class GraftTest {

	@Test
	public void shouldFindNodeByProperty() {
		Graft db = new Graft();
		PropertiesHolder node = db.createNode();
		node.put("id", "100mDash");
		node.put("comment", "this is a cool dash!");

		PropertiesHolder fetched = db.getNodeByProperty("id", "100mDash");

		assertNotNull(fetched);
		assertEquals("this is a cool dash!", fetched.get("comment"));
	}

	@Test
	public void tmp_shouldListenForChangesOnItsNodes() {
		Graft db = new Graft();
		PropertiesHolder node = db.createNode();

		final boolean[] updated = {false};
		NodeListener myListener = new NodeListener() {
			public void update(PropertiesHolder target) {
				updated[0] = true;
			}
		};
		node.addListener(myListener);

		node.put("id", "100mDash");

		assertTrue("Should notify its listeners", updated[0]);

		// db
		assertEquals(2, node.getListeners().size());
		assertTrue("Should have the DB as a listener always", node.getListeners().contains(db));
	}


}
