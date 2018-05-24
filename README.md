# LoginControl
Minecraft Plugin LoginControl Repository  

**Overview**  

As for player login, it is a plug-in that supports variously  
  
1.Ability to display referenced players to console or authorized player in real time.  
2.Return Modified MotD-Message for player's server query.  
3.Teleport new players to a specific location.  
4.Ability to give items to new players.  
5.History record and reference function of logged-in player and failed player.  
6.Ability to display the list of users from the same IP to authorized player.  

Extra function
1. Flight mode / flight [on / off]
2. Trash Can Signboard [Trash Can]
3. Death announcement (incomplete)

  
LoginControlは、プレイヤーログインに付いて、サポートをするプラグインです  
  
1.クライアントのサーバーリストを更新した際に、ホストに対して行われる参照をコンソールまたは許可されたプレイヤー画面にリアルタイムに表示します  
2.参照されたプレイヤーに対し、MotD内容を成形し送信します  
3.新規ログインプレイヤーに対して、特定場所へテレポートする機能  
4.新規ログインプレイヤーに特定のアイテムをプレゼントする機能  
5.ログイン時にデータベース記録し、参照する機能（直近５人）またはコマンドによって１０人  
6.同一IPからのアクセスを監視し、プレイヤー名を列挙する機能  

おまけ機能  
1.フライトモード /flight [on/off]  
2.ゴミ箱看板    [Trash Can]  
3.死亡アナウンス（不完全）  

**Usage**  

/Loginlist [d:yymmdd] [u:player name] [full]  

/LoginCtl reload  
/LoginCtl info:[ip address]  
/LoginCtl chg:[ip address]:[host name]  
/LoginCtl PingTop  

**How to Install**  
1.サーバーのプラグインディレクトリにLoginControl.jarを入れて起動します  
2.一旦終了し、作成されたConfig.ymlを編集します  
3.config設定の通りMySQLにデーターベースをCreateします  
4.再度サーバーを起動  
  
Contact is Discord Kitsune#5955  
Discord Server https://discord.gg/AgX3UxR  

