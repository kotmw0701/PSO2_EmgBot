package jp.kotmw.pso2_discordbot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

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
		if(!allemg.contains("緊急クエスト予告"))
			return;
		try {
			Main.manager.getMotherClient().getChannelByID("236138218955866128").sendMessage("───────────────" +Main.sepa+ allemg);
			setHistory(allemg, false);
		} catch (MissingPermissionsException | RateLimitException
				| DiscordException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void setHistory(String allemg, boolean initialization) throws IOException, InterruptedException {
		Calendar cal = new GregorianCalendar();
		String date = cal.get(Calendar.YEAR)
				+ "-" + (cal.get(Calendar.MONTH) + 1)
				+ "-" + cal.get(Calendar.DAY_OF_MONTH);
		String hour = (String.valueOf(cal.get(Calendar.HOUR_OF_DAY)+1).length() < 2 ? "0"+String.valueOf(cal.get(Calendar.HOUR_OF_DAY)+1) : String.valueOf(cal.get(Calendar.HOUR_OF_DAY)+1));
		Main.sendDebugMessage("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
		Main.sendDebugMessage("1: "+allemg);
		Main.sendDebugMessage("─────────────────────────────");
		Main.history.setAllEmergency(allemg = formatEmergencyText(allemg.replaceAll("\n", "/"), hour));
		BotClientManager.updateStatus(initialization);
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
	
	private static String formatEmergencyText(String allemg, String hour) {
		String formated = "Error:不具合を確認しました";
		List<String> sepas = Arrays.asList(allemg.split("/"));
		sepas = sepas.subList(1, sepas.size()).stream().filter(s -> !s.contains("メンテ日時変更検知"))
				.filter(s -> !s.matches("メンテナンス\\d時間前です。"))
				.collect(Collectors.toList());
		//League
		if(sepas.stream().anyMatch(emg -> emg.contains("アークスリーグ"))) {
			String league = sepas.get(a(sepas));
			List<String> list = sepas.stream().filter(s -> !s.contains("アークスリーグ")).collect(Collectors.toList());
			String[] emgarray = new String[10];
			for(int i = 0; i < 10; i++) {
				for(String emg : list)
					if(i == (Integer.valueOf(emg.split(":")[0])-1))
						emgarray[i] = emg.split(":")[1];
				if(emgarray[i] == null)
					emgarray[i] = league;
			}
			formated = "";
			for(int i = 0; i < 10; i++) {
				formated += formated.equalsIgnoreCase("") ? "Random:"+emgarray[i] : "/"+emgarray[i];
			}
		}
		//Notice
		else if(sepas.size() < 10)
			formated = "Notice:"+sepas.get(0);
		//Random
		else if(sepas.size() == 10) {
			formated = "";
			for(String emg : sepas) {
				emg = emg.split(":")[1].equalsIgnoreCase("―") ? "報告がありません" : emg.split(":")[1];
				formated += (formated.equalsIgnoreCase("") ? "Random:" + emg : "/" + emg);
			}
		}
		FxControllers.addLog(formated);
		return formated;
	}
	
	static int a(List<String> emgs) {
		for(String emg : emgs) {
			if(emg.contains("アークスリーグ"))
				return emgs.indexOf(emg);
		}
		return -1;
	}
}
