/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.config;

import java.util.List;
import java.util.Map;

/**
 * 設定ファイルを読み込む
 *
 * @author sugichan
 */
public class Config {

    public static String programCode = "LC";

    public static boolean kumaisu;

    public static String host;
    public static String port;
    public static String database;
    public static String username;
    public static String password;
    public static int MaximumPoolSize;
    public static int MinimumIdle;

    public static boolean JumpStats;
    public static boolean OpJumpStats;
    public static boolean CheckIPAddress;
    public static boolean playerPingB;
    public static float fx;
    public static float fy;
    public static float fz;
    public static float fpitch;
    public static float fyaw;
    public static String fworld;

    public static List<String> present;
    public static List<String> IgnoreReportName;
    public static List<String> IgnoreReportIP;

    public static boolean MotDControl;
    public static int AlarmCount;

    public static String LogFull;
    public static String Reload;
    public static String ArgsErr;
    public static String OptError;
    public static boolean Announce;
    public static boolean NewJoin;
    public static boolean ReturnJoin;
    public static boolean PlayerQuit;
    public static String New_Join_Message;
    public static String Returning_Join_Message;
    public static String AnnounceMessage;
    public static String PlayerQuitMessage;
    public static Map< String, String > KnownServers;
    public static Map< String, String > NewJoinMessage;
    public static Map< String, String > ReturnJoinMessage;
    public static String Incomplete_Message;
}
