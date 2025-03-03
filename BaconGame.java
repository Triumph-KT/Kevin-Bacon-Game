import java.io.*;
import java.util.*;

/**
 * BaconGame is a beginner-friendly command-line interface for the "Kevin Bacon" problem.
 *
 * The program maintains a Graph<String,Set<String>> where each vertex is an actor's name,
 * and each edge stores the set of movies in which two actors co-starred.
 * By running BFS, we can find the "shortest path" (chain of co-starring) from any actor to
 * a chosen "center" (e.g., Kevin Bacon).
 *
 * This class supports commands like:
 *   - p <name>: print the path from <name> to the current center
 *   - u <name>: set <name> as the new center of the universe
 *   - d <low> <high>: list actors by degree (co-star count) between low and high
 *   - c <n>: compute the "best" centers by average separation
 *   - i: show how many actors are connected and the average separation to the current center
 *   - s <low> <high>: list actors by BFS separation range
 *   - q: quit the game
 *
 * @author Triumph Kia Teh, Dartmouth CS 10, Winter 2025
 */
public class BaconGame {

    // The main graph of actor relationships: vertex is an actor (String),
    // and edges store movies (Set<String>) in which they co-starred.
    protected Graph<String, Set<String>> graph;

    // A BFS tree that represents shortest paths from the "center" (root) to other actors.
    // The edges in this tree go from child -> parent.
    protected Graph<String, Set<String>> bfsTree;

    // The current "center of the universe" actor (e.g., "Kevin Bacon").
    protected String center;

    // actorIdToName: maps actor IDs (like "100") to actor names (like "Kevin Bacon").
    protected Map<String, String> actorIdToName = new HashMap<>();
    // movieIdToTitle: maps movie IDs (like "50") to movie titles (like "Footloose (1984)").
    protected Map<String, String> movieIdToTitle = new HashMap<>();
    // movieIdToActorIds: maps a movie ID to the set of actor IDs in that movie.
    protected Map<String, Set<String>> movieIdToActorIds = new HashMap<>();

    /**
     * Default constructor initializes an empty graph (AdjacencyMapGraph).
     * The BFS tree will be built later after we have chosen a center.
     */
    public BaconGame() {
        graph = new AdjacencyMapGraph<>();
    }

