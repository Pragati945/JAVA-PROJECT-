package model;
public class Location
{
    private static int nextId = 1;
    private int id;
    private String name;
    public Location(int id, String name) 
    {
        this.id = id;
        this.name = name;
    }
    public Location(String name)
    {
        this(nextId++, name);
    }
    public int getId() 
    {
        return id;
    }
    public String getName() 
    {
        return name;
    }
    public boolean equals(Object obj) 
    {
        if (this == obj) 
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) 
        {
            return false;
        }
        Location other = (Location) obj;
        return this.id == other.id;
    }
    public int hashCode() 
    {
        return id;  
    }
    public String toString() 
    {
        return name;
    }
}
