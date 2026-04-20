import logic.*;
import model.*;
import database.*;
import java.util.*;
public class Main
{
    public static void main(String[] args) 
    {
        CityDAO cityDAO = new CityDAO();
        EdgeDAO edgeDAO = new EdgeDAO();
        Map<Integer, Location> cityMap = cityDAO.getCityMap();
        Graph graph = new Graph();
        for (Location loc : cityMap.values()) 
        {
            graph.addLocation(loc);
        }
        List<Edge> edges = edgeDAO.getAllEdges(cityMap);
        for (Edge e : edges) 
        {
            graph.addEdge(e.getSource(), e.getDestination(), e.getDistance(), e.getTime(), e.getTraffic());
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println(" Dehradun Navigation System ");
        // Pass cityDAO so new locations can be saved to DB
        graphEditMenu(scanner, graph, cityDAO);
        System.out.println("Available locations: " + graph.getLocations());
        Location source      = askLocation(scanner, graph, "source");
        Location destination = askLocation(scanner, graph, "destination");
        if (source.equals(destination)) 
        {
            System.out.println("Source and destination cannot be the same. Exiting.");
            return;
        }
        System.out.printf("Selected source: %s    destination: %s%n%n", source, destination);
        System.out.printf("Source: %s  Destination: %s%n%n", source, destination);
        int pref = askPreference(scanner);
        switch (pref) 
        {
            case 1:
                applyStrategy(new ShortestPathStrategy(), graph, source, destination, "Shortest Path (by Distance)");
                break;
            case 2:
                applyStrategy(new FastestPathStrategy(), graph, source, destination, "Fastest Path (by Time)");
                break;
            case 3:
                applyStrategy(new LeastTrafficStrategy(), graph, source, destination, "Least Traffic Path");
                break;
            case 4:
                applyStrategy(new ShortestPathStrategy(), graph, source, destination, "Shortest Path (by Distance)");
                applyStrategy(new FastestPathStrategy(), graph, source, destination, "Fastest Path (by Time)");
                applyStrategy(new LeastTrafficStrategy(), graph, source, destination, "Least Traffic Path");
                break;
            default:
                System.out.println("Invalid choice. Exiting.");
        }
    }
    private static Location askLocation(Scanner scanner, Graph graph, String role)
    {
        Location chosen = null;
        while (chosen == null) 
        {
            System.out.printf("Enter %s location name: ", role);
            String input = scanner.nextLine().trim();
            for (Location loc : graph.getLocations()) 
            {
                if (loc.getName().equalsIgnoreCase(input)) 
                {
                    chosen = loc;
                    break;
                }
            }
            if (chosen == null) 
            {
                System.out.println("Invalid location. Try again: " + graph.getLocations());
            }
        }
        return chosen;
    }
    private static void graphEditMenu(Scanner scanner, Graph graph, CityDAO cityDAO)
    {
        while (true) 
        {
            System.out.println("\nGraph configuration menu:");
            System.out.println("1 - Add location");
            System.out.println("2 - Add directed edge");
            System.out.println("3 - Done (continue to route selection)");
            System.out.print("Choose option [1-3]: ");
            String choice = scanner.nextLine().trim();
            switch (choice) 
            {
                case "1":
                    System.out.print("Enter new location name: ");
                    String name = scanner.nextLine().trim();
                    if (name.isEmpty()) 
                    {
                        System.out.println("Name cannot be empty.");
                    } 
                    else if (graph.getLocations().stream().anyMatch(l -> l.getName().equalsIgnoreCase(name))) 
                    {
                        System.out.println("Location already exists.");
                    } 
                    else 
                    {
                        // Save to database first, get the generated ID
                        int newId = cityDAO.insertCity(name);
                        if (newId == -1) 
                        {
                            System.out.println("Failed to save location to database. Location not added.");
                        } 
                        else 
                        {
                            // Add to in-memory graph with the real DB id
                            graph.addLocation(new Location(newId, name));
                            System.out.println("Location added: " + name + " (id=" + newId + ")");
                        }
                    }
                    break;
                case "2":
                    System.out.print("Enter source location name: ");
                    String from = scanner.nextLine().trim();
                    System.out.print("Enter destination location name: ");
                    String to = scanner.nextLine().trim();
                    Location src = graph.getLocations().stream().filter(l -> l.getName().equalsIgnoreCase(from)).findFirst().orElse(null);
                    Location dst = graph.getLocations().stream().filter(l -> l.getName().equalsIgnoreCase(to)).findFirst().orElse(null);
                    if (src == null || dst == null) 
                    {
                        System.out.println("Location not found. Available: " + graph.getLocations());
                        break;
                    }
                    System.out.print("Enter distance: ");
                    double dist = readDouble(scanner);
                    System.out.print("Enter time: ");
                    double time = readDouble(scanner);
                    System.out.print("Enter traffic: ");
                    double traffic = readDouble(scanner);
                    graph.addEdge(src, dst, dist, time, traffic);
                    System.out.printf("Edge added: %s -> %s (d=%.2f, t=%.2f, tr=%.2f)%n", src, dst, dist, time, traffic);
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid choice. Choose 1-3.");
            }
        }
    }
    private static double readDouble(Scanner scanner)
    {
        while (true) 
        {
            try 
            {
                return Double.parseDouble(scanner.nextLine().trim());
            } 
            catch (NumberFormatException e) 
            {
                System.out.print("Invalid number, try again: ");
            }
        }
    }
    private static int askPreference(Scanner scanner)
    {
        System.out.println("Select routing preference:");
        System.out.println("1 - Shortest path (distance)");
        System.out.println("2 - Fastest path (time)");
        System.out.println("3 - Least traffic path");
        System.out.println("4 - All strategies");
        System.out.print("Enter choice [1-4]: ");
        while (true) 
        {
            String line = scanner.nextLine().trim();
            try 
            {
                int choice = Integer.parseInt(line);
                if (choice >= 1 && choice <= 4) 
                {
                    return choice;
                }
            } catch (NumberFormatException e) {
                
            }
            System.out.print("Invalid selection. Enter choice [1-4]: ");
        }
    }
    private static void applyStrategy(RouteStrategy strategy, Graph graph, Location source, Location destination, String label)
    {
        RouteResult result = strategy.calculateRoute(graph, source, destination);
        printResult(label, result);
    }
    private static void printResult(String label, RouteResult result)
    {
        System.out.println(" " + label + " ");
        System.out.println("Path  : " + result.getPath());
        System.out.printf("Cost  : %.2f%n", result.getTotalCost());
    }
}