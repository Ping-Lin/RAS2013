import java.util.ArrayList;

/**
 * 此Class為ReceivingArea，
 * 存放軌道以及其一些設限
 * 
 * @author Ping
 */
public class ReceivingArea implements Runnable{
	Track[] receivingTrack;
	ArrayList<ArrayList<Block>> blockList;
	private int countRemoveTrack;   //計算有幾輛車來了
	
	public ReceivingArea(ArrayList<ArrayList<Block>> BL){
		receivingTrack = new Track[Constants.RECEIVING_TRACKS_NUMBER];
		for(int i=0 ; i<receivingTrack.length ; i++)   //new 空間
			receivingTrack[i] = new Track();
		blockList = BL;
		countRemoveTrack = blockList.size();
	}
	/*
	 * 假想有一個隱形的引擎把火車拉向receiving area
	 */
	public void moveInTrain(){
		int j=0;
		for(Track rt : receivingTrack){
			if(rt.ifEmpty==true && countRemoveTrack>0 && blockList.get(0).get(0).timeAtReceivingArea <= Test1.time
					&& (blockList.get(0).get(0).timeAtReceivingArea <= Test1.time1P || 
						blockList.get(0).get(0).timeAtReceivingArea <= Test1.time2P)){
				System.out.println("!!!!");
				rt.train = blockList.get(0);
				for(Block b : rt.train){   //設定每一個car的receiving track為當前的track
					b.receivingTrackNo = j;
				}
				rt.ifEmpty = false;
				blockList.remove(0);
				countRemoveTrack--;
				break;
			}
			j++;
		}
	}
	
	/*
	 * 找出軌道裡有最長的火車編號並回傳(int)，
	 * 若回傳-1表示沒東西了
	 */
	public int findLongestTrainTrack(){
		moveInTrain();
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
	public void run(){
		moveInTrain();
		if(countRemoveTrack == 0){
			try {
				Thread.sleep(1000);
				System.out.println("[Receiving Area]All trains have come");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
