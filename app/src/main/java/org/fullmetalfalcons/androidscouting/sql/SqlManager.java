package org.fullmetalfalcons.androidscouting.sql;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.fullmetalfalcons.androidscouting.activities.DHActivity;
import org.fullmetalfalcons.androidscouting.activities.RetrieveDataActivity;
import org.fullmetalfalcons.androidscouting.activities.RetrieveSettingsActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;

/**
 * Handles accessing remote SQL Data
 *
 * Created by djher on 3/3/2016.
 */
public class SqlManager {
    private static final String USERNAME_BASE = "ridget35_";
    private static final String URL_BASE = "jdbc:mysql://ridgetopclub.com:3306/";

    public static void requestTeamNumber(DHActivity a, String teamNum){
        RequestTask rt = new RequestTask(a);
        try {
            ResultSet rs = rt.execute(teamNum).get();
            if (rs!=null){
                if (!rs.next()){
                    RetrieveDataActivity.setResponseString("NoReadTeam");
                } else {
                    rs.beforeFirst();
                    RetrieveDataActivity.setResultSet(rs);
                }
            } else {
                RetrieveDataActivity.setResponseString("cancel");
            }
        } catch (InterruptedException e) {
            a.sendError("Interrupted while requesting team", false, e);
        } catch (ExecutionException e) {
            a.sendError("Erorr while requesting team",false, e);
        } catch (SQLException e) {
            a.sendError("SQL Error while requesting team",false,e);
        }

    }

    public static void searchForTeams(DHActivity a, String type, String column, String operator, String value) {
        SearchTask searchTask = new SearchTask(a);
        try {
            ResultSet rs = searchTask.execute(type,column,operator,value).get();
            if (rs!=null){
                if (!rs.next()){
                    RetrieveDataActivity.setResponseString("NoSearchResult");
                } else {
                    rs.beforeFirst();
                    RetrieveDataActivity.setResultSet(rs);
                }
            } else {
                RetrieveDataActivity.setResponseString("cancel");
            }
        } catch (InterruptedException e) {
            a.sendError("Interrupted while searching for teams", false, e);
        } catch (ExecutionException e) {
            a.sendError("Erorr while searching for teams", false, e);
        } catch (SQLException e) {
            a.sendError("SQL Error while searching for teams", false, e);
        }
    }

    private static class RequestTask extends AsyncTask<String,Void,ResultSet>{

        private Connection c = null;
        private final DHActivity a;

        public RequestTask(DHActivity a){
            this.a = a;
        }


        @Override
        protected ResultSet doInBackground(String... params) {
            try {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(a);
                String driver = "com.mysql.jdbc.Driver";
                Class.forName(driver).newInstance();
                String teamNum = sharedPref.getString(RetrieveSettingsActivity.TEAM_NUMBER_KEY,null);
                String username = USERNAME_BASE + teamNum;
                String password = sharedPref.getString(RetrieveSettingsActivity.PASSWORD_KEY,null);
                String url = URL_BASE + username;
                c = DriverManager.getConnection(url, username, password);
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM team_data WHERE team_num=" + params[0]);
                c.close();
                return rs;
            } catch (SQLException e) {
                if (e.getSQLState().equals("28000")){
                    a.sendError("Invalid Team Number/Password combination",false);
                }
                System.out.println(e.getErrorCode() + ":" + e.getSQLState());
                e.printStackTrace();

            } catch (ClassNotFoundException e) {
                a.sendError("SQLite drive not found",false,e);
            } catch (InstantiationException e) {
                a.sendError("Could not instantiate SQLite driver", false, e);
            } catch (IllegalAccessException e) {
                a.sendError("Illegal Access while requesting team", false, e);
            }
            return null;
        }

    }

    private static class SearchTask extends AsyncTask<String,Void,ResultSet>{

        private Connection c = null;
        private final DHActivity a;

        public SearchTask(DHActivity a){
            this.a = a;
        }


        @Override
        protected ResultSet doInBackground(String... params) {
            try {
                String type = params[0];
                String column = params[1];
                String operator = params[2];
                String value = params[3];

                switch (operator) {
                    case "≥":
                        operator = ">=";
                        break;
                    case "≤":
                        operator = "<=";
                        break;
                    case "≠":
                        operator = "!=";
                        break;
                }

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(a);
                String driver = "com.mysql.jdbc.Driver";
                Class.forName(driver).newInstance();
                String teamNum = sharedPref.getString(RetrieveSettingsActivity.TEAM_NUMBER_KEY,null);
                String username = USERNAME_BASE + teamNum;
                String password = sharedPref.getString(RetrieveSettingsActivity.PASSWORD_KEY,null);
                String url = URL_BASE + username;
                c = DriverManager.getConnection(url, username, password);
                Statement s = c.createStatement();
                String query = String.format("SELECT team_num, num_matches, %s FROM team_data WHERE %s %s %s",
                        column,
                        type.equals("Raw Value") ? column : column + "/ num_matches",
                        operator,
                        value);
                ResultSet rs = s.executeQuery(query);
                c.close();
                return rs;
            } catch (SQLException e) {
                if (e.getSQLState().equals("28000")){
                    a.sendError("Invalid Team Number/Password combination",false);
                }
                e.printStackTrace();

            }  catch (ClassNotFoundException e) {
                a.sendError("SQLite drive not found", false, e);
            } catch (InstantiationException e) {
                a.sendError("Could not instantiate SQLite driver", false, e);
            } catch (IllegalAccessException e) {
                a.sendError("Illegal Access while searching for teams", false, e);
            }
            return null;
        }

    }
}
