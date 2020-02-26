/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.logincontrol.config;

import java.util.List;

/**
 * 設定ファイルを読み込む
 *
 * @author sugichan
 */
public class Reward {
    public static boolean sound_play;
    public static String sound_type;
    public static int sound_volume;
    public static int sound_pitch;

    public static String basic_message;
    public static List<String> basic_command;

    public static String advance_message;
    public static List<String> advance_command;
}
