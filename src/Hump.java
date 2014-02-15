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
		timer.scheduleAtFixedRate(new HumpTask(), 0, 300);
	}
	
	class HumpTask extends TimerTask{
		public void run(){
			int trackNo = receivingArea.findLongestTrainTrack();
			double time;   //用來比較classification time的時間
			if(trackNo != -1){   //有火車			
				time = receivingArea.receivingTrack[trackNo].train.get(0).timeAtReceivingArea + Constants.TECHNICAL_INSPECTION_TIME;    //每做一次hump的前置時間
				if(Test1.time+Constants.HUMP_INTERVAL<=time){
					Test1.time = time + Constants.HUMP_INTERVAL;
				}
				else{
					Test1.time += Constants.HUMP_INTERVAL;   //如果在等待被拉的火車已經在那邊等了,即用現行時間來繼續跑時間
				}
				System.out.println("[Hump]" + Test1.time);
				for(Block b : receivingArea.receivingTrack[trackNo].train){   //找出receivingArea最長的火車
					b.timeStartHump = Test1.time;
					b.timeEndHump = Test1.time + Constants.HUMP_RATE * receivingArea.receivingTrack[trackNo].train.size();
					while(classificationArea.receiveHumpBlock(b) == false){   //沒有接收成功就結束掉或等待
						
						System.out.println("classification tracks are full so fast, (wait or)break program.");
						try {
							Thread.sleep(1000);						
							Test1.time = findClosestTime(Test1.time);   //如果有卡住的話，時間用classification area紀錄的最後時間(pull back抓走的時間)
							b.timeStartHump = Test1.time;
							b.timeEndHump = Test1.time + Constants.HUMP_RATE * receivingArea.receivingTrack[trackNo].train.size();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						//System.exit(1);
					}
				}
				Test1.time += Constants.HUMP_RATE * receivingArea.receivingTrack[trackNo].train.size();
				classificationArea.updateTime(Test1.time);
				receivingArea.receivingTrack[trackNo].train.clear();
				receivingArea.receivingTrack[trackNo].ifEmpty = true;			
			}
			else{   //如果沒有火車就結束掉hump的工作(這邊可能要再修改，注意結果是不是有所有block都有抓到)
				System.out.println("[Hump]There is no train at receivingArea.");
				
				timer.cancel();
			}
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
