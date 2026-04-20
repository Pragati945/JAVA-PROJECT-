package database;
import java.sql.*;
import java.util.*;
import model.Location;
public class CityDAO 
{
    public Map<Integer, Location> getCityMap() 
    {
        Map<Integer, Location> map = new HashMap<>();
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM cities")) 
            {
            while (rs.next()) 
            {
                int id      = rs.getInt("id");
                String name = rs.getString("name");
                map.put(id, new Location(id, name));
            }
        } 
        catch (Exception e) 
        {
            System.out.println("City load error: " + e.getMessage());
        }
        return map;
    }

    // Returns the new auto-generated ID, or -1 on failure
    public int insertCity(String name) 
    {
        String sql = "INSERT INTO cities (name) VALUES (?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) 
        {
            ps.setString(1, name);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) 
            {
                return keys.getInt(1);
            }
        } 
        catch (Exception e) 
        {
            System.out.println("City insert error: " + e.getMessage());
        }
        return -1;
    }
}