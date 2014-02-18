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
	public int countRemoveTrack;   //計算有幾輛車來了
	
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
		for(int i=0 ; i<receivingTrack.length ; i++){
			if((receivingTrack[i].ifEmpty==true) && (countRemoveTrack > 0)
					&& (blockList.get(0).get(0).timeAtReceivingArea <= Test1.time + Constants.HUMP_INTERVAL)
					&& ((blockList.get(0).get(0).timeAtReceivingArea <= Test1.time1P) || 
						(blockList.get(0).get(0).timeAtReceivingArea <= Test1.time2P))){
				if(blockList.get(0).get(0).timeAtReceivingArea >= receivingTrack[i].humpTime){
					System.out.println("!!!!" + receivingTrack[i].humpTime);
					receivingTrack[i].train = blockList.get(0);
					for(Block b : receivingTrack[i].train){   //設定每一個car的receiving track為當前的track
						b.receivingTrackNo = i;
					}
					receivingTrack[i].ifEmpty = false;
					blockList.remove(0);
					countRemoveTrack--;
				}
			}
		}
	}
	
	/*
	 * 找出軌道裡有最長的火車編號並回傳序列由大到小，
	 * 若回傳第一個為-1表示沒東西了
	 */
	public ArrayList<Integer> findLongestTrainTrack(){
		ArrayList<Integer> maxId = new ArrayList<Integer>();   //最長火車的id序列
		
		for(int i=0 ; i<receivingTrack.length ; i++){
			int count = maxId.size();
			for(int j=maxId.size()-1 ; j>=0 ; j--){
				if(receivingTrack[i].ifEmpty == false){
					
					if(receivingTrack[i].train.size() > receivingTrack[maxId.get(j)].train.size()){
						count = j;
					}
				}
			}
			if(receivingTrack[i].ifEmpty == false)
				maxId.add(count, i);
		}
	
		if(maxId.isEmpty()){   //如果為空
			maxId.add(-1);
		}
		else{
			for(int i: maxId)
			System.out.println("%%" + receivingTrack[i].train.size());
		}
		return maxId;
	}
	
	public void run(){
		moveInTrain();
	}
}
