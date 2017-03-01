package jp.kotmw.pso2_discordbot;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import jp.kotmw.pso2_discordbot.controllers.FxControllers;
import jp.kotmw.pso2_discordbot.controllers.ToggleCoolTime;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;

public class Main extends Application {

	public static TwitterStream twitterStream;
	public static BotClientManager manager;
	static Logger logger;
	static String sepa = System.getProperty("line.separator");
	static boolean debug = true;
	static boolean allenable = false;
	static EmgHistory history;
	static int enableerror;
	static ToggleCoolTime toggleenable;
	static ToggleCoolTime setemg;
	static LoadToken tokens = LoadToken.getInstance();

	public static void main(String args[]) throws InterruptedException, IOException {
		System.setProperty("file.encoding", "UTF-8");
		logger = Logger.getLogger("Emg_bot_logger");
		logger.log(Level.INFO, "Botを起動します...");
		history = new EmgHistory();
		tokens.initialize();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1*1000);
					FxControllers.addLog("Staring Emergency Bot");
					/*Alert alert = new Alert(AlertType.CONFIRMATION, "鯖別Botも立ち上げますか？", ButtonType.YES, ButtonType.NO);
					alert.setTitle("選択");
					allenable = alert.showAndWait().get().equals(ButtonType.YES);*/
					FxControllers.addLog("TwitterStream conneting...");
					twitterStream = new TwitterStreamFactory().getInstance();
					twitterStream.addListener(new MyUserStream());
					twitterStream.user();
					FxControllers.addLog("TwitterStream connected");
					FxControllers.addLog("Discord connecting...");
					manager = new BotClientManager(allenable);
					FxControllers.addLog("Discord connected");
					getFinalStatus(new TwitterFactory().getInstance());
					EventDispatcher dispatcher = manager.getMotherClient().getDispatcher();
					dispatcher.registerListener(new EventListener());
					Platform.runLater(()-> {
						Alert alert2 = new Alert(AlertType.INFORMATION);
						alert2.setTitle("起動完了");
						alert2.setContentText("全Botの立ち上げが完了しました  Error数: "+enableerror+"？");
						alert2.show();
					});
					for(int i = 1; i<= 10; i++)
						FxControllers.getServer(i).enable();
					FxControllers.addLog("Bot started");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		launch(args);
	}

	public static void sendDebugMessage(String msg) {
		if(debug)
			logger.log(Level.INFO, msg);
	}

	private static void getFinalStatus(Twitter twitter) {
		try {
			User user = twitter.showUser("@pso2_emg_hour");
			MyUserStream.setHistory(twitter.getUserTimeline(user.getId()).get(0).getText().replaceAll("#PSO2", ""), true);
		} catch (TwitterException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("緊急Bot管理GUI");
		primaryStage.getIcons().add(new Image("arks_iogo.jpg"));
		primaryStage.setResizable(false);
		primaryStage.setScene(new Scene(FXMLLoader.load(ClassLoader.getSystemResource("MainWindow.fxml")), 950, 630));
		primaryStage.show();
		primaryStage.setOnCloseRequest(event -> {
			try {
				shutdownConfirnation(event);
			} catch (DiscordException e) {
				e.printStackTrace();
			}
		});
	}
	
	public static void shutdownConfirnation(Event event) throws DiscordException {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("確認");
		alert.setContentText("Botをすべて停止させてよろしいですか？");
		if(alert.showAndWait().get() == ButtonType.OK) {
			System.exit(0);
			Main.manager.shutdown();
			Main.twitterStream.shutdown();
		} else {
			event.consume();
		}
	}
}
