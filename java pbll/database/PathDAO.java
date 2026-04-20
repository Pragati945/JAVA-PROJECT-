package database;
import java.sql.*;
public class PathDAO 
{
    public void savePath(int src, int dest, String path, double dist) 
    {
        try 
        {
            Connection con = DBConnection.getConnection();
            if (con == null) { System.out.println("savePath: No DB connection."); return; }
            String q = "INSERT INTO shortest_paths(source_id, destination_id, path, total_distance) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(q);
            ps.setInt(1, src);
            ps.setInt(2, dest);
            ps.setString(3, path);
            ps.setDouble(4, dist);
            ps.executeUpdate();
            con.close();
        } 
        catch (Exception e) 
        {
            System.out.println(e);
        }
    }
    public void getPath(int src, int dest) 
    {
        try 
        {
            Connection con = DBConnection.getConnection();
            if (con == null) { System.out.println("getPath: No DB connection."); return; }
            String q = "SELECT * FROM shortest_paths WHERE source_id=? AND destination_id=?";
            PreparedStatement ps = con.prepareStatement(q);
            ps.setInt(1, src);
            ps.setInt(2, dest);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) 
            {
                System.out.println("Path: " + rs.getString("path"));
                System.out.println("Distance: " + rs.getDouble("total_distance"));
            } 
            else 
            {
                System.out.println("No cached path found");
            }
            con.close();
        } 
        catch (Exception e) 
        {
            System.out.println(e);
        }
    }
}