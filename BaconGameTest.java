import java.util.Scanner;

/**
 * BaconGameTest is the main driver for the Kevin Bacon game.
 * It uses a BaconGame instance to load a dataset of actors and movies,
 * then provides an interactive command loop (via console) to query relationships
 * in a "Bacon" or "center of the universe" style.
 *
 * @author Triumph Kia Teh, Dartmouth CS 10, Winter 2025
 */
public class BaconGameTest {

    /**
     * The entry point of the program. This method:
     * 1) Creates a BaconGame object.
     * 2) Loads actor, movie, and movie-actor data from files.
     * 3) Builds the co-star graph.
     * 4) Sets the default "center of the universe" to Kevin Bacon.
     * 5) Repeatedly reads user commands from standard input, then
     *    dispatches those commands to the BaconGame for processing.
     *
     * @param args Command-line arguments (unused in this program).
     */
    public static void main(String[] args) {
        // Create a BaconGame instance
        BaconGame game = new BaconGame();

        // Load the larger dataset (actors, movies, and the links between them).
        game.loadActors("inputs/actors.txt");
        game.loadMovies("inputs/movies.txt");
        game.loadMovieActors("inputs/movie-actors.txt");

        // Construct the main graph of actors after data is loaded.
        game.buildGraph();

        // Set "Kevin Bacon" as the default center of the acting universe.
        game.center = "Kevin Bacon";
        System.out.println(game.center + " is now the center of the acting universe.");

        // Open a Scanner to read user input (console).
        Scanner sc = new Scanner(System.in);

        // This loop keeps running until the user types 'q'.
        while (true) {
            // Print the prompt and read the next command line.
            System.out.print("\nKevin Bacon game > ");
            String line = sc.nextLine().trim();

            // If the user pressed Enter on an empty line, provide a reminder.
            if (line.isEmpty()) {
                System.out.println("Enter a command or type 'h' for help.");
                continue;
            }

            // Split the input into "cmd" and an optional "rest".
            // For example: "p Diane Keaton" => cmd="p", rest="Diane Keaton"
            String[] parts = line.split("\\s+", 2);
            String cmd = parts[0];
            String rest = (parts.length > 1) ? parts[1].trim() : "";

            // Handle the command using a switch statement.
            switch(cmd) {
                case "q":
                    // If user types 'q', print a goodbye message and exit.
                    System.out.println("Goodbye!");
                    return;

                case "h":
                    // Print a help menu of available commands.
                    System.out.println("""
                       Commands:
                         p <name>  - find shortest path from <name> to current center
                         u <name>  - change center of the universe
                         d <low> <high> - list actors with co-stars between <low> and <high>
                         s <low> <high> - list actors sorted by separation from center
                         c <n>  - find the top <n> best 'centers of the universe'
                         i  - show number of connected actors & average separation
                         q  - quit
                    """);
                    break;

                case "p":
                    // "p <name>" => find the path from <name> to the current center
                    if (rest.isEmpty()) {
                        System.out.println("Usage: p <name>");
                    } else {
                        game.printPath(rest);
                    }
                    break;

                case "u":
                    // "u <name>" => change the center of the universe to <name>
                    if (rest.isEmpty()) {
                        System.out.println("Usage: u <actor name>");
                    } else {
                        // Verify <name> is in the graph before changing center
                        if (!game.graph.hasVertex(rest)) {
                            System.out.println(rest + " is not in the dataset. Center not changed.");
                        } else if (game.center.equals(rest)) {
                            System.out.println(rest + " is already the center of the universe.");
                        } else {
                            game.center = rest;
                            System.out.println(rest + " is now the center of the acting universe.");
                            // BFS tree will be updated on demand (when needed).
                        }
                    }
                    break;

                case "d":
                    // "d <low> <high>" => list actors by degree in the range [low..high]
                    String[] ab = rest.split("\\s+");
                    if (ab.length != 2) {
                        System.out.println("Usage: d <low> <high>");
                    } else {
                        try {
                            int low = Integer.parseInt(ab[0]);
                            int high = Integer.parseInt(ab[1]);
                            game.listActorsByDegree(low, high);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please use two integers for <low> <high>.");
                        }
                    }
                    break;

                case "s":
                    // "s <low> <high>" => list actors by BFS separation [low..high] from center
                    String[] xy = rest.split("\\s+");
                    if (xy.length != 2) {
                        System.out.println("Usage: s <low> <high>");
                    } else {
                        try {
                            int low = Integer.parseInt(xy[0]);
                            int high = Integer.parseInt(xy[1]);
                            game.listBySeparationRange(low, high);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please use two integers for <low> <high>.");
                        }
                    }
                    break;

                case "c":
                    // "c <n>" => find the top n best centers by average separation
                    if (rest.isEmpty()) {
                        System.out.println("Usage: c <n>");
                    } else {
                        try {
                            int n = Integer.parseInt(rest);
                            game.findBestCenters(n);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number for <n>.");
                        }
                    }
                    break;

                case "i":
                    // "i" => show how many actors are connected and the average separation from center
                    // Ensure we have an up-to-date BFS tree for the current center
                    game.doBFS();

                    // Count how many vertices (besides the center) appear in bfsTree
                    int conn = 0;
                    for (String v : game.bfsTree.vertices()) {
                        if (!v.equals(game.center)) {
                            conn++;
                        }
                    }
                    double avg = GraphLib.averageSeparation(game.bfsTree, game.center);

                    // Print connected count and average separation
                    System.out.println(game.center + " is connected to " + conn + " actors.");
                    System.out.println("Average separation: " + avg);
                    break;

                default:
                    // For unknown commands, prompt user to check the help menu.
                    System.out.println("Unknown command. Enter 'h' to see available commands.");
                    break;
            }
        }
    }
}
