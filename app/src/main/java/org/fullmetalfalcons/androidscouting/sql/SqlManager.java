package org.fullmetalfalcons.androidscouting.sql;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.fullmetalfalcons.androidscouting.activities.RetrieveSettingsActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;

/**
 * Created by djher on 3/3/2016.
 */
public class SqlManager {
    private static final String USERNAME_BASE = "ridget35_";
    private static final String URL_BASE = "jdbc:mysql://ridgetopclub.com:3306/";

    public static void requestTeamNumber(Activity a, String teamNum){
        RequestTask rt = new RequestTask(a);
        try {
            ResultSet rs = rt.execute(teamNum).get();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = rs.getString(i);
                    System.out.print(columnValue + " " + rsmd.getColumnName(i));
                }
                System.out.println("");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static class RequestTask extends AsyncTask<String,Void,ResultSet>{

        private Connection c = null;
        private Activity a;

        public RequestTask(Activity a){
            this.a = a;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(a);
                String driver = "com.mysql.jdbc.Driver";
                Class.forName(driver).newInstance();
                String teamNum = sharedPref.getString(RetrieveSettingsActivity.TEAM_NUMBER_KEY,null);
                String username = USERNAME_BASE + teamNum;
                String password = sharedPref.getString(RetrieveSettingsActivity.PASSWORD_KEY,null);
                String url = URL_BASE + username;
                c = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected ResultSet doInBackground(String... params) {
            try {
                Statement s = c.createStatement();
                return s.executeQuery("SELECT * FROM team_data WHERE team_num="+params[0]);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResultSet resultSet) {
            super.onPostExecute(resultSet);
            try {
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
