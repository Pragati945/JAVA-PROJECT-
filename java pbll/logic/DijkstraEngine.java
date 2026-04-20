package logic;
import model.*;
import java.util.*;
public class DijkstraEngine 
{
    public enum MetricType {
        DISTANCE, TIME, TRAFFIC
    }
    public RouteResult findShortestPath(Graph graph, Location source, Location destination) 
    {
        return findPathWithMetric(graph, source, destination, MetricType.DISTANCE);
    }
    public RouteResult findPathWithMetric(Graph graph, Location source, Location destination, MetricType metric) 
    {
        Map<Location, Double> distance = new HashMap<>();
        Map<Location, Location> previous = new HashMap<>();
        PriorityQueue<Location> pq = new PriorityQueue<>(Comparator.comparingDouble(l -> distance.getOrDefault(l, Double.MAX_VALUE)));
        for (Location location : graph.getLocations()) 
        {
            distance.put(location, Double.MAX_VALUE);
        }
        distance.put(source, 0.0);
        pq.add(source);
        while (!pq.isEmpty()) 
        {
            Location current = pq.poll();
            if (current.equals(destination))
                break;
            for (Edge edge : graph.getEdges(current)) 
            {
                Location neighbor = edge.getDestination();
                double edgeWeight = getMetricValue(edge, metric);
                double newDist = distance.get(current) + edgeWeight;
                if (newDist < distance.get(neighbor)) 
                {
                    distance.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    pq.add(neighbor);
                }
            }
        }
        List<Location> path = new ArrayList<>();
        Location step = destination;
        while (step != null) 
        {
            path.add(0, step);
            step = previous.get(step);
        }
        return new RouteResult(path, distance.get(destination));
    }
    private double getMetricValue(Edge edge, MetricType metric) 
    {
        switch (metric) 
        {
            case DISTANCE:
                return edge.getDistance();
            case TIME:
                return edge.getTime();
            case TRAFFIC:
                return edge.getTraffic();
            default:
                return edge.getDistance();
        }
    }
}