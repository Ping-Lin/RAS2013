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
		timer.scheduleAtFixedRate(new HumpTask(), 0, 20);
	}
	
	class HumpTask extends TimerTask{
		public void run(){
			int trackNo = receivingArea.findLongestTrainTrack();
			double time;   //每做一次hump的前置時間
			if(trackNo != -1){   //有火車	
				time = receivingArea.receivingTrack[trackNo].train.get(0).timeAtReceivingArea + Constants.TECHNICAL_INSPECTION_TIME;    //每做一次hump的前置時間
				if(Test1.time<time){   
					Test1.time = time + Constants.HUMP_INTERVAL;
				}
				else{
					Test1.time += Constants.HUMP_INTERVAL;   //如果在等待被拉的火車已經在那邊等了,即用現行時間來繼續跑時間
				}
				
				for(Block b : receivingArea.receivingTrack[trackNo].train){   //找出receivingArea最長的火車
					b.timeStartHump = Test1.time;
					Test1.time += Constants.HUMP_RATE * receivingArea.receivingTrack[trackNo].train.size();
					b.timeEndPullBack = Test1.time;
					if(classificationArea.receiveHumpBlock(b) == false){   //沒有接收成功就結束掉
						System.out.println("classification tracks are full so fast, break program.");
						System.exit(1);
					}
				}
				receivingArea.receivingTrack[trackNo].ifEmpty = true;
				receivingArea.receivingTrack[trackNo].train.clear();
				
			}
			else{   //如果沒有火車就結束掉hump的工作(這邊可能要再修改，注意結果是不是有所有block都有抓到)
				System.out.println("There are no train at receivingArea.");
				timer.cancel();
			}
		}
	}
}
