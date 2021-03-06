import java.util.ArrayList;

/*
 * 此class定義有關分類軌道的事項
 * 
 *  @author Ping
 */
public class ClassificationArea {
	Track[] classificationTrack;
	private double lastTime;
	ArrayList<Double> timePullBack;
	
	public ClassificationArea(){
		classificationTrack = new Track[Constants.CLASSIFICATION_TRACKS_NUMBER];
		for(int i=0 ; i<classificationTrack.length ; i++)   //new 空間
			classificationTrack[i] = new Track();
		lastTime = 0.0;
		timePullBack = new ArrayList<Double>();
	}
	
	/*
	 * 將block存放在classification track，並將track是否為空設定為false。
	 * true為成功接收，false為失敗
	 */
	public boolean receiveHumpBlock(ArrayList<Block> bl){
		boolean ifAdd=false;
		int trackNo;
		ArrayList<Integer> addId = new ArrayList<Integer>();
		for(Block b : bl){
			ifAdd = false;
			trackNo=0;
			for(Track ct : classificationTrack){   //先找有沒有符合同樣block name的track，注意不能超出其capacity
				if((ct.ifEmpty == false) && ((ct.train.size()+1) <= Constants.CLASSIFICATION_TRACKS_CAPACITY) && (ct.ifPullBackPull == false)){
					if(ct.train.get(0).blockName.equals(b.blockName)){
						ct.train.add(b);
						ifAdd = true;
						addId.add(trackNo);
						ct.btt.get(ct.btt.size()-1).endingTime = b.timeEndHump;
						break;   //成功就回傳
					}
				}
				trackNo++;
			}
			//如果找不到可以add進去的track的話，就另闢空的track吧
			trackNo=0;
			if(ifAdd == false){
				for(Track ct : classificationTrack){
					if(ct.ifEmpty == true){
						ct.train.add(b);
						ct.ifEmpty = false;   //闢新的track要記錄時間，並將東西記錄到blockToTrack object(btt)

						if(ct.btt.size()>0)
							ct.btt.get(ct.btt.size()-1).endingTime = b.timeStartHump-Constants.HUMP_INTERVAL;   //把之前的ending time存進去
						BlockToTrack tmp = new BlockToTrack();
						tmp.trackNo = trackNo;
						tmp.blockName = b.blockName;
						tmp.dayNo = ((int)b.timeStartHump-1)/24+1;
						tmp.startingTime = b.timeStartHump-Constants.HUMP_INTERVAL;
						ct.btt.add(tmp);
						tmp = null;
						
						ifAdd = true;
						addId.add(trackNo);
						break;
					}
					trackNo++;
				}
			}
			
			//如果都不能加就不能加這串火車
			if(ifAdd == false){
				//把加進去的刪回來
				for(int i : addId){
					if(classificationTrack[i].train.size()-1!=0)
						classificationTrack[i].train.remove(classificationTrack[i].train.size()-1);
					else
						classificationTrack[i].train.remove(0);
				}
				addId = null;
				return false;
			}
		}
		return true;
	}
	
