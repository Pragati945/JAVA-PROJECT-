package database;
import java.sql.*;
import java.util.*;
import model.*;
public class EdgeDAO 
{
    public List<Edge> getAllEdges(Map<Integer, Location> cityMap) 
    {
        List<Edge> edges = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM edges")) 
            {
            while (rs.next()) 
            {
                Location src = cityMap.get(rs.getInt("source_id"));
                Location dst = cityMap.get(rs.getInt("destination_id"));
                if (src == null || dst == null) continue;
                edges.add(new Edge(src, dst,rs.getDouble("distance"),rs.getDouble("time"),rs.getDouble("traffic")));
            }
        } 
        catch (Exception e) 
        {
            System.out.println("Edge load error: " + e.getMessage());
        }
        return edges;
    }
}