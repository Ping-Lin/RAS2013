import java.util.ArrayList;
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
		for(int i=0 ; i<classificationTrack.length ; i++)   //new 空間
			classificationTrack[i] = new Track();
		timer = new Timer();
		timer.scheduleAtFixedRate(new ClassificationTask(), 0, 10);
	}
	
	class ClassificationTask extends TimerTask{
		public void run(){

		}
	}
	/*
	 * 將block存放在classification track，並將track是否為空設定為false。
	 * true為成功接收，false為失敗
	 */
	public boolean receiveHumpBlock(Block b){
		int trackNo=0;
		for(Track ct : classificationTrack){   //先找有沒有符合同樣block name的track，注意不能超出其capacity
			if(ct.ifEmpty == false && ct.train.size()+1 <= Constants.CLASSIFICATION_TRACKS_CAPACITY){
				if(ct.train.get(0).blockName.equals(b.blockName)){
					classificationTrack[trackNo].train.add(b);
					return true;   //成功就回傳
				}
			}
			trackNo++;
		}
		//如果找不到可以add進去的track的話，就另闢空的track吧
		trackNo = 0;
		for(Track ct : classificationTrack){
			if(ct.ifEmpty == true){
				classificationTrack[trackNo].train.add(b);
				classificationTrack[trackNo].ifEmpty = false;
				return true;
			}
			trackNo++;
		}
		return false;
	}
}
