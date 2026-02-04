package shredders;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Graph<T> {
    private Map<T, List<T>> adjVertices = new HashMap<>();
    private int numVertices = 0;

    public void addVertex(T data) {
        adjVertices.putIfAbsent(data, new ArrayList<>());
        numVertices++;
    }

    public void addEdge(T source, T destination) {
        // Ensure both vertices exist
        addVertex(source);
        addVertex(destination);
        // Add edge from source to destination (directed graph)
        adjVertices.get(source).add(destination);
        // For an undirected graph, also add edge from destination to source:
        // adjVertices.get(destination).add(source); 
    }

    public List<T> getAdjVertices(T data) {
        return adjVertices.get(data);
    }

    //bfs example, I thought itd be helpful to see but wont be used
    /*public ArrayList<T> traverse_bfs(T start) {
        Set<T> visited = new HashSet<>();
        ArrayDeque<T> queue = new ArrayDeque<>();
        ArrayList<T> traversal = new ArrayList<>();

        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            T vertex = queue.removeFirst();
            traversal.add(vertex);

            List<T> neighbors = adjVertices.get(vertex);
            if (neighbors != null) {
                for (T neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return traversal;
    } */
}

