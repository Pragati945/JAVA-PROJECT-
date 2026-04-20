package database;
import java.sql.*;
public class DBConnection 
{
    static final String URL  = "jdbc:mysql://localhost:3306/route_db?useSSL=false&allowPublicKeyRetrieval=true";
    static final String USER = "root";
    static final String PASS = "";
    public static Connection getConnection() 
    {
        try 
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            System.out.println("Connection Error: " + e.getMessage());
            return null;
        }
    }
}