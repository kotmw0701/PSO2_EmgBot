package jp.kotmw.pso2_discordbot.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import jp.kotmw.pso2_discordbot.BotClientManager;

public class ShipController implements Initializable {

	@FXML
	Label shipNum;
	
	@FXML
	Pane pane;
	
	@FXML
	ToggleButton toggle;
	
	int server;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		toggle.setDisable(true);
	}
	
	@FXML
	public void toggleStatus(ActionEvent event) {
		if(!changeStatus(pane, toggle)) return;
		BotClientManager.changeEnable(server, toggle.isSelected());
	}
	
	private boolean changeStatus(Pane pane, ToggleButton enable) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("確認");
		alert.setContentText("Botを"+ (enable.isSelected() ? "稼働" : "停止") +"させてよろしいですか？");
		if(alert.showAndWait().get() == ButtonType.OK) {
			change(pane, enable);
			return true;
		}
		enable.setSelected(!enable.isSelected());
		return false;
	}
	
	private static void change(Pane pane, ToggleButton enable) {
		if(enable.isSelected()) {
			pane.setStyle("-fx-background-color: #0F0; -fx-border-style: solid;");
			((Label)pane.getChildren().get(0)).setText("稼働中");
		} else {
			pane.setStyle("-fx-background-color: #F20; -fx-border-style: solid;");
			((Label)pane.getChildren().get(0)).setText("停止中");
		}
	}
	
	public void togglechange() {
		toggle.setSelected(!toggle.isSelected());
		change(pane, toggle);
	}
	
	public ShipController setServer(int server) {
		this.server = server;
		shipNum.setText("Ship"+server);
		return this;
	}
	
	public int getServer() {
		return server;
	}
	
	public void enable() {
		toggle.setDisable(false);
	}
}
