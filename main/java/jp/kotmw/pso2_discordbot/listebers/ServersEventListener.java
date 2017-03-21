package jp.kotmw.pso2_discordbot.listebers;

import jp.kotmw.pso2_discordbot.BotClientManager;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class ServersEventListener {
	
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent e) {
		
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
			channel.sendMessage(e.getUser().mention()+"さん、いらっしゃい!"+text);
		} catch (MissingPermissionsException | RateLimitException
				| DiscordException e1) {
			e1.printStackTrace();
		}
	}
}
