package jp.kotmw.pso2_discordbot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jp.kotmw.pso2_discordbot.controllers.FxControllers;

public class EmgHistory {

	private static EmgHistory instance = new EmgHistory();
	private String notice;
	private Map<Integer,String> ships = new HashMap<>();
	
	private EmgHistory() {}
	
	public static EmgHistory getInstance() {
		return instance;
	}
	
	public void setHistory(String allemg, boolean initialization) throws IOException, InterruptedException {
		Calendar cal = new GregorianCalendar();
		String date = cal.get(Calendar.YEAR)
				+ "-" + (cal.get(Calendar.MONTH) + 1)
				+ "-" + cal.get(Calendar.DAY_OF_MONTH);
		String hour = (String.valueOf(cal.get(Calendar.HOUR_OF_DAY)+1).length() < 2 ? "0"+String.valueOf(cal.get(Calendar.HOUR_OF_DAY)+1) : String.valueOf(cal.get(Calendar.HOUR_OF_DAY)+1));
		Main.sendDebugMessage("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
		Main.sendDebugMessage("1: "+allemg);
		Main.sendDebugMessage("─────────────────────────────");
		setAllEmergency(allemg = formatEmergencyText(allemg.replaceAll("\n", "/"), hour));
		BotClientManager.updateStatus(initialization);
		FxControllers.addLog(allemg);
		if(initialization)
			return;
		Main.sendDebugMessage("処理チェック3");
		Main.sendDebugMessage("3: "+allemg);
		Main.sendDebugMessage("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
		File file = new File("C:\\Discord_bot\\PSO2_EmgBot\\history\\emg_"+date+".log");
		if(!file.exists()) {
			file.createNewFile();
		}
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true),"UTF-8"));
		writer.println(hour+": "+allemg);
		writer.close();
	}
	
	private String formatEmergencyText(String allemg, String hour) {
		String formated = "Error:不具合を確認しました";
		List<String> sepas = Arrays.asList(allemg.split("/"));
		sepas = sepas.subList(1, sepas.size()).stream().filter(s -> !s.contains("メンテ日時変更検知"))
				.filter(s -> !s.matches("メンテナンス\\d時間前です。"))
				.filter(s -> !s.contains("イベント情報更新"))
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
				emg = emg.split(":")[1].contains("―") ? "報告がありません" : emg.split(":")[1];
				formated += (formated.equalsIgnoreCase("") ? "Random:" + emg : "/" + emg);
			}
		}
		return formated;
	}
	
	private int a(List<String> emgs) {
		for(String emg : emgs) {
			if(emg.contains("アークスリーグ"))
				return emgs.indexOf(emg);
		}
		return -1;
	}
	
	public void setAllEmergency(String allemg) {
		if(allemg.startsWith("Random:"))
			setAllEmergency("random", allemg.replaceAll("Random:", ""));
		else if(allemg.startsWith("Notice:"))
			setAllEmergency("notice", allemg.replaceAll("Notice:", ""));
	}
	
	public void setAllEmergency(String type, String emg) {
		for(int i = 1; i <= 10; i++) {
			if(type.equalsIgnoreCase("notice")) {
				ships.put(i, emg);
				notice = emg;
			} else if(type.equalsIgnoreCase("random")) {
				ships.put(i, emgTextConverter(emg)[i-1]);
				notice = "ランダム緊急がある可能性";
			}
		}
	}
	
	private String[] emgTextConverter(String emg) {
		emg = emg.replace(",", "/");
		String[] emgarray = new String[10];
		int server = 0;
		for(String emgs : emg.split("/")) {
			if(emgs.indexOf(":") > -1) {
				server = Integer.valueOf(emgs.split(":")[0])-1;
				emgs = emgs.split(":")[1];
			}
			emgarray[server] = emgs;
			server++;
		}
		return emgarray;
	}
	
	public void setEmergency(int server, String emg) {
		ships.put(server, emg);
	}

	public String getEmergency(int server) {
		if(!ships.containsKey(server))
			return "無し";
		return ships.get(server);
	}
	
	public String getNotice() {
		return notice;
	}
}
