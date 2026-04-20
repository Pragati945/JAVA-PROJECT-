package logic;
import model.*;
public class LeastTrafficStrategy implements RouteStrategy 
{
    public RouteResult calculateRoute(Graph graph, Location source, Location destination) 
    {
        DijkstraEngine engine = new DijkstraEngine();
        return engine.findPathWithMetric(graph, source, destination, DijkstraEngine.MetricType.TRAFFIC);
    }
}