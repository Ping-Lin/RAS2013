import java.util.Timer;
import java.util.TimerTask;

/**
 * 此Class為ReceivingArea，
 * 存放軌道以及其一些設限
 * 
 * @author Ping
 */
public class Hump {
	private Timer timer;
	ReceivingArea receivingArea;
	ClassificationArea classificationArea;
	
	public Hump(ReceivingArea ra, ClassificationArea ca){
		receivingArea = ra;
		classificationArea = ca;
		
		timer = new Timer();
		timer.scheduleAtFixedRate(new HumpTask(), 0, 10);
	}
	
	class HumpTask extends TimerTask{
		public void run(){
			
		}
	}
}
