package jp.kotmw.pso2_discordbot.listebers;

import jp.kotmw.pso2_discordbot.BotClientManager;
import jp.kotmw.pso2_discordbot.EmgHistory;
import jp.kotmw.pso2_discordbot.Main;
import jp.kotmw.pso2_discordbot.controllers.FxControllers;
import jp.kotmw.pso2_discordbot.util.MessageUtil;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class ServersEventListener extends MessageUtil{
	
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent e) throws DiscordException, MissingPermissionsException, RateLimitException {
		IMessage imsg = e.getMessage();
		if(hasMotherBot(imsg.getGuild()))
			return;
		String txt = imsg.getContent();
		if(!txt.startsWith("%"))
			return;
		String args[] = txt.substring(1).split(" ");
		String command = args[0];
		IChannel channel = imsg.getChannel();
		int server = BotClientManager.getServerNum(e.getClient().getApplicationClientID());
		if(server == -1)
			return;
		FxControllers.addLog("BotNum."+server+": " + imsg.getAuthor().getName()+" issued command: "+txt);
		if(command.equalsIgnoreCase("help")) {
			sendMessage(channel, "------Commands------"
					,"**`< >`** 必須引数, **`[ ]`** 任意引数"
					,"**`%emergency [server]`**	現在予告が出てる緊急を表示します"
					,"**`%geturl`**	Botの認証URLを取得します");
		} else if(command.equalsIgnoreCase("emg") || command.equalsIgnoreCase("emergency")) {
			sendMessageandTimeRemover(channel, 5, EmgHistory.getInstance().getEmergency(server));
		} else if(command.equalsIgnoreCase("geturl")) {
			String applicationId = BotClientManager.getID(server);
			sendMessageandTimeRemover(channel, 10, server+"鯖Botの認証URL [ https://discordapp.com/oauth2/authorize?client_id="+applicationId+"&scope=bot&permissions=84992 ]");
		}
	}
	
	@EventSubscriber
	public void onJoin(UserJoinEvent e) {
		IGuild guild = e.getGuild();
		try {
			IChannel channel = BotClientManager.getNoticeChannel(guild);
			if(e.getUser().isBot()) {
				channel.sendMessage("Botが追加されました Name: "+e.getUser().getName());
				return;
			}
			String text = guild.getID().equalsIgnoreCase("190495171371204608") ? e.getClient().getChannelByID("190789370335199232").mention()+" に自己紹介を書いてくれると感謝感謝です！" : "";
			channel.sendMessage(e.getUser().mention()+" さん、いらっしゃい!"+text);
		} catch (MissingPermissionsException | RateLimitException
				| DiscordException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * コマンドを受け取った鯖に中枢Botが居たらtrue、いなかったらfalseを返す
	 * 
	 * @param iguild 対象の鯖
	 * 
	 * @return 居たらtrue、居ないならfalse
	 * @throws DiscordException 
	 * 
	 */
	private boolean hasMotherBot(IGuild iguild) throws DiscordException {
		for(IUser users : iguild.getUsers()) {
			if(users.getID().equals(Main.manager.getMotherClient().getApplicationClientID()));
				return true;
		}
		return false;
	}
}
