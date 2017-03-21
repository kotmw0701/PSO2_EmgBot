package jp.kotmw.pso2_discordbot.util;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class CommandTextRemover extends Thread {

	IMessage imessage;
	int second;
	
	public CommandTextRemover(IMessage imessage, int second) {
		this.imessage = imessage;
		this.second = second;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(second*1000);
			this.imessage.delete();
		} catch (InterruptedException |
				MissingPermissionsException | 
				RateLimitException | 
				DiscordException e) {
			e.printStackTrace();
		}
	}
}
