/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.logincontrol.rewards;

import java.util.Random;
import org.bukkit.Sound;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.mycompany.kumaisulibraries.Tools;
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.logincontrol.config.Config;
import com.mycompany.logincontrol.config.Reward;
import com.mycompany.logincontrol.database.HostData;

/**
 *
 * @author sugichan
 */
public class Rewards {

    public static void Reward( Player player ) {
        Tools.Prt( ChatColor.YELLOW + "Daily Rewards !!", Tools.consoleMode.full, Config.programCode );
        HostData.SetRewardDate( player.getAddress().getHostString() );

        if ( Reward.sound_play ) {
            Tools.Prt( "Sound Play !!", Tools.consoleMode.full, Config.programCode );
            ( player.getWorld() ).playSound(
                player.getLocation(),                   // 鳴らす場所
                Sound.valueOf( Reward.sound_type ),     // 鳴らす音
                Reward.sound_volume,                    // 音量
                Reward.sound_pitch                      // 音程
            );
        }

        Tools.Prt( player, Utility.ReplaceString( Reward.basic_message, player.getName() ), Config.programCode );
        Reward.basic_command.stream().forEach( BR -> {
            String BRC = Utility.ReplaceString( BR, player.getName() );
            Tools.ExecOtherCommand( player, BRC, "" );
            Tools.Prt( ChatColor.AQUA + "Command Execute : " + ChatColor.WHITE + BRC, Tools.consoleMode.max, Config.programCode );
        } );

        if ( Reward.advance_command.size() > 0 ) {
            Random random = new Random();
            int randomValue = random.nextInt( Reward.advance_command.size() + 1 );
            Tools.Prt( "Advance : " + Reward.advance_command.size() + " ( " + randomValue + ")", Tools.consoleMode.full, Config.programCode );
            if ( randomValue > 0 ) {
                Tools.Prt( player, Utility.ReplaceString( Reward.advance_message, player.getName() ), Config.programCode );
                String AR = Utility.ReplaceString( Reward.advance_command.get( randomValue - 1 ), player.getName() );
                Tools.ExecOtherCommand( player, AR, "" );
                Tools.Prt( ChatColor.AQUA + "Command Execute : " + ChatColor.WHITE + AR, Tools.consoleMode.max, Config.programCode );
            }
        }
    }

}
