package logic;
import model.*;
public interface RouteStrategy 
{
    RouteResult calculateRoute(Graph graph, Location source, Location destination);
}