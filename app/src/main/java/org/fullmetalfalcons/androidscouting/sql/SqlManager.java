package org.fullmetalfalcons.androidscouting.sql;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;

import org.fullmetalfalcons.androidscouting.activities.DHActivity;
import org.fullmetalfalcons.androidscouting.activities.RetrieveDataActivity;
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
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static class RequestTask extends AsyncTask<String,Void,ResultSet>{

        private Connection c = null;
        private DHActivity a;

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
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