	/*
	 * 找出軌道裡當下最長且可以滿足最低下限就出去的準則，讓Combine減少，
	 * 的火車組合(與ReceivingArea的不同)編號並回傳(ArrayList<int>)，
	 * 若List為空表示沒東西了。
	 * 可以用一個queue來記錄現在有的種類
	 */
	public ArrayList<Integer> findTrainCombine(){
		int max = Integer.MIN_VALUE;
		boolean ifAllTrackEmpty = true;
		ArrayList<Integer> maxIdCombine = new ArrayList<Integer>();   //最長火車組合的id list
		ArrayList<Integer> tmpIdCombine = new ArrayList<Integer>();
		ArrayList<String> tmpNameCombine = new ArrayList<String>();
		BlockValues bl = new BlockValues();
		
		for(int i=0 ; i<classificationTrack.length ; i++){   //插入排序法
			int count = tmpIdCombine.size();
			for(int j=tmpIdCombine.size()-1 ; j>=0 ; j--){
				if(classificationTrack[i].ifEmpty == false){
					ifAllTrackEmpty = false;
					if(classificationTrack[i].train.size() > classificationTrack[tmpIdCombine.get(j)].train.size()){
						count = j;
					}
				}
			}
			if(classificationTrack[i].ifEmpty == false)
				tmpIdCombine.add(count, i);
		}		
		
		if(ifAllTrackEmpty == true){   //加速程式進行，如果為空就回傳
			maxIdCombine.clear();
			return maxIdCombine;
		}
		
		for(int i=0 ; i<tmpIdCombine.size() ; i++){   //如果單一軌道可以出去就直接出去，依序增加
			maxIdCombine.clear();
			maxIdCombine.add(tmpIdCombine.get(i));
			tmpNameCombine.clear();
			tmpNameCombine.add(classificationTrack[tmpIdCombine.get(i)].train.get(0).blockName);
			max = classificationTrack[tmpIdCombine.get(i)].train.size();
			for(int j=i+1 ; j<tmpIdCombine.size() ; j++){
				if(max >= Constants.MIN_OUTBOUND_TRAIN_NUMBER){   //如果可以出去(滿足最小出去原則)
					return maxIdCombine;
				}
				else{
					tmpNameCombine.add(classificationTrack[tmpIdCombine.get(j)].train.get(0).blockName);
					if(bl.ifBlockCombination(tmpNameCombine)){   //判斷是否可以結合,如果可以結合就加進去
						maxIdCombine.add(tmpIdCombine.get(j));
						max += classificationTrack[tmpIdCombine.get(j)].train.size();
					}
					else{   //不行就把tmp的除掉
						if(tmpNameCombine.size()>0)
							tmpNameCombine.remove(tmpNameCombine.size()-1);
					}
				}
			}
			if(max >= Constants.MIN_OUTBOUND_TRAIN_NUMBER){   //最後一次，如果可以出去(滿足最小出去原則)
				return maxIdCombine;
			}
		}
		
		//最後記得釋放
		tmpIdCombine = null;
		tmpNameCombine = null;
		bl = null;
		/*
		 * 加上最小限制
		 */
		if(max < Constants.MIN_OUTBOUND_TRAIN_NUMBER){
			maxIdCombine.clear();
			maxIdCombine.add(-1);
		}
			
		return maxIdCombine;
	}
	
	/*
	 * 更新classification time的最後時間
	 */
	void updateTime(double time){
		if(time > lastTime)   //如果有大於原本的時間在更新
			lastTime = time;
	}
	
