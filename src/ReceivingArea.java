import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 此Class為ReceivingArea，
 * 存放軌道以及其一些設限
 * 
 * @author Ping
 */
public class ReceivingArea {
	Track[] receivingTrack;
	ArrayList<ArrayList<Block>> blockList;
	private Timer timer;
	private int countRemoveTrack;   //計算有幾輛車來了
	
	public ReceivingArea(ArrayList<ArrayList<Block>> BL){
		receivingTrack = new Track[Constants.RECEIVING_TRACKS_NUMBER];
		for(int i=0 ; i<receivingTrack.length ; i++)   //new 空間
			receivingTrack[i] = new Track();
		blockList = BL;
		countRemoveTrack = blockList.size();
		timer = new Timer();
		timer.scheduleAtFixedRate(new ReceivingTask(), 0, 300);
	}
	/*
	 * 假想有一個隱形的引擎把火車拉向receiving area
	 */
	public void moveInTrain(){
		int j=0;
		for(Track rt : receivingTrack){
			for(int i=0 ; i<blockList.size() ; i++){
				if(rt.ifEmpty==true && blockList.get(i).get(0).timeAtReceivingArea <= Test1.time){
					rt.train = blockList.get(i);
					for(Block b : rt.train){   //設定每一個car的receiving track為當前的track
						b.receivingTrackNo = j;
					}
					rt.ifEmpty = false;
					blockList.remove(i);
					countRemoveTrack--;
					break;
				}
			}
			j++;
		}
	}
	
	/*
	 * 找出軌道裡有最長的火車編號並回傳(int)，
	 * 若回傳-1表示沒東西了
	 */
	public int findLongestTrainTrack(){
		int max = Integer.MIN_VALUE;
		int maxId = -1;   //最長火車的id
		int i=0;
		for(Track rt : receivingTrack){
			if(rt.ifEmpty == false){
				if(rt.train.size() > max){
					max = rt.train.size();
					maxId =i;
				}		
			}
			i++;
		}
		return maxId;
	}
	/*
	 * 持續做接收車子的動作
	 */
	class ReceivingTask extends TimerTask{
		public void run(){
			moveInTrain();
			if(countRemoveTrack == 0){
				System.out.println("[Receiving Area]All trains have come");
				timer.cancel();
			}
		}
	}
}
