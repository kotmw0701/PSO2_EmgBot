package jp.kotmw.pso2_discordbot.util;

import java.util.ArrayList;
import java.util.List;

import jp.kotmw.pso2_discordbot.BotClientManager;
import jp.kotmw.pso2_discordbot.Main;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class MessageUtil {

	public List<IMessage> sendGuildsMessageforNoticeChannel(List<IGuild> guilds, String... msgs) throws MissingPermissionsException, RateLimitException, DiscordException {
		List<IMessage> guildmessages = new ArrayList<>();
		for(IGuild guild : guilds) {
			IChannel channel = BotClientManager.getNoticeChannel(guild);
			if(channel == null)
				continue;
			guildmessages.add(sendMessage(channel, msgs));
		}
		return guildmessages;
	}
	
	public void sendGuildsMessageTimeRemover(List<IGuild> guilds, int delaysecond, String... msgs) throws MissingPermissionsException, RateLimitException, DiscordException {
		for(IMessage imsgs : sendGuildsMessageforNoticeChannel(guilds, msgs))
			new CommandTextRemover(imsgs, delaysecond).start();
	}
	
	public IMessage sendMessage(IChannel channel, String... msgs) throws MissingPermissionsException, RateLimitException, DiscordException {
		String separator = "\r\n";
		String text = "";
		if(Main.debug)
			text = "**``現在デバッグモード中です、ご迷惑をおかけします``**"+separator;
		for(String msg : msgs)
			text = text+msg+separator;
		return channel.sendMessage(text);
	}
	
	public void sendMessageandTimeRemover(IChannel channel, int delaysecond, String... msgs) throws MissingPermissionsException, RateLimitException, DiscordException {
		new CommandTextRemover(sendMessage(channel, msgs), delaysecond).start();
	}
}
