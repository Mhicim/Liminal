package com.megastrata.world.graph;

import net.minecraft.core.BlockPos;
import java.util.*;

public class SpatialGraphSystem {
    public enum NodeType {
        MAIN_HUB,
        DETOUR_LOC,
        DEAD_END_VAULT,
        VERTICAL_SHAFT,
        SAFE_SHELTER
    }

    public enum EdgeType {
        PRIMARY_HIGHWAY,
        SECONDARY_BYPASS,
        LOOP_CONNECTOR,
        VERTICAL_LINK,
        HIDDEN_SERVICE_DUCT
    }

    public record GraphNode(UUID id, BlockPos pos, NodeType type, String moduleType) {}
    public record GraphEdge(UUID from, UUID to, EdgeType type, boolean requiresRestoration) {}

    private final List<GraphNode> nodes = new ArrayList<>();
    private final List<GraphEdge> edges = new ArrayList<>();

    public static SpatialGraphSystem buildComplexGraph(long complexSeed, BlockPos complexCenter) {
        SpatialGraphSystem graph = new SpatialGraphSystem();
        Random rand = new Random(complexSeed);

        GraphNode hub = new GraphNode(UUID.randomUUID(), complexCenter, NodeType.MAIN_HUB, "transport_terminal");
        GraphNode shelter = new GraphNode(UUID.randomUUID(), complexCenter.offset(64, 0, 64), NodeType.SAFE_SHELTER, "dispatcher_shelter");
        graph.nodes.add(hub);
        graph.nodes.add(shelter);
        graph.edges.add(new GraphEdge(hub.id(), shelter.id(), EdgeType.SECONDARY_BYPASS, false));

        GraphNode prevNode = hub;
        for (int i = 1; i <= 6; i++) {
            int dx = (rand.nextInt(7) - 3) * 256;
            int dz = (rand.nextInt(7) - 3) * 256;
            int dy = (i % 3 == 0) ? (rand.nextBoolean() ? 400 : -400) : 0;

            NodeType type = switch (i) {
                case 2 -> NodeType.DEAD_END_VAULT;
                case 3, 6 -> NodeType.VERTICAL_SHAFT;
                case 4 -> NodeType.SAFE_SHELTER;
                default -> NodeType.DETOUR_LOC;
            };

            GraphNode nextNode = new GraphNode(UUID.randomUUID(), prevNode.pos().offset(dx, dy, dz), type, "sector_node_" + i);
            graph.nodes.add(nextNode);

            EdgeType edgeType = (dy != 0) ? EdgeType.VERTICAL_LINK : EdgeType.PRIMARY_HIGHWAY;
            graph.edges.add(new GraphEdge(prevNode.id(), nextNode.id(), edgeType, dy != 0 && rand.nextBoolean()));

            if (i == 5) {
                graph.edges.add(new GraphEdge(nextNode.id(), hub.id(), EdgeType.LOOP_CONNECTOR, false));
            } else if (i == 6) {
                graph.edges.add(new GraphEdge(nextNode.id(), shelter.id(), EdgeType.HIDDEN_SERVICE_DUCT, true));
            }
            prevNode = nextNode;
        }
        return graph;
    }

    public List<GraphNode> getNodes() { return nodes; }
    public List<GraphEdge> getEdges() { return edges; }
}
