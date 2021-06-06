import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JFrame;

public class DBConfigure {
	
	public static void createNewDatabase(String fileName) {

        String url = "jdbc:sqlite:" + fileName;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
	
	public static void createNewTable1(String fileName) {
        String url = "jdbc:sqlite:" + fileName;
        String sql = "CREATE TABLE IF NOT EXISTS games (\n"
                + "	name TEXT NOT NULL PRIMARY KEY,\n"
                + "	game TEXT NOT NULL,\n"
                + "	timeleft INTEGER,\n"
                + "	minesleft INTEGER\n"
                + ");";
        
        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
	
	public static void createNewTable2(String fileName) {
        String url = "jdbc:sqlite:" + fileName;
        String sql = "CREATE TABLE IF NOT EXISTS scores (\n"
                + "	id INTEGER PRIMARY KEY,\n"
                + "	name TEXT NOT NULL,\n"
                + "	score INTEGER\n"
                + ");";
        
        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
	
	public static void main(String[] args) {  
		createNewDatabase("minesweeper.db");
		createNewTable1("minesweeper.db");
		createNewTable2("minesweeper.db");
	}
}
