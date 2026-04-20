package model;
import java.util.List;
public class RouteResult 
{
    private List<Location> path;
    private double totalCost;
    public RouteResult(List<Location> path, double totalCost) 
    {
        this.path = path;
        this.totalCost = totalCost;
    }
    public List<Location> getPath() 
    {
        return path;
    }
    public double getTotalCost() 
    {
        return totalCost;
    }
    public double getTotalDistance() 
    {
        return totalCost;
    }
}