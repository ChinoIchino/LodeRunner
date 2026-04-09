package io.github.Chino.LodeRunner.GameInterface.Leaderboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
            ResultSet allTableInformations = generalStat.executeQuery("SELECT id, nameOfPlayer, scoreOfPlayer FROM " + DATABASE_NAME + ".leaderboard_solo ORDER BY scoreOfPlayer DESC, id ASC"); // "SELECT id, nom, prochMaintenance FROM proj.machine WHERE prochMaintenance <= \'" + dateFilter + "\'"
            
            
            // Collect informations until it reach the end or the ammount to fetch
            while(allTableInformations.next() && ((fetchedInformations.size() / 2) < ammountToFetch)){
                fetchedInformations.add(allTableInformations.getString(2));
                fetchedInformations.add(allTableInformations.getInt(3));
            }
            
            allTableInformations.close();
            generalStat.close();

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
            ResultSet allTableInformations = generalStat.executeQuery("SELECT id, nameOfPlayers, scoreOfTeam FROM " + DATABASE_NAME + ".leaderboard_coop ORDER BY scoreOfTeam DESC, id ASC");
            
            
            // Collect informations until it reach the end or the ammount to fetch
            while(allTableInformations.next() && ((fetchedInformations.size() / 2) < ammountToFetch)){
                fetchedInformations.add(allTableInformations.getArray(2));
                fetchedInformations.add(allTableInformations.getInt(3));
            }
            
            allTableInformations.close();
            generalStat.close();

            return fetchedInformations;
        } catch (SQLException e) {

            System.out.println("\nERROR GameInterface/Leaderboard/LeaderboardHandler.java: function fetchAllSoloLeaderboardDatabase catched a SQLException");
            e.printStackTrace();
        }

        return null;
    }

    public void addPlayerToSoloLeaderboardDatabase(String playerUsername, int scoreOfPlayer){
        try {
            Statement generalStat = this.connection.createStatement();

            ResultSet ammountOfPlayers = generalStat.executeQuery("SELECT COUNT(*) FROM " + this.DATABASE_NAME + ".leaderboard_solo");
            ammountOfPlayers.next();

            int idOfPlayer = ammountOfPlayers.getInt(1) + 1;

            ammountOfPlayers.close();
            generalStat.close();

            PreparedStatement addToTable = this.connection.prepareStatement(
                "INSERT INTO " + this.DATABASE_NAME + ".leaderboard_solo(id, nameOfPlayer, scoreOfPlayer) VALUES (" + idOfPlayer + ", ?, ?)"
            );
            
            addToTable.setString(1, playerUsername);
            addToTable.setInt(2, scoreOfPlayer);

            addToTable.executeUpdate();

            addToTable.close();
        } catch (SQLException e) {
            System.out.println("\nERROR GameInterface/Leaderboard/LeaderboardHandler.java: function addPlayerToSoloLeaderboardDatabase catched a SQLException");
            e.printStackTrace();
        }
        
    }
    public void addPlayersToCoopLeaderboardDatabase(ArrayList<String> playersNames, int scoreOfTeam){
        try {
            Statement generalStat = this.connection.createStatement();

            ResultSet ammountOfGroups = generalStat.executeQuery("SELECT COUNT(*) FROM " + this.DATABASE_NAME + ".leaderboard_coop");
            ammountOfGroups.next();

            int idOfPlayer = ammountOfGroups.getInt(1) + 1;

            ammountOfGroups.close();
            generalStat.close();

            // Prepare the statment until the array
            String statmentToPrepare = "INSERT INTO " + this.DATABASE_NAME + ".leaderboard_coop(id, nameOfPlayers, scoreOfTeam) VALUES (" + idOfPlayer + ", ARRAY[?";
            
            // Add a additional slot for every player
            for (int i = 1; i < playersNames.size(); i++) {
                statmentToPrepare += ", ?";
            }
            // end the array and add a slot for the score of the team
            statmentToPrepare += "], ?)";
            
            PreparedStatement preparedStatement = this.connection.prepareStatement(statmentToPrepare);

            // Add to the preparedStatment all the names, based on the slots created before
            for (int i = 1; i < playersNames.size() + 1; i++) {
                preparedStatement.setString(i, playersNames.get(i - 1));
            }

            preparedStatement.setInt(playersNames.size() + 1, scoreOfTeam);

            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("\nERROR GameInterface/Leaderboard/LeaderboardHandler.java: function addPlayersToCoopLeaderboardDatabase catched a SQLException");
            e.printStackTrace();
        }
        
    }
}
