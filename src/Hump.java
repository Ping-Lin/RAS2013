import java.util.ArrayList;


/**
 * 此Class為ReceivingArea，
 * 存放軌道以及其一些設限
 * 
 * @author Ping
 */
public class Hump implements Runnable{
	ReceivingArea receivingArea;
	ClassificationArea classificationArea;
	ArrayList<ArrayList<Block>> humpBlockList;
	int count;   //時間用
	
	public Hump(ReceivingArea ra, ClassificationArea ca){
		receivingArea = ra;
		classificationArea = ca;
		humpBlockList = new ArrayList<>();
	}

	public void run(){
		ArrayList<Integer> trackNo = receivingArea.findLongestTrainTrack();
		double time;   //用來比較classification time的時間
		ArrayList<Block> tmpList = new ArrayList<Block>();   //temp block list
		boolean ifSuccessAccept = false;
		for(int i=0 ; i<trackNo.size() ; i++){
			if(ifSuccessAccept == true)   //如果成功接收就跳掉
				return;
			
			if(trackNo.get(0).equals(-1)==false){   //有火車			
				time = receivingArea.receivingTrack[trackNo.get(i)].train.get(0).timeAtReceivingArea + Constants.TECHNICAL_INSPECTION_TIME;    //每做一次hump的前置時間
				
				if(Test1.time+Constants.HUMP_INTERVAL<=time){
					time = time + Constants.HUMP_INTERVAL;
				}
				else{
					time = Test1.time + Constants.HUMP_INTERVAL;   //如果在等待被拉的火車已經在那邊等了,即用現行時間來繼續跑時間
				}
				for(Block b : receivingArea.receivingTrack[trackNo.get(i)].train){   //找出receivingArea最長的火車
					b.timeStartHump = time;
					b.timeEndHump = time + Constants.HUMP_RATE * receivingArea.receivingTrack[trackNo.get(i)].train.size();
				}
				if(classificationArea.receiveHumpBlock(receivingArea.receivingTrack[trackNo.get(i)].train) == false){   //沒有接收成功就結束掉或等待
					System.out.println("[Hump]classification tracks are full. Change the train to hump.");
					continue;
				}
				else{   //接收成功
					for(Block b : receivingArea.receivingTrack[trackNo.get(i)].train){ 
						tmpList.add(b);   //加進去tmp block list
					}
					Test1.time = time;
					System.out.println("[Hump]Start:" + Test1.time);
					receivingArea.receivingTrack[trackNo.get(i)].humpTime = Test1.time;
					Test1.time += Constants.HUMP_RATE * receivingArea.receivingTrack[trackNo.get(i)].train.size();
					classificationArea.updateTime(Test1.time);
					receivingArea.receivingTrack[trackNo.get(i)].train = null;
					receivingArea.receivingTrack[trackNo.get(i)].ifEmpty = true;
					humpBlockList.add(tmpList);
					tmpList = null;
					ifSuccessAccept = true;
					count++;
				}
			}
			else{   //如果沒有火車就結束掉hump的工作(這邊可能要再修改，注意結果是不是有所有block都有抓到)			
				System.out.println("[Hump]There is no train at receivingArea now.");
				if(Test1.time < receivingArea.blockList.get(0).get(0).timeAtReceivingArea)
					Test1.time = receivingArea.blockList.get(0).get(0).timeAtReceivingArea;
				return;
			}
		}
		if(ifSuccessAccept == false){   //如果接收失敗就加時間跳掉
			System.out.println("[Hump]train add into classification area error.");
			if(receivingArea.blockList.isEmpty()==false)
				Test1.time+=1.0f/60;
		}
	}
	
	/*
	 * 如果hump卡住，要更新時間，須利用最接近的pullback拉走的時間去更新
	 */
	double findClosestTime(double ht){
		for(Double t : classificationArea.timePullBack){
			if(t > ht){
				return t;
			}
		}
		return ht;
	}
}
