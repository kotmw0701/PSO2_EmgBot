package jp.kotmw.pso2_discordbot.controllers;

public class ToggleCoolTime extends Thread {

	int second;
	
	public ToggleCoolTime(int second) {
		this.second = second;
	}
	
	@Override
	public void run() {
		while(second > 0) {
			FxControllers.updateCooltime(second);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			second--;
		}
	}
	
	public int getCooltime() {
		return second;
	}
	
	public boolean reset() {
		if(second == 0)
			return false;
		second = 0;
		FxControllers.updateCooltime(second);
		return true;
	}
}
