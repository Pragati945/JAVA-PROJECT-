package model;
import java.util.*;
public class Graph 
{
    private Map<Location, List<Edge>> adjacencyList;
    public Graph() 
    {
        adjacencyList = new HashMap<>();
    }
    public void addLocation(Location location) 
    {
        adjacencyList.putIfAbsent(location, new ArrayList<>());
    }
    public void addEdge(Location source, Location destination, double distance, double time, double traffic) 
    {
        if (source == null || destination == null) 
        {
            System.out.println("Warning: Cannot add edge with null source or destination");
            return;
        }
        if (!adjacencyList.containsKey(source)) 
        {
            System.out.println("Warning: Source location not found in graph: " + source);
            return;
        }
        Edge edge = new Edge(source, destination, distance, time, traffic);
        adjacencyList.get(source).add(edge);
    }
    public List<Edge> getEdges(Location location) 
    {
        return adjacencyList.getOrDefault(location, new ArrayList<>());
    }
    public Set<Location> getLocations() 
    {
        return adjacencyList.keySet();
    }
}