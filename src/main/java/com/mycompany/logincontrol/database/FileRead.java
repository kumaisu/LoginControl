/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.database;

import com.mycompany.kumaisulibraries.Tools;
import static com.mycompany.logincontrol.config.Config.programCode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.bukkit.ChatColor;

/**
 *
 * @author sugichan
 */
public class FileRead {
    private static final Calendar loopCalendar = Calendar.getInstance();
    private static Date userDate;
    private static String userName;
    private static String userUUID;
    private static String userIP;

    private static String dispCalendar( Calendar calendar ) {
        int year = calendar.get( Calendar.YEAR );
        int month = calendar.get( Calendar.MONTH ) + 1;
        int day = calendar.get( Calendar.DATE );

        StringBuffer sb = new StringBuffer();
        sb.append( year );
        sb.append( "-" );
        sb.append( String.format( "%02d", month ) );
        sb.append( "-" );
        sb.append( String.format( "%02d", day ) );
        sb.append( "-" );
        // System.out.println( new String( sb ) );
        return new String( sb );
    }
    
    private static String GetDate() {
        int year = loopCalendar.get( Calendar.YEAR );
        int month = loopCalendar.get( Calendar.MONTH ) + 1;
        int day = loopCalendar.get( Calendar.DATE );
        StringBuffer sb = new StringBuffer();
        sb.append( year );
        sb.append( "/" );
        sb.append( String.format( "%02d", month ) );
        sb.append( "/" );
        sb.append( String.format( "%02d", day ) );
        return new String( sb );
    }

    private static void LoginInfo( String line ) {
        //  [23:42:24] [User Authenticator #10/INFO]: UUID of player fuuuuma is d6bb1768-0960-45dc-9c22-6a0e46953e3c
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        String strDate = GetDate() + " " + line.substring( 1, 9 );
        try {
            userDate = sdFormat.parse( strDate );
        } catch (ParseException ex) {
            Tools.Prt( "LoginInfo Error : " + ex.getMessage(), programCode );
        }
        userName = line.substring( line.indexOf( "player" ) + 7, line.indexOf( " is " ) );
        userUUID = line.substring( line.indexOf( " is " ) + 4 );
    }
    
    private static void MCBanInfo( String line ) {
        /*
        [00:08:14] [User Authenticator #2/INFO]: Disconnecting com.mojang.authlib.GameProfile@6625162c[id=<null>,name=peron821,properties={},legacy=false] (/119.25.60.209:57703): Authentication servers are down. Please try again later, sorry!
        [00:08:14] [User Authenticator #2/INFO]: Disconnecting com.mojang.authlib.GameProfile@7580949a[id=8ba4495b-5885-4de5-9dd5-6cd5a20c21b4,name=KENT34,properties={textures=[com.mojang.authlib.properties.Property@4383b45f]},legacy=false] (/101.141.21.148:64527): あなたのReputation値は、このサーバの閾値以下です！
        [id=8ba4495b-5885-4de5-9dd5-6cd5a20c21b4,name=KENT34,properties={textures=[com.mojang.authlib.properties.Property@4383b45f]},legacy=false] (/101.141.21.148:64527): 
        */
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        String strDate = GetDate() + " " + line.substring( 1, 9 );
        try {
            userDate = sdFormat.parse( strDate );
        } catch (ParseException ex) {
            Tools.Prt( "MCBanInfo Error : " + ex.getMessage(), programCode );
        }
        userName = line.substring( line.indexOf( "name=" ) + 5, line.indexOf( ",properties" ) );
        userUUID = line.substring( line.indexOf( "id=" ) + 3, line.indexOf( ",name=" ) );
        String IP = line.substring( line.indexOf( " (/" ) + 3, line.indexOf( "): " ) - 2 );
        userIP = IP.substring( 0, IP.indexOf( ":" ) );
    }

    private static void GetFileLine( String fileName ) {
        DatabaseControl DBA = new DatabaseControl();
        try {
            //ファイルを読み込む
            FileReader fr = new FileReader( fileName );
            BufferedReader br = new BufferedReader( fr );

            //読み込んだファイルを１行ずつ画面出力する
            String line;
            int count = 0;
            DBA.open();
            while ( ( line = br.readLine() ) != null ) {
                ++count;
                if ( line.contains( "Authenticator") ) {
                    Tools.Prt( count + "行目：" + line, Tools.consoleMode.max, programCode );
                    if ( line.contains( "UUID of" ) ) {
                        LoginInfo( line );
                        // System.out.println(  "Date = " + userDate.toString() + " , Name = [" + userName + "] , UUID = [" + userUUID + "]" );
                    }
                    if ( line.contains( "Disconnecting" ) && line.contains( "com.mojang.authlib" ) ) {
                        MCBanInfo( line );
                        if ( !userUUID.equals( "<null>" ) ) {
                            Tools.Prt(  "Date = " + userDate.toString() + " , Name = [" + userName + "] , UUID = [" + userUUID + "], IP = [" + userIP + "]", programCode );
                            DBA.listSave( userDate, userName, userUUID, userIP, 0 );
                        }
                    }
                }
                if ( line.contains( userName + "[/" ) ) {
                    //  [19:41:27] [Server thread/INFO]: peron821[/119.25.60.209:60484] logged in with entity id 42209 at ([world] 6812.276863575932, 63.0, 2.92843090938411)
                    Tools.Prt( count + "行目：" + line, Tools.consoleMode.max, programCode );
                    String IP = line.substring( line.indexOf( "[/" ) + 2, line.indexOf( "] l" ) - 1 );
                    userIP = IP.substring( 0, IP.indexOf( ":" ) );
                    Tools.Prt(  "Date = " + userDate.toString() + " , Name = [" + userName + "] , UUID = [" + userUUID + "], IP = [" + userIP + "]", programCode );
                    DBA.listSave( userDate, userName, userUUID, userIP, 1 );
                }
            }
            DBA.close();

            //終了処理
            br.close();
            fr.close();
        } catch (IOException ex) {
            //例外発生時処理
            Tools.Prt( "Error : " + ex.getMessage(), programCode );
        }        
    }
    
    /**
     *
     * @param numStr
     */
    public static void GetLogFile( String numStr ) {
        int Year;
        
        try {
            Year = Integer.valueOf( numStr );
        } catch ( NumberFormatException e ) {
            Tools.Prt( ChatColor.RED + "NumException : " + e.getMessage(), programCode);
            return;
        }
        
        Calendar endDate = Calendar.getInstance();

        // 月はｰ1で設定する
        loopCalendar.set( Year, 0, 1 );
        dispCalendar( loopCalendar );
        endDate.set( Year, 11, 31 );
        String EndDateText = dispCalendar( endDate );

        boolean check = true;
        do {
            loopCalendar.add( Calendar.DAY_OF_MONTH, 1 );
            String file1 = dispCalendar( loopCalendar );

            boolean exit = true;
            int j = 0;
            do {
                j++;
                String FileName = file1 + j + ".log";
                File file = new File( "/home/minecraft/tools/" + FileName );
                if ( file.exists() ) {
                    Tools.Prt( FileName, Tools.consoleMode.max, programCode );
                    GetFileLine( "/home/minecraft/tools/" + FileName );
                } else exit = false;
            } while ( exit );
        } while( !dispCalendar( loopCalendar ).equals( EndDateText ) );
    }
}
