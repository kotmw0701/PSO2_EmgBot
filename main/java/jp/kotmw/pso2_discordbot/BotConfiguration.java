package jp.kotmw.pso2_discordbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BotConfiguration {
	
	private static String mothertoken;
	private static Map<Integer, String> tokens = new HashMap<>();
	private static Properties config;
	private static String filepath = "setting.properties";//コンパイル前には必ず置き換えること！C:\\Discord_bot\\PSO2_EmgBot\\
	
	static {
		//Propertiesクラス使用
		config = new Properties();
		File file = new File(filepath);
		try {
			if(!file.exists())
				file.createNewFile();
			config.load(new FileInputStream(file));
			if(!config.containsKey("mother"))
				config.setProperty("mother", "");
			mothertoken = config.getProperty("mother");//中枢BotのToken設定
			for(int i = 1; i <= 10; i++) {
				if(!config.containsKey("ship"+i)) {
					config.setProperty("ship"+i, "");
				}
				tokens.put(i, config.getProperty("ship"+i));//鯖別BotのToken設定
			}
			config.store(new FileOutputStream(file), "comments");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//BufferedReader使ってファイル自体読み込んでごにょごにょする
		/*BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("setting.properties")));
		String str;
		try {
			while((str = reader.readLine()) != null) {
				if(str.contains("mother"))
					mothertoken = str.split(":")[1];//中枢BotのToken設定
				else if(str.contains("ship"))
					tokens.put(Integer.valueOf(str.replaceAll("ship", "").split(":")[0]), str.split(":")[1]);//鯖別BotのToken設定
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	
	public static String getMotherToken() {
		if(mothertoken.isEmpty())
			return null;
		return mothertoken;
	}
	
	public static String getToken(int server) {
		if(!tokens.containsKey(server))
			return null;
		else if(tokens.get(server).isEmpty())
			return null;
		return tokens.get(server);
	}
	
	static void updateToken(String token, int server) {
		tokens.put(server, token);
	}
	
	public static void saveMotherToken(String token) throws FileNotFoundException, IOException {
		mothertoken = token;
		config.setProperty("mother", token);
		config.store(new FileOutputStream(filepath), "comments");
	}
}
