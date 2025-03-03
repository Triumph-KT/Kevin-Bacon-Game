import java.util.*;

/**
 * GraphLib is a simple library of static methods that use BFS (Breadth-First Search)
 * on a generic Graph<V, E> to perform various analyses:
 * 1) bfs(...) builds a new BFS tree (child->parent edges) from a given source.
 * 2) getPath(...) reconstructs a path from any vertex back to the source in that BFS tree.
 * 3) missingVertices(...) finds all vertices that aren't reached by the BFS.
 * 4) averageSeparation(...) computes the average distance from the source to all reachable vertices.
 *
 * @author Triumph Kia Teh, Dartmouth CS 10, Winter 2025
 */
public class GraphLib {

    /**
     * Performs a BFS (Breadth-First Search) starting from 'source' in the graph 'g'.
     * Returns a new Graph which is the BFS tree with edges directed child->parent.
     * If 'source' doesn't exist in 'g', returns an empty tree.
     *
     * @param g       the original graph on which BFS is performed
     * @param source  the starting vertex for BFS
     * @param <V>     the vertex type
     * @param <E>     the edge label type
     * @return        a new Graph that represents the BFS tree (child->parent edges)
     */
    public static <V, E> Graph<V, E> bfs(Graph<V, E> g, V source) {
        // Create an empty adjacency map graph for the BFS tree
        Graph<V, E> tree = new AdjacencyMapGraph<>();

        // If the source is not even in the original graph, return an empty BFS tree
        if (!g.hasVertex(source)) {
            return tree;
        }

        // Keep track of each node's parent and distance (for reference if needed)
        Map<V, V> parent = new HashMap<>();
        Map<V, Integer> distance = new HashMap<>();

        // The source has no parent, and its distance is 0
        parent.put(source, null);
        distance.put(source, 0);
        tree.insertVertex(source);

        // Use a queue for BFS
        Queue<V> queue = new LinkedList<>();
        queue.add(source);

        // Standard BFS loop
        while (!queue.isEmpty()) {
            V current = queue.remove();
            int currentDepth = distance.get(current);

            // Explore all out-neighbors of 'current' in the original graph
            for (V neighbor : g.outNeighbors(current)) {
                // If 'neighbor' was never seen before, visit and record
                if (!parent.containsKey(neighbor)) {
                    parent.put(neighbor, current);
                    distance.put(neighbor, currentDepth + 1);

                    // Insert this neighbor as a vertex in the BFS tree
                    tree.insertVertex(neighbor);

                    // Create an edge from neighbor->current (child->parent) in the BFS tree
                    // Copy the label (movies, etc.) from the original graph
                    tree.insertDirected(neighbor, current, g.getLabel(current, neighbor));

                    queue.add(neighbor);
                }
            }
        }
        return tree;
    }

    /**
     * Reconstructs the path from vertex 'v' back to the source in the BFS tree.
     * The BFS tree has edges child->parent. Therefore, the parent is found by checking
     * the outNeighbors of each child (there should be exactly one outNeighbor: the parent).
     *
     * @param tree  the BFS tree created by bfs(...)
     * @param v     the vertex from which we want the path back to the root
     * @param <V>   the vertex type
     * @param <E>   the edge label type
     * @return      a List of vertices from the BFS tree's root to 'v' (empty if 'v' not in tree)
     */
    public static <V,E> List<V> getPath(Graph<V,E> tree, V v) {
        List<V> path = new ArrayList<>();

        // If 'v' is not in the BFS tree, return an empty list
        if (!tree.hasVertex(v)) {
            return path;
        }

        // We'll move from child -> parent by following outNeighbors until there's no parent
        V current = v;
        while (true) {
            path.add(current);

            // In the BFS tree with child->parent edges, outNeighbors(child) should have exactly 1 parent
            Iterator<V> outIt = tree.outNeighbors(current).iterator();

            // If there's no outNeighbor, we've reached the root (the BFS source)
            if (!outIt.hasNext()) {
                break;
            }

            // Otherwise, move up to the parent
            current = outIt.next();
        }

        // We built the path backwards (v ... -> root), so reverse it
        Collections.reverse(path);
        return path;
    }

    /**
     * missingVertices(...) returns the set of vertices that appear in 'graph' but not in 'subgraph'.
     * Typically used to find all vertices that weren't reached by a BFS tree.
     *
     * @param graph     the original graph
     * @param subgraph  the BFS tree or another subgraph
     * @param <V>       the vertex type
     * @param <E>       the edge label type
     * @return          a set of vertices from 'graph' that are missing in 'subgraph'
     */
    public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph) {
        Set<V> missing = new HashSet<>();
        for (V v : graph.vertices()) {
            if (!subgraph.hasVertex(v)) {
                missing.add(v);
            }
        }
        return missing;
    }

    /**
     * averageSeparation(...) computes the average distance-from-root in a BFS tree,
     * ignoring the root itself. The BFS tree has edges child->parent, so to calculate
     * distance, we move "down" the tree using inNeighbors (i.e., parent->child in reversed sense).
     *
     * @param tree   the BFS tree (child->parent edges)
     * @param root   the root (source) of this BFS tree
     * @param <V>    the vertex type
     * @param <E>    the edge label type
     * @return       the average distance from 'root' to all other reachable vertices.
     *               Returns 0.0 if there are no other vertices in the BFS tree.
     */
    public static <V,E> double averageSeparation(Graph<V,E> tree, V root) {
        Map<V, Integer> distance = new HashMap<>();
        Queue<V> queue = new LinkedList<>();

        // The root starts at distance 0
        distance.put(root, 0);
        queue.add(root);

        int totalDistance = 0;
        int count = 0;

        // BFS in the BFS tree, but we traverse from parent->child by using inNeighbors
        while (!queue.isEmpty()) {
            V current = queue.poll();
            int currentDepth = distance.get(current);

            // Because the BFS tree edges are child->parent,
            // the children of 'current' are inNeighbors(current).
            for (V child : tree.inNeighbors(current)) {
                if (!distance.containsKey(child)) {
                    distance.put(child, currentDepth + 1);
                    totalDistance += (currentDepth + 1);
                    count++;
                    queue.add(child);
                }
            }
        }

        // If 'count' is 0, it means no other vertices were reachable or exist in the tree
        if (count == 0) {
            return 0.0;
        } else {
            return (double) totalDistance / count;
        }
    }
}
