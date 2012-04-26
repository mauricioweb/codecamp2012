package no.iterate.graft.client.remote;

import static org.junit.Assert.*;
import no.iterate.graft.Node;

import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

public class GraftReplicationRemoteTest {

	@Test
	public void replicateDataOverSocket() throws Exception {
		GraftServer first = GraftServer.start(1234);
		GraftServer second = GraftServer.start(1235);
		first.addReplica(second);

		try {
			GraftClient firstClient = new GraftClient(1234);
			Node created = firstClient.createNode();
			firstClient.kill();
			GraftClient secondClient = new GraftClient(1235);
			Node fetched = secondClient.getNodeById(created.getId());
			assertEquals(created.get("id"), fetched.get("id"));
		} finally {
			first.die();
			second.die();
		}
	}
	
	@Test
	public void clientConnectsToSocket() throws IOException, InterruptedException {
		GraftServer server = GraftServer.start(3456);
		try {
			Thread.sleep(10);
			GraftClient client = new GraftClient(3456);
			String response = client.ping();
			assertEquals("OK", response);
		} finally {
			server.die();
		}
	}

	@Test
	public void forceServerToRunALoop() throws IOException, InterruptedException {
		GraftServer server = GraftServer.start(3456);
		try {
			Thread.sleep(10);
			GraftClient client = new GraftClient(3456);
			String response = client.ping();
			assertEquals("OK", response);
			String second = client.ping();
			assertEquals("OK", second);
		} finally {
			server.die();
		}
	}

	@Test(expected=SocketTimeoutException.class)
	public void whatTheHellDoesSOTimeoutDo() throws IOException {
		ServerSocket s = new ServerSocket(12334);
		s.setSoTimeout(1);
		s.accept();
	}

}
