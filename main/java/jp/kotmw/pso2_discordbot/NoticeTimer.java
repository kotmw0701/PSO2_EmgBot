package jp.kotmw.pso2_discordbot;

import java.util.TimerTask;

import jp.kotmw.pso2_discordbot.controllers.FxControllers;
import jp.kotmw.pso2_discordbot.util.MessageUtil;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class NoticeTimer extends TimerTask {

	@Override
	public void run() {
		FxControllers.addLog(super.scheduledExecutionTime());
		if(EmgHistory.getInstance().getNotice().equalsIgnoreCase("ランダム緊急がある可能性"))
			for(int i = 1; i <= 10; i++)
				if(isEmg(EmgHistory.getInstance().getEmergency(i)))
					sendNotice(i);
		else if(EmgHistory.getInstance().getNotice().contains("【準備中】"))
			sendNoticeAllServer();
	}
	
	private void sendNoticeAllServer() {
		for(int i = 1; i <= 10; i++)
			sendNotice(i);
	}
	
	private void sendNotice(int server) {
		IDiscordClient bot = BotClientManager.getClient(server);
		MessageUtil util = new MessageUtil();
		if(bot == null)
			return;
		try {
			String serveremg = EmgHistory.getInstance().getEmergency(server);
			String emg = EmgHistory.getInstance().getHour()+"時より "+ (serveremg.contains("【準備中】") ? serveremg.replaceAll("【準備中】 ", "").split(" ")[1] : serveremg) +" 緊急が発生します。";
			for(IGuild guild : bot.getGuilds()) {
				IChannel channel = BotClientManager.getNoticeChannel(guild);
				if(channel == null)
					continue;
				String mention = guild.getID().equalsIgnoreCase("190495171371204608") ? guild.getRolesByName("Ship"+server).get(0).mention()+" " : getMentions(server, guild);
				util.sendMessage(channel, mention+emg);
			}
		} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String getMentions(int server, IGuild guild) {
		String mentions = "";
		for(String id : BotConfiguration.getServerUsers(server)) 
			if(id.split("\\.")[0].equalsIgnoreCase(guild.getID()))
				mentions += "<@!"+id.split("\\.")[1]+"> ";
		return mentions;
	}
	
	private boolean isEmg(String emg) {
		if(emg == "無し")
			return false;
		else if(emg.matches("\\(\\d{2}時 巨躯\\)") || emg.matches("\\(\\d{2}時 敗者\\)") || emg.matches("\\(\\d{2}時 禍津\\)"))
			return false;
		else switch(emg) {
		case "報告がありません":
		case "[発生中]":
			return false;
		}
		return true;
	}
}
