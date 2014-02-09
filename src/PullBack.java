import java.util.Timer;
import java.util.TimerTask;

public class PullBack {
	ClassificationArea classificationArea;
	OutboundArea outboundArea;
	private Timer timer;
	
	public PullBack(ClassificationArea ca, OutboundArea oa){
		classificationArea = ca;
		outboundArea = oa;
		timer = new Timer();
		
		timer.scheduleAtFixedRate(new PullBackTask(), 0, 20);
	}
	
	class PullBackTask extends TimerTask{
		public void run(){
			classificationArea.findLongestTrainTrack()lk
		}
	}
}
