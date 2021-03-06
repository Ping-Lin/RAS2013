import java.util.ArrayList;

public class OutboundArea implements Runnable{
	Track[] outboundTrack;
	int count;   //算出去的車子數
	double time;
	ArrayList<ArrayList<Block>> outboundBlockList;
	
	public OutboundArea(){
		outboundTrack = new Track[Constants.DEPARTURE_TRACKS_NUMBER];
		for(int i=0 ; i<outboundTrack.length ; i++)   //new 空間
			outboundTrack[i] = new Track();
		count = 0;
		time = 0.0;
		outboundBlockList = new ArrayList<>();
	}
	
	/*
	 * 將block存放在outbound track，並將track是否為空設定為false。
	 * true為成功接收，false為失敗
	 */
	public boolean receivePullBackBlock(ArrayList<Block> b){
		//塞空的軌道
		for(Track ot : outboundTrack){
			if((ot.ifEmpty == true) && (b.get(0).timeStartPullBack >= ot.pullBackTime)){
				ot.train.addAll(b);
				ot.ifEmpty = false;
				return true;
			}
		}
		return false;
	}
	
	/*
	 * 跑go out train, 結束跟pull back同步
	 */
	public void run(){
		goOutTrain();
	}
	
	/*
	 * 讓火車出去吧
	 */
	public void goOutTrain(){
		double t;
		int id = firstGoOutTrainId();
		if(id!=-1){		
			t = outboundTrack[id].train.get(0).timeEndPullBack + Constants.TECHNICAL_INSPECTION_TIME;    //每做一次out train的前置時間
			if(time+Constants.OUTBOUND_TRAIN_INTERVAL<=t){
				time = t + Constants.OUTBOUND_TRAIN_INTERVAL;
			}
			else{
				time += Constants.OUTBOUND_TRAIN_INTERVAL;   //如果在等待被拉的火車已經在那邊等了,即用現行時間來繼續跑時間
			}
			System.out.println("[Outbound]" + time);
			
			for(Block b : outboundTrack[id].train){
				b.outboundTrainNo = count;
				b.departureTrackNo = id;
				b.timeDepartureAtDepartureArea = time;			
				b.departureDay = (int)(time/24)+ 1;
			}
			outboundTrack[id].pullBackTime = time;
			ArrayList <Block> tmpBlockList = new ArrayList<Block>();
			tmpBlockList.addAll(outboundTrack[id].train);
			outboundBlockList.add(tmpBlockList);

			outboundTrack[id].train.clear();
			outboundTrack[id].ifEmpty = true;
			count++;
		}
	}
	
	/*
	 * 找最先出去的火車的id
	 */
	private int firstGoOutTrainId(){
		double minTime=Double.MAX_VALUE;
		int goOutId=-1;
		int i=0;
		for(Track t : outboundTrack){   //找要最先出去的軌道
			if(t.ifEmpty == false){
				if(t.train.get(0).timeEndPullBack < minTime){
					minTime = t.train.get(0).timeEndPullBack;
					goOutId = i;
				}
			}
			i++;
		}
		return goOutId;
	}
	
	/*
	 * 如果傳進來的時間比每個軌道的pullBack時間都還小,則找到空的軌道且回傳最近時間的那個時間
	 */
	double smallestPullBackTimeOfEmptyTrack(double pt){
		double min = Double.MAX_VALUE;
		boolean empty = false;
		for(Track t : outboundTrack){
			if(t.ifEmpty == true){
				empty = true;
				if(pt >= t.pullBackTime)
					return pt;
				if(t.pullBackTime < min){
					min = t.pullBackTime;
				}
			}
		}
		if(empty == true)
			return min;
		else
			return pt;
	}
}
