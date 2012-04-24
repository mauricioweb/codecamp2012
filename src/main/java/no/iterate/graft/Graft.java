package no.iterate.graft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.iterate.geekolympics.GeekOlympics;

public class Graft implements NodeListener {

	private static final String ID = "id";
	private final List<Graft> replicas = new ArrayList<Graft>();
	private final Collection<Node> nodes = new ArrayList<Node>();
	private final Collection<Edge> edges = new ArrayList<Edge>();
	private long nextId = 0;

	private Map<String, Collection<GeekOlympics>> subscriptions = new HashMap<String, Collection<GeekOlympics>>();

	public static List<Graft> getTwoGrafts() {
		List<Graft> grafts = new ArrayList<Graft>();
		Graft graft1 = new Graft();
		Graft graft2 = new Graft();
		graft1.addReplica(graft2);
		graft2.addReplica(graft1);
		grafts.add(graft1);
		grafts.add(graft2);

		return grafts;
	}

	public synchronized Node createNode() {
		Node node = new Node(generateId(), this);
		nodes.add(node);

		addNodeToReplicas(node);

		return node;
	}

	public Node getNodeByProperty(String property, String value) {
		for (Node each : nodes) {
			if (value.equals(each.get(property))) {
				return each;
			}
		}
		throw new IllegalStateException("Node not found - property: "
				+ property + " val : " + value);
	}

	public Edge createEdge(Node from, Node to) {
		Edge edge = addEdge(from, to);
		for (Graft replica : replicas) {
			replica.addReplicaEdge(edge.getId(), from.getId(), to.getId());
		}
		return edge;
	}

	public Collection<Edge> getEdgesFrom(String nodeId) {
		Collection<Edge> results = new ArrayList<Edge>();
		for (Edge each : edges) {
			if (each.getFrom().getId().equals(nodeId))
				results.add(each);
		}
		return results;
	}

	public void kill() {
		nodes.clear();
	}

	public void update(PropertiesHolder target) {
		Map<String, String> properties = target.getProperties();
		for (Graft each : replicas) {
			each.updateNode(properties);
		}
	}

	public void subscribe(String eventId, GeekOlympics geekOlympics) {
		Collection<GeekOlympics> gekGeekOlympics = subscriptions.get(eventId);
		if (gekGeekOlympics == null) {
			gekGeekOlympics = new ArrayList<GeekOlympics>();
			subscriptions.put(eventId, gekGeekOlympics);
		}
		gekGeekOlympics.add(geekOlympics);
	}

	public void notifySubscribers(String eventId, String message,
			String userName) {
		Collection<GeekOlympics> collection = subscriptions.get(eventId);
		if (collection == null) {
			return; // Never mind...
		}

		String eventName = getNodeById(eventId).get(ID);
		for (GeekOlympics each : collection) {
			each.notifyComment(message, eventName, userName);
		}
	}

	private Edge getEdgeByProperty(String property, String value) {
		for (Edge each : edges) {
			if (value.equals(each.get(property))) {
				return each;
			}
		}
		throw new IllegalStateException("Edge not found - property: "
				+ property + " val : " + value);
	}

	private String generateId() {
		return String.valueOf(nextId++);
	}

	private Edge addEdge(Node from, Node to) {
		return addEdgeWithId(generateId(), from, to);
	}

	private Edge addEdgeWithId(String id, Node from, Node to) {
		Edge edge = new Edge(id, this, from, to);
		edges.add(edge);
		return edge;
	}

	private void addReplicaEdge(String edgeId, String fromId, String toId) {
		Node from = getNodeById(fromId);
		Node to = getNodeById(toId);
		addEdgeWithId(edgeId, from, to);
	}

	private Node getNodeById(String nodeId) {
		if (nodeId == null)
			throw new IllegalArgumentException("id required");
		return getNodeByProperty(ID, nodeId);
	}

	private Edge getEdgeById(String nodeId) {
		if (nodeId == null)
			throw new IllegalArgumentException("id required");
		return getEdgeByProperty(ID, nodeId);
	}

	private void addReplica(Graft graft) {
		replicas.add(graft);
	}

	private void updateNode(Map<String, String> properties) {
		String targetId = properties.get(ID);
		PropertiesHolder node;
		try {
			node = getNodeById(targetId);
		} catch (IllegalStateException e) {
			node = getEdgeById(targetId);
		}
		node.setProperties(properties);

	}

	private void addNodeToReplicas(PropertiesHolder node) {
		for (Graft replica : replicas) {
			replica.addNewReplicatedNode(node.getId());
		}
	}

	private void addNewReplicatedNode(String id) {
		nodes.add(new Node(id, this));
	}
}