    /**
     * Loads actor data from a file where each line is "actorID|actorName".
     * Stores the mapping in actorIdToName.
     */
    public void loadActors(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    actorIdToName.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Loads movie data from a file where each line is "movieID|movieTitle".
     * Stores the mapping in movieIdToTitle.
     */
    public void loadMovies(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    movieIdToTitle.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Loads movie-actor data from a file where each line is "movieID|actorID".
     * Fills the movieIdToActorIds map so we know which actor IDs belong to which movie.
     */
    public void loadMovieActors(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    String movieID = parts[0];
                    String actorID = parts[1];
                    // Ensure we have a set for this movieID
                    movieIdToActorIds.putIfAbsent(movieID, new HashSet<>());
                    // Add the actorID to that movie's set
                    movieIdToActorIds.get(movieID).add(actorID);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Builds the main graph by connecting every pair of actors who co-starred in each movie.
     * For each movie, we find all actor IDs, convert them to names, and form undirected edges.
     */
    public void buildGraph() {
        // For each movie, connect all pairs of actors in that movie
        for (String movieID : movieIdToActorIds.keySet()) {
            Set<String> cast = movieIdToActorIds.get(movieID);
            String movieTitle = movieIdToTitle.get(movieID);

            // Convert the set of actorIDs to a List for easy pairwise iteration
            List<String> actorList = new ArrayList<>(cast);
            for (int i = 0; i < actorList.size(); i++) {
                for (int j = i + 1; j < actorList.size(); j++) {
                    // Convert actorID to actor name
                    String a1 = actorIdToName.get(actorList.get(i));
                    String a2 = actorIdToName.get(actorList.get(j));

                    // Insert both actors as vertices if not already present
                    graph.insertVertex(a1);
                    graph.insertVertex(a2);

                    // Retrieve existing movies on the a1-a2 edge, or create a new set
                    Set<String> label12 = graph.getLabel(a1, a2);
                    if (label12 == null) {
                        label12 = new HashSet<>();
                    }
                    // Add this movie title to the set of shared movies
                    label12.add(movieTitle);

                    // Insert undirected edges for these two actors, including the updated set
                    graph.insertUndirected(a1, a2, label12);
                }
            }
        }
    }

    /**
     * Runs BFS from the current center (this.center) and stores the resulting shortest-path tree
     * in bfsTree. The edges in bfsTree point from each child actor to its parent actor in the path.
     */
    protected void doBFS() {
        bfsTree = GraphLib.bfs(graph, center);
    }

    /**
     * Prints the shortest path from a given actor to the current center, using the bfsTree.
     * Shows the "Bacon number" (distance in co-starring steps) and each link in the path.
     *
     * @param actor The name of the actor from which we want to find a path to the center.
     */
    protected void printPath(String actor) {
        // If the actor is not even in the main graph, show an error
        if (!graph.hasVertex(actor)) {
            System.out.println(actor + " is not in the dataset.");
            return;
        }
        // If BFS tree isn't built or doesn't have the center, build it
        if (bfsTree == null || !bfsTree.hasVertex(center)) {
            doBFS();
        }
        // If the actor is already the center, just print that
        if (actor.equals(center)) {
            System.out.println(actor + " is the center of the universe.");
            return;
        }
        // If the BFS tree doesn't contain this actor, then they're not connected
        if (!bfsTree.hasVertex(actor)) {
            System.out.println(actor + " is not connected to " + center);
            return;
        }

        // Get the path from actor back to the center
        List<String> path = GraphLib.getPath(bfsTree, actor);

        // Distance is number of edges, which is path.size() - 1
        int distance = path.size() - 1;
        System.out.println(actor + "'s number is " + distance);

        // Print each "appearance chain" along the path
        for (int i = 0; i < path.size() - 1; i++) {
            String a1 = path.get(i);
            String a2 = path.get(i + 1);
            // Retrieve the set of movies in which a1 and a2 co-starred
            Set<String> movies = graph.getLabel(a1, a2);
            System.out.println(a1 + " appeared in " + movies + " with " + a2);
        }
    }

    /**
     * Lists actors whose outDegree (co-star count) is in the range [low..high].
     * Then sorts them by their numeric degree before printing.
     */
    protected void listActorsByDegree(int low, int high) {
        List<String> result = new ArrayList<>();
        for (String actor : graph.vertices()) {
            int deg = graph.outDegree(actor);
            // Check if the degree is in the specified range
            if (deg >= low && deg <= high) {
                result.add(actor + " (" + deg + " co-stars)");
            }
        }
        if (result.isEmpty()) {
            System.out.println("No actors found with degree between " + low + " and " + high + ".");
            return;
        }

        // Sort by numeric degree in ascending order
        result.sort((a, b) -> {
            int idxA = a.lastIndexOf('(');
            String subA = a.substring(idxA + 1).replaceAll("[^0-9]", "");
            int dA = Integer.parseInt(subA);

            int idxB = b.lastIndexOf('(');
            String subB = b.substring(idxB + 1).replaceAll("[^0-9]", "");
            int dB = Integer.parseInt(subB);

            return Integer.compare(dA, dB);
        });

        System.out.println("Actors with degree between " + low + " and " + high + ":");
        for (String s : result) {
            System.out.println("  " + s);
        }
    }

    /**
     * Finds the best centers by average separation. For each actor in the graph,
     * we run a BFS to get a shortest-path tree, then compute that actor's average separation.
     * Finally, we sort them in ascending order of average separation and print the top n.
     *
     * Note: This can take a while on large datasets because it does BFS for every actor.
     */
    protected void findBestCenters(int n) {
        System.out.println("Computing best centers... This may take a while.");

        Map<String, Double> avgSeparation = new HashMap<>();

        // For each actor, do a BFS from that actor, then compute the average separation
        for (String actor : graph.vertices()) {
            double avgSep = GraphLib.averageSeparation(GraphLib.bfs(graph, actor), actor);
            avgSeparation.put(actor, avgSep);
        }

        // Sort by ascending average separation
        List<String> sortedActors = new ArrayList<>(avgSeparation.keySet());
        sortedActors.sort(Comparator.comparingDouble(avgSeparation::get));

        System.out.println("Top " + n + " best centers of the universe:");
        for (int i = 0; i < Math.min(n, sortedActors.size()); i++) {
            String actor = sortedActors.get(i);
            System.out.println("  " + actor + " (Avg separation: " + avgSeparation.get(actor) + ")");
        }
    }

    /**
     * Lists actors by their BFS separation distance to the current center.
     * We first compute distances by traversing the BFS tree (child->parent edges)
     * in reverse (i.e., from parent to child). Then we pick those whose distances
     * fall between 'low' and 'high'. Finally, we sort them by distance.
     */
    protected void listBySeparationRange(int low, int high) {
        // If there's no BFS tree or the center isn't in the BFS tree, build it
        if (bfsTree == null || !bfsTree.hasVertex(center)) {
            doBFS();
        }

        // We'll store distances (center = 0) as we move down child->parent edges in reverse
        Map<String, Integer> distance = new HashMap<>();
        Queue<String> queue = new LinkedList<>();

        distance.put(center, 0);
        queue.add(center);

        // BFS in the BFS tree, using inNeighbors because each child's parent is "outNeighbor"
        // So from the parent's perspective, children are "inNeighbors".
        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentDistance = distance.get(current);

            for (String child : bfsTree.inNeighbors(current)) {
                if (!distance.containsKey(child)) {
                    distance.put(child, currentDistance + 1);
                    queue.add(child);
                }
            }
        }

        // Now gather all actors whose separation is in [low..high]
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : distance.entrySet()) {
            int sep = entry.getValue();
            if (sep >= low && sep <= high) {
                result.add(entry.getKey() + " (Separation: " + sep + ")");
            }
        }

        if (result.isEmpty()) {
            System.out.println("No actors found with separation between " + low + " and " + high + ".");
            return;
        }

        // Sort by the numeric separation
        System.out.println("Actors with separation between " + low + " and " + high + ":");
        result.sort((a, b) -> {
            int sepA = Integer.parseInt(a.replaceAll("[^0-9]", ""));
            int sepB = Integer.parseInt(b.replaceAll("[^0-9]", ""));
            return Integer.compare(sepA, sepB);
        });

        // Print them in ascending order
        for (String s : result) {
            System.out.println("  " + s);
        }
    }
}
