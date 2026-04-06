package io.github.Chino.LodeRunner.GameInterface.Leaderboard;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**Object used to manipulate with the leaderboards of the Lode Runner database.
 * Collecting informations, adding new scores, etc...*/
public class LeaderboardHandler {
    private final String DATABASE_NAME;
    
    private Connection connection;

    public LeaderboardHandler(String databaseName, Connection connection){
        this.DATABASE_NAME = databaseName;

        this.connection = connection;
    }

    /**
     * @param ammountToFetch : the best [ammountToFetch] scores on the leaderboard, if ammountToFetch is greater than 
     * the ammount of players in the database, it get all the table
     * @return ArrayList<Object> : if function was able to fetch the informations, if not return null.
     * Even indexes are player names (including 0) / Not-even indexes are player scores
    */
    public ArrayList<Object> fetchSoloLeaderboardDatabase(int ammountToFetch){
        
        try {
            ArrayList<Object> fetchedInformations = new ArrayList<>();
            
            Statement generalStat = this.connection.createStatement();
            ResultSet allTableInformations = generalStat.executeQuery("SELECT nameOfPlayer, scoreOfPlayer FROM " + DATABASE_NAME + ".leaderboard_solo ORDER BY scoreOfPlayer DESC"); // "SELECT id, nom, prochMaintenance FROM proj.machine WHERE prochMaintenance <= \'" + dateFilter + "\'"
            
            // Collect informations until it reach the end or the ammount to fetch
            while(allTableInformations.next() && ((fetchedInformations.size() / 2) < ammountToFetch)){
                fetchedInformations.add(allTableInformations.getString(1));
                fetchedInformations.add(allTableInformations.getInt(2));
            }

            return fetchedInformations;
        } catch (SQLException e) {
            System.out.println("\nERROR GameInterface/Leaderboard/LeaderboardHandler.java: function fetchAllSoloLeaderboardDatabase catched a SQLException");
            e.printStackTrace();
        }

        return null;
    }
    /**
     * @param ammountToFetch : the best [ammountToFetch] scores on the leaderboard, if ammountToFetch is greater than 
     * the ammount of players in the database, it get all the table
     * @return ArrayList<Object> : if function was able to fetch the informations, if not return null.
     * Even indexes is a Array of the players names in the group (including 0) / Not-even indexes are the group scores
    */
    public ArrayList<Object> fetchCoopLeaderboardDatabase(int ammountToFetch){
        
        try {
            ArrayList<Object> fetchedInformations = new ArrayList<>();
            
            Statement generalStat = this.connection.createStatement();
            ResultSet allTableInformations = generalStat.executeQuery("SELECT nameOfPlayers, scoreOfTeam FROM " + DATABASE_NAME + ".leaderboard_coop ORDER BY scoreOfTeam DESC");
            
            // Collect informations until it reach the end or the ammount to fetch
            while(allTableInformations.next() && ((fetchedInformations.size() / 2) < ammountToFetch)){
                fetchedInformations.add(allTableInformations.getArray(1));
                fetchedInformations.add(allTableInformations.getInt(2));
            }

            return fetchedInformations;
        } catch (SQLException e) {
            System.out.println("\nERROR GameInterface/Leaderboard/LeaderboardHandler.java: function fetchAllSoloLeaderboardDatabase catched a SQLException");
            e.printStackTrace();
        }

        return null;
    }
}
