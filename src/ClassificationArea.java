import java.util.Timer;
import java.util.TimerTask;

/*
 * 此class定義有關分類軌道的事項
 * 
 *  @author Ping
 */
public class ClassificationArea {
	private Timer timer;
	Track[] classificationTrack;
	public ClassificationArea(){
		classificationTrack = new Track[Constants.CLASSIFICATION_TRACKS_NUMBER];
		
		timer = new Timer();
		timer.scheduleAtFixedRate(new ClassificationTask(), 0, 10);
	}
	
	class ClassificationTask extends TimerTask{
		public void run(){
			
		}
	}
}
