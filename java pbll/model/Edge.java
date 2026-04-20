package model;
public class Edge 
{
    private Location source;
    private Location destination;
    private double distance;
    private double time;
    private double traffic;
    public Edge(Location source, Location destination, double distance, double time, double traffic) 
    {
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.time = time;
        this.traffic = traffic;
    }
    public Location getSource() 
    {
        return source;
    }
    public Location getDestination() 
    {
        return destination;
    }
    public double getDistance() 
    {
        return distance;
    }
    public double getTime() 
    {
        return time;
    }
    public double getTraffic() 
    {
        return traffic;
    }
}
