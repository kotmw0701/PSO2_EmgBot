package jp.kotmw.pso2_discordbot;

import java.io.IOException;
import java.util.Optional;
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
import javafx.scene.control.TextInputDialog;
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
	private static Stage primarystage;
	static boolean debug = false;
	static Logger logger;
	static String sepa = System.getProperty("line.separator");
	static ToggleCoolTime toggleenable;
	static ToggleCoolTime setemg;

	public static void main(String... args) throws InterruptedException, IOException {
		System.setProperty("file.encoding", "UTF-8");
		logger = Logger.getLogger("Emg_bot_logger");
		logger.log(Level.INFO, "Botを起動します...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					FxControllers.addLog("Staring Emergency Bot");
					Thread.sleep(1*1000);
					if(BotConfiguration.getMotherToken() == null) {
						Platform.runLater(() -> {
							TextInputDialog dialog = new TextInputDialog();
							dialog.setTitle("Token入力画面");
							dialog.setHeaderText("中心BotのTokenを打ち込んでください");
							Optional<String> result = dialog.showAndWait();
							result.ifPresent(token -> {
								try {
									BotConfiguration.saveMotherToken(token);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							});
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.setTitle("確認");
							if(!result.isPresent() || result.get().isEmpty()) {
								alert.setContentText("取り消されました、プログラムを停止します");
								System.exit(0);
								return;
							} else
								alert.setContentText("保存しました、起動を開始します");
							alert.showAndWait();
							startup();
						});
						return;
					}
					startup();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			private void startup() {
				FxControllers.addLog("TwitterStream conneting...");
				twitterStream = new TwitterStreamFactory().getInstance();
				twitterStream.addListener(new MyUserStream());
				twitterStream.user();
				FxControllers.addLog("TwitterStream connected");
				FxControllers.addLog("Discord connecting...");
				manager = new BotClientManager();
				FxControllers.addLog("Discord connected");
				EventDispatcher dispatcher = manager.getMotherClient().getDispatcher();
				dispatcher.registerListener(new EventListener());
				getFinalStatus(new TwitterFactory().getInstance());
				Platform.runLater(() -> {
					Alert alert2 = new Alert(AlertType.INFORMATION);
					alert2.setTitle("起動完了");
					alert2.setContentText("Botの起動が完了しました");
					alert2.show();
				});
				for(int i = 1; i<= 10; i++)
					FxControllers.getServer(i).enable();
				FxControllers.addLog("Bot started");
			}
		}).start();
		launch(args);
	}

	public static void sendDebugMessage(String msg) {
		if(debug)
			FxControllers.addLog(msg);
	}
	
	public static boolean toggleDebugmode() {
		return Main.debug = !Main.debug;
	}

	private static void getFinalStatus(Twitter twitter) {
		try {
			User user = twitter.showUser("@pso2_emg_hour");
			EmgHistory.getInstance().setHistory(twitter.getUserTimeline(user.getId()).get(0).getText().replaceAll("#PSO2", ""), true);
		} catch (TwitterException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Main.primarystage = primaryStage;
		primaryStage.setTitle("緊急Bot管理GUI");
		primaryStage.getIcons().add(new Image("arks_logo.jpg"));
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
	
	public static Stage getPrimary() {
		return primarystage;
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
