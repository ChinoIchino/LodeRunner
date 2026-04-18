package io.github.Chino.LodeRunner.GameInterface.Leaderboard;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LeaderboardCreator {
    private Connection connection = null;

    private final String DATABASE_NAME = "loderunnerdatabase";

    /**
     * Constructor of LeaderboardCreator, used to establish connection with jar and the sql database
     */
    public LeaderboardCreator(){
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection("jdbc:postgresql://kafka.iem/kb483753", "kb483753", "kb483753");
            doesCorrectDatabaseExist(DATABASE_NAME);
            System.out.println("Everything went fine!");
        } catch (SQLException e) {
            System.out.println("\nERROR GameInterface/Leaderboard/LeaderboardCreator.java: Connection wasn't able to be established");
            e.printStackTrace();
        } 
        catch(ClassNotFoundException cnfe){
            System.out.println("\nERROR GameInterface/Leaderboard/LeaderboardCreator.java: Didn't found the postgresql jar");
            cnfe.printStackTrace();
        }
    }

    /**
     * @return LeaderboardHandler, if constructor was able to fetch a connection, if not return null
    */
    public LeaderboardHandler createLeaderboardHandler(){
        if(connection != null){
            return new LeaderboardHandler(DATABASE_NAME, this.connection);
        }
        return null;
    }

    private void doesCorrectDatabaseExist(String schemaName) throws SQLException{
        DatabaseMetaData metaDataOfDatabase = this.connection.getMetaData();
        ResultSet stat = metaDataOfDatabase.getSchemas();

        boolean correctSchemaExist = false;
        while(stat.next()){
            if(stat.getString(1).equals(schemaName)){
                correctSchemaExist = true;
                break;
            }
        }
        
        if(!correctSchemaExist){
            throw new SQLException();
        }
    }
}
