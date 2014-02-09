import java.util.ArrayList;


/*
 * 此class定義有關分類軌道的事項
 * 
 *  @author Ping
 */
public class ClassificationArea {
	Track[] classificationTrack;
	public ClassificationArea(){
		classificationTrack = new Track[Constants.CLASSIFICATION_TRACKS_NUMBER];
		for(int i=0 ; i<classificationTrack.length ; i++)   //new 空間
			classificationTrack[i] = new Track();
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
	
	/*
	 * 找出軌道裡有最長的火車組合(與ReceivingArea的不同)編號並回傳(ArrayList<int>)，
	 * 若List為空表示沒東西了
	 */
	public ArrayList<Integer> findLongestTrainTrackCombine(){
		int max = Integer.MIN_VALUE;
		ArrayList<Integer> maxIdCombine = new ArrayList<Integer>();   //最長火車組合的id list
		int i=0;
		for(Track ct : classificationTrack){   //先找看看單一有沒有最長的
			if(ct.ifEmpty == false){
				if(ct.train.size() > max){
					max = ct.train.size();
					maxIdCombine.clear();
					maxIdCombine.add(i);
				}		
			}
			i++;
		}
		//同type的組合
		int count=0;
		for(int sm : findTheSameMax()){
			count += classificationTrack[sm].train.size();
		}
		if(count > max){
			max = count;
			maxIdCombine = findTheSameMax();
		}
		
		//不同type的組合
		count=0;
		for(int cm : findCombineMax()){
			count += classificationTrack[cm].train.size();
		}
		if(count > max){
			max = count;
			maxIdCombine = findCombineMax();
		}
		
		return maxIdCombine;
	}
	
	private ArrayList<Integer> findTheSameMax(){
		int max = Integer.MIN_VALUE;
		int count=0;
		ArrayList<Integer> maxIdCombine = new ArrayList<Integer>();
		ArrayList<Integer> tmpIdCombine = new ArrayList<Integer>();	
		for(int i=0 ; i<classificationTrack.length; i++){
			count = classificationTrack[i].train.size();
			tmpIdCombine.add(i);
			for(int j=i+1 ; j<classificationTrack.length ; j++){
				if(classificationTrack[i].ifEmpty == false && classificationTrack[j].ifEmpty == false){
					if(classificationTrack[i].train.get(0).blockName.equals(classificationTrack[j].train.get(0).blockName)){
						count += classificationTrack[j].train.size();
						tmpIdCombine.add(j);
					}
				}
			}
			if(count > max){
				max = count;
				maxIdCombine = tmpIdCombine;
			}
			tmpIdCombine.clear();
		}
		return maxIdCombine;
	}
	
	private ArrayList<Integer> findCombineMax(){
		int max = Integer.MIN_VALUE;
		ArrayList<Integer> maxIdCombine = new ArrayList<Integer>();   //存現在有的總類，編號
		ArrayList<Integer> idCombine = new ArrayList<Integer>();   //存現在有的總類，編號
		ArrayList<String> blockNameList = new ArrayList<String>();   //存現在有的總類，block Name
		BlockValues blockValues = new BlockValues();
		ArrayList<Integer>tmpIdCombine = new ArrayList<Integer>();   //用來計算每一種組合的長度，id
		ArrayList<Integer>tmpCountNumber = new ArrayList<Integer>();   //用來計算每一種組合的長度，數量
		int i=0;
		for(Track ct : classificationTrack){
			if(ct.ifEmpty == false){
				blockNameList.add(ct.train.get(0).blockName);   //將block name加入list內，方便後面組合
				idCombine.add(i);
			}
			i++;
		}

		for(String b : blockValues.blockCombination){
			i=0;
			for(String bc : blockNameList){   //bc為block list current
				if(b.contains(bc) == true){
					tmpIdCombine.add(i);
				}
				i++;
			}

			int count = 0;
			for(int c : tmpIdCombine){
				count += classificationTrack[c].train.size();   //先把全部組合加起來
				tmpCountNumber.add(classificationTrack[c].train.size());
			}
			while(count > Constants.MAX_OUTBOUND_TRAIN_NUMBER){   //若超過上限
				//從最小的開始刪除
				int min = Integer.MAX_VALUE;
				int j=0;
				int tmpN = -1;
				for(int tcn : tmpCountNumber){
					if(tcn < min){
						min = tcn;
						tmpN = j;
					}
					j++;
				}
				count -= tmpCountNumber.get(tmpN);
				tmpCountNumber.remove(tmpN);
				tmpIdCombine.remove(tmpN);
			}
			//最後比較組合的是否為最大
			if(count > max){
				max = count;
				maxIdCombine.clear();
				maxIdCombine.addAll(tmpIdCombine);
			}
			//最後記得要清掉暫存
			tmpIdCombine.clear();
			tmpCountNumber.clear();
		}
		
		return maxIdCombine;
	}
}
