import java.util.ArrayList;;

public class PullBack implements Runnable{
	ClassificationArea classificationArea;
	OutboundArea outboundArea;
	ArrayList<Integer> tmpIdCombine;
	ArrayList<Block> train;   //要送去outboundArea的火車組合
	int pullBackNumber = 0;
	int count;   //用來計算第一次的時間
	
	public PullBack(ClassificationArea ca, OutboundArea oa, int pN) {
		classificationArea = ca;
		outboundArea = oa;
		tmpIdCombine = new ArrayList<Integer>();
		train = new ArrayList<Block>();
		this.pullBackNumber = pN;
		count = 0;
	}

	public void run(){
		boolean ifBreak = false;
		train.clear();   //記得清掉
		tmpIdCombine.clear();
		tmpIdCombine = classificationArea.findTrainCombine();
		double time=0.0;

		if(tmpIdCombine.isEmpty() == false && tmpIdCombine.get(0) == -1){  //classification不能出去
			System.out.println("[Pullback]Train in classification track are too small to go out.");
			updateTime(getTime()+0.1f);
			return;
		}
		else if(tmpIdCombine.isEmpty() == false){   //classification可以拉
			count++;
			
			for(int tic : tmpIdCombine){
				if(classificationArea.classificationTrack[tic].ifPullBackPull == true){   //這段是在檢查是否同時有兩個pull back拉同個地方
					ifBreak=true;
					break;
				}
				classificationArea.classificationTrack[tic].ifPullBackPull = true;   //防止hump又拉同屬性進來
				train.addAll(classificationArea.classificationTrack[tic].train);
			}
			
			if(count == 1){   //第一次的時間設置
				time = findBlockLatestTime(train);
				updateTime(time);
			}
			/*for(Block b : train)
				System.out.print(b.blockName + ",");
			System.out.println("\n" + train.size() + "@@");*/
			if(ifBreak == false){
				//時間處理的部分
				time = findBlockLatestTime(train);
				if(getTime()>time)
					time = getTime();
				
				System.out.println("[Pullback]Start: " + time);
				for(Block b: train){
					b.timeStartPullBack = time;
					b.timeEndPullBack = time + Constants.PULL_BACK_INTERVAL + Constants.PULL_BACK_MULTI_EACH_ADDITIONAL*1.0f*(tmpIdCombine.size()-1);
					b.pullBackEngineNo = pullBackNumber;
				}
				
				if(outboundArea.receivePullBackBlock(train)==false){   //如果接收失敗
					System.out.println("[Pullback]Outbound Area is full");
				}
				else{   //如果接收成功再把classification area的部分清掉
					//接收成功在改變時間
					updateTime(time + Constants.PULL_BACK_INTERVAL + Constants.PULL_BACK_MULTI_EACH_ADDITIONAL*1.0f*(tmpIdCombine.size()-1));
					classificationArea.updateTime(getTime());
					classificationArea.timePullBack.add(getTime());
					
					for(int tic : tmpIdCombine){
						classificationArea.classificationTrack[tic].train.clear();
						classificationArea.classificationTrack[tic].ifEmpty = true;						
					}
					System.out.println("Pull back Number: " + pullBackNumber + "\nCount: " + train.size());
					for(Block a : train)
						System.out.print(a.blockName + ",");
					System.out.println("\n");
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			for(int tic : tmpIdCombine)   //最後把操控權回復
				classificationArea.classificationTrack[tic].ifPullBackPull = false;
		}
		else{   //沒東西拉了，GG結束
			System.out.println("[Pull Back] There is no train at classification area.");
			updateTime(getTime()+0.1f);
		}
	}
	
	void updateTime(double t){
		if(pullBackNumber == 1){   //第一個pull back
			if(Test1.time1P < t)
				Test1.time1P = t;
		}
		else{   //第二個pull back
			if(Test1.time2P < t)
				Test1.time2P = t;
		}
	}
	
	double getTime(){
		if(pullBackNumber == 1){   //第一個pull back
			return Test1.time1P;
		}
		else{   //第二個pull back
			return Test1.time2P;
		}
	}
	
	double findBlockLatestTime(ArrayList<Block> train){
		double latestT=0.0;
		for(Block b : train){
			if(b.timeEndHump > latestT)
				latestT = b.timeEndHump;
		}
		return latestT;
	}
}

