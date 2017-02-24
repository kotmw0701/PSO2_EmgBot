package jp.kotmw.pso2_discordbot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

import jp.kotmw.pso2_discordbot.controllers.FxControllers;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import twitter4j.Status;
import twitter4j.UserStreamAdapter;

public class MyUserStream extends UserStreamAdapter {

	@Override
	public void onStatus(Status status) {
		super.onStatus(status);
		/*try {
			omikuzi(status);
		} catch (TwitterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		if(!status.getUser().getScreenName().equalsIgnoreCase("pso2_emg_hour") && (!status.getUser().getScreenName().equalsIgnoreCase("kotmw_sub") || !Main.debug))
			return;
		if(status.isRetweeted()) {
			System.out.println("RTのツイートのため流しません");
			System.out.println(status.getText());
			return;
		}
		String allemg = status.getText().replaceAll("#PSO2", "");
		try {
			Main.manager.getMotherClient().getChannelByID("").sendMessage("───────────────" +Main.sepa+ allemg);
			setHistory(allemg, false);
		} catch (MissingPermissionsException | RateLimitException
				| DiscordException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void setHistory(String allemg, boolean initialization) throws IOException {
		Calendar cal = new GregorianCalendar();
		String date = cal.get(Calendar.YEAR)
				+ "-" + (cal.get(Calendar.MONTH) + 1)
				+ "-" + cal.get(Calendar.DAY_OF_MONTH);
		String hour = ""+(cal.get(Calendar.HOUR_OF_DAY)+1);
		Main.sendDebugMessage("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
		Main.sendDebugMessage("1: "+allemg);
		Main.sendDebugMessage("─────────────────────────────");
		hour = hour.length() < 2 ? "0"+hour : hour;
		/*if(!allemg.startsWith("＜"+(String.valueOf((cal.get(Calendar.HOUR_OF_DAY)+1)).length() < 2 ? "0"+hour : hour)+"時 緊急クエスト予告＞"))
			return;*/
		allemg = allemg.
				substring(15).replace("　", "").
				replaceAll("\r", Main.sepa).
				replaceAll("\n", Main.sepa).
				replaceAll("\r\n", Main.sepa).
				replaceAll(Main.sepa+"メンテナンス2時間前です。", "").
				replaceAll(Main.sepa+"メンテナンス1時間前です。", "");
		if(allemg.substring(5, 7).equalsIgnoreCase("続報"))
			return;
		Main.sendDebugMessage("2: "+allemg);
		Main.sendDebugMessage("─────────────────────────────");
		//String[] ships = allemg.split(Main.sepa);
		if (allemg.startsWith("【準備中】")) {
			if(allemg.indexOf(Main.sepa) != -1)
				allemg = allemg.substring(0, allemg.indexOf(Main.sepa)+2).replaceAll("【準備中】", "");
			else
				allemg = allemg.replaceAll("【準備中】", "");
		} else if (allemg.startsWith("【開催中】")) {
			if(allemg.indexOf(Main.sepa) != -1) {
				allemg = allemg.replaceAll(allemg.substring(0,allemg.indexOf(Main.sepa)+2),"");
				if(allemg.startsWith("【開催間近】"))
					if(allemg.indexOf(Main.sepa) != -1)
						allemg = allemg.substring(0, allemg.indexOf(Main.sepa)+2).replaceAll("【開催間近】", "");
					else
						allemg = allemg.replaceAll("【開催間近】", "");
				else if(allemg.startsWith("【準備中】"))
					if(allemg.indexOf(Main.sepa) != -1)
						allemg = allemg.substring(0, allemg.indexOf(Main.sepa)+2).replaceAll("【準備中】", "");
					else
						allemg = allemg.replaceAll("【準備中】", "");
				else allemg = "無し";
			} else allemg = "無し";
		}
		String[] ships = allemg.split(Main.sepa);
		if(ships.length == 10) {
			allemg = "";
			for(int i = 0; i <= 9; i++) {
				String ship[] = ships[i].split(":");
				if(ship[1].equalsIgnoreCase("―"))
					ship[1] = "無し";
				allemg = allemg.equalsIgnoreCase("") ? "Random:"+ship[0]+":"+ship[1] : allemg+"/"+ship[0]+":"+ship[1];
			}
		} else {
			if(allemg.indexOf(Main.sepa) != -1)
				allemg = allemg.substring(0, allemg.indexOf(Main.sepa)+2);
			allemg = "Notice:"+allemg.replaceAll(Main.sepa, "");
		}
		//allemg = formatEmergencyText(ships);
		Main.history = new EmgHistory(allemg);
		BotClientManager.changeStatus(allemg, hour, initialization);
		if(initialization)
			return;
		Main.sendDebugMessage("処理チェック3");
		Main.sendDebugMessage("3: "+allemg);
		Main.sendDebugMessage("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
		FxControllers.addLog(allemg);
		File file = new File("C:\\Discord_bot\\PSO2_EmgBot\\history\\emg_"+date+".log");
		if(!file.exists()) {
			file.createNewFile();
		}
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true),"UTF-8"));
		writer.println(hour+": "+allemg.replaceAll(Main.sepa, ""));
		writer.close();
	}
	
	@SuppressWarnings("unused")
	private static boolean isNotice(String[] args) {
		for(String txt : args) {
			if(txt.indexOf("アークスリーグ") > -1)
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private static String formatEmergencyText(String[] allemg) {
		String formated = null;
		
		
		
		
		return formated;
	}
}
