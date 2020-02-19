/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.logincontrol.database;

import java.util.Date;
import java.text.SimpleDateFormat;
import com.zaxxer.hikari.HikariDataSource;

/**
 *
 * @author sugichan
 */
public class Database {
    public static HikariDataSource dataSource = null;
    public static final SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

    //  sql = "CREATE TABLE IF NOT EXISTS hosts (ip INTEGER UNSIGNED, host varchar(60), count int, newdate DATETIME, lastdate DATETIME )";
    public static String IP = "0.0.0.0";
    public static String Host = "Unknown";
    public static int Count = 0;
    public static Date NewDate = new Date();
    public static Date LastDate = new Date();
    public static boolean Warning = false;
}