	/*
	 * 取得classification time的最後時間
	 */
	double getTime(){
		return lastTime;
	}
	
	
	
	
//	public ArrayList<Integer> findLongestTrainTrackCombine(){
//		int max = Integer.MIN_VALUE;
//		boolean ifAllTrackEmpty = true;
//		ArrayList<Integer> maxIdCombine = new ArrayList<Integer>();   //最長火車組合的id list
//		int i=0;
//		for(Track ct : classificationTrack){   //先找看看單一有沒有最長的
//			if(ct.ifEmpty == false){
//				ifAllTrackEmpty = false;
//				if(ct.train.size() > max){
//					max = ct.train.size();
//					maxIdCombine.clear();
//					maxIdCombine.add(i);
//				}		
//			}
//			i++;
//		}
//		if(ifAllTrackEmpty == true)   //加速程式進行
//			return maxIdCombine;
//		
//		//同type的組合
//		int count=0;
//		for(int sm : findTheSameMax()){
//			count += classificationTrack[sm].train.size();
//		}
//		if(count > max){
//			max = count;
//			maxIdCombine = findTheSameMax();
//		}
//		
//		//不同type的組合
//		count=0;
//		for(int cm : findCombineMax()){
//			count += classificationTrack[cm].train.size();
//		}
//		if(count > max){
//			max = count;
//			maxIdCombine = findCombineMax();
//		}
//		/*
//		 * 這段是在考慮是否要加最小限制
//		 */
//		/*if(max < Constants.MIN_OUTBOUND_TRAIN_NUMBER){
//			maxIdCombine.clear();
//			maxIdCombine.add(-1);
//		}*/
//		
//		return maxIdCombine;
//	}
//	
//	private ArrayList<Integer> findTheSameMax(){
//		int max = Integer.MIN_VALUE;
//		int count=0;
//		ArrayList<Integer> maxIdCombine = new ArrayList<Integer>();
//		ArrayList<Integer> tmpIdCombine = new ArrayList<Integer>();	
//		for(int i=0 ; i<classificationTrack.length; i++){
//			count = classificationTrack[i].train.size();
//			tmpIdCombine.add(i);
//			for(int j=i+1 ; j<classificationTrack.length ; j++){
//				if(classificationTrack[i].ifEmpty == false && classificationTrack[j].ifEmpty == false){
//					if(classificationTrack[i].train.get(0).blockName.equals(classificationTrack[j].train.get(0).blockName)){
//						count += classificationTrack[j].train.size();
//						tmpIdCombine.add(j);
//					}
//				}
//			}
//			if(count > max){
//				max = count;
//				maxIdCombine = tmpIdCombine;
//			}
//			tmpIdCombine.clear();
//		}
//		return maxIdCombine;
//	}
//
//	private ArrayList<Integer> findCombineMax(){
//		int max = Integer.MIN_VALUE;
//		ArrayList<Integer> maxIdCombine = new ArrayList<Integer>();   //存現在有的總類，編號
//		ArrayList<Integer> idCombine = new ArrayList<Integer>();   //存現在有的總類，編號
//		ArrayList<String> blockNameList = new ArrayList<String>();   //存現在有的總類，block Name
//		BlockValues blockValues = new BlockValues();
//		ArrayList<Integer>tmpIdCombine = new ArrayList<Integer>();   //用來計算每一種組合的長度，id
//		ArrayList<Integer>tmpCountNumber = new ArrayList<Integer>();   //用來計算每一種組合的長度，數量
//		int i=0;
//		for(Track ct : classificationTrack){
//			if(ct.ifEmpty == false){
//				blockNameList.add(ct.train.get(0).blockName);   //將block name加入list內，方便後面組合
//				idCombine.add(i);
//			}
//			i++;
//		}		
//		int count = 0;
//		for(String b : blockValues.blockCombination){
//			i=0;
//			for(String bc : blockNameList){   //bc為block list current
//				if(b.contains(bc) == true){
//					tmpIdCombine.add(idCombine.get(i));
//				}
//				i++;
//			}
//
//			count = 0;
//			for(int c : tmpIdCombine){
//				count += classificationTrack[c].train.size();   //先把全部組合加起來
//				tmpCountNumber.add(classificationTrack[c].train.size());
//			}
//			
//			while(count > Constants.MAX_OUTBOUND_TRAIN_NUMBER){   //若超過上限
//				//從最小的開始刪除
//				int min = Integer.MAX_VALUE;
//				int j=0;
//				int tmpN = -1;
//				for(int tcn : tmpCountNumber){
//					if(tcn < min){
//						min = tcn;
//						tmpN = j;
//					}
//					j++;
//				}
//				count -= tmpCountNumber.get(tmpN);
//				tmpCountNumber.remove(tmpN);
//				tmpIdCombine.remove(tmpN);
//			}
//			//最後比較組合的是否為最大
//			if(count > max){
//				max = count;
//				maxIdCombine.clear();
//				maxIdCombine.addAll(tmpIdCombine);
//			}
//			//最後記得要清掉暫存
//			tmpIdCombine.clear();
//			tmpCountNumber.clear();
//		}
//		
//		return maxIdCombine;
//	}
}
