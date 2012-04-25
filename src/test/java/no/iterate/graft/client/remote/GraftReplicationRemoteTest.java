package no.iterate.graft.client.remote;

import static org.junit.Assert.*;
import no.iterate.graft.Node;

import org.junit.Test;

public class GraftReplicationRemoteTest {

	@Test
	public void replicateDatatOverSocket() throws Exception {
		GraftClient client = new GraftClient();
		GraftServer first = GraftServer.start(1234);
		GraftServer second = GraftServer.start(1235);
		client.connectTo(first);
		Node created = client.createNode();
		client.kill();
		client.connectTo(second);
		Node fetched = client.getNodeById(created.getId());
		assertEquals("value", fetched.get("key"));
	}

}
