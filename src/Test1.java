import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import jxl.*;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Test1 {
	static double time=0.0;
	static double time1P = 0.0;   //第一個pull back的時間
	static double time2P = 0.0;   //第二個pull back的時間
	public static void main(String[] args) {
		ArrayList<ArrayList<Block>> blockList = new ArrayList<>();
		readExcelFile(blockList);   //讀完檔，開始跑receiving Area
		time = blockList.get(0).get(0).timeAtReceivingArea;   //用第一個時間當作time的初始
		time1P = blockList.get(0).get(0).timeAtReceivingArea;   //用第一個時間當作time的初始
		time2P = blockList.get(0).get(0).timeAtReceivingArea;   //用第一個時間當作time的初始
		ReceivingArea receivingArea = new ReceivingArea(blockList);
		ClassificationArea classificationArea = new ClassificationArea();
		Hump hump = new Hump(receivingArea, classificationArea);
		OutboundArea outboundArea = new OutboundArea();
		ScheduledExecutorService service = Executors.newScheduledThreadPool(4);

		service.scheduleAtFixedRate(receivingArea , 0, 250, TimeUnit.MILLISECONDS);
		service.scheduleAtFixedRate(hump , 120, 250, TimeUnit.MILLISECONDS);
		service.scheduleAtFixedRate(new PullBack(classificationArea, outboundArea, 1), 150, 250, TimeUnit.MILLISECONDS);
		service.scheduleAtFixedRate(new PullBack(classificationArea, outboundArea, 2), 172, 280, TimeUnit.MILLISECONDS);
		service.scheduleAtFixedRate(outboundArea ,170, 250, TimeUnit.MILLISECONDS);

		try {
			Thread.sleep(5000);
			service.shutdown();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		outPutInboundTrainInfo(hump.humpBlockList);
		outPutBlockToTrack(classificationArea.classificationTrack);
		outPutOutboundTrainInfo(outboundArea.outboundBlockList);
		outPutRailcarItinerary(outboundArea.outboundBlockList);
	}

	/*
	 * readExcelFile 為獨取Excel檔的function, excel檔格式副檔名須為xls
	 */
	public static void readExcelFile(ArrayList<ArrayList<Block>> blockList){
		
		try {
			InputStream excelFile = new FileInputStream("src/data_readme/Input_data_set_1.xls");   //要讀的檔案
			jxl.Workbook readWorkBook = Workbook.getWorkbook(excelFile);   //將其讀入workbook中
			Sheet readSheet = readWorkBook.getSheet("Inbound Train Info");   //讀sheet
			
			ArrayList<Block> tmpList = new ArrayList<Block>();   //temp block list
			for(int i=1 ; i<readSheet.getRows() ; i++){
				Block tmp = new Block();   //temp for block to save into			
				tmp.arrivalDay = Integer.parseInt(readSheet.getCell(0,i).getContents());
				tmp.trainId = Integer.parseInt(readSheet.getCell(1,i).getContents());
				NumberCell nc = (NumberCell)readSheet.getCell(2,i);
				tmp.timeAtReceivingArea = arrivalTimePlusDay(tmp.arrivalDay, nc.getValue());
				tmp.blockName = readSheet.getCell(3,i).getContents();
				tmp.carNo = Integer.parseInt(readSheet.getCell(4,i).getContents());
				
				if(tmp.carNo == 1 && i!=1){   //如果是下一台火車，就把原先的火車存進去block list裡面
					blockList.add(tmpList);
					tmpList = null;
					tmpList = new ArrayList<Block>();
				}
				tmpList.add(tmp);
				tmp = null;
			}
			blockList.add(tmpList);
			tmpList = null;
			
			readWorkBook.close();   //關檔
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* 
	 * inbound train information output
	 */
	public static void outPutInboundTrainInfo(ArrayList<ArrayList<Block>> bl){
		try{
			jxl.write.WritableWorkbook writeWorkBook = Workbook.createWorkbook(new File("Output_File.xls"));
			jxl.write.WritableSheet writeSheet = writeWorkBook.createSheet("inbound_train", 0);   //建立一個工作表名字，然後0表示為第一頁		
			writeSheet.addCell(new Label(0, 0, "Inbound Train No"));
			writeSheet.addCell(new Label(1, 0, "Day No"));
			writeSheet.addCell(new Label(2, 0, "Arriving time at receiving area"));
			writeSheet.addCell(new Label(3, 0, "Receiving track No"));
			writeSheet.addCell(new Label(4, 0, "Number of blocks"));
			writeSheet.addCell(new Label(5, 0, "Number of cars"));
			writeSheet.addCell(new Label(6, 0, "Destination of blocks (number of cars)"));
			writeSheet.addCell(new Label(7, 0, "Starting time of humping job"));
			writeSheet.addCell(new Label(8, 0, "Ending time of humping job"));
			
			
			for(int i=0 ; i<bl.size() ; i++){
				ArrayList<ArrayList<String>> tmpBlockName = countBlocks(bl.get(i));
				String destinationOfBlocks = new String();
				for(ArrayList<String> tb : tmpBlockName){
					destinationOfBlocks += tb.get(0) + "(" + tb.size() + ");";
				}
				writeSheet.addCell(new Label(0, i+1, "i" + bl.get(i).get(0).trainId));
				writeSheet.addCell(new Label(1, i+1, Integer.toString(bl.get(i).get(0).arrivalDay)));
				writeSheet.addCell(new Label(2, i+1, changeToFormatTime(bl.get(i).get(0).timeAtReceivingArea)));
				writeSheet.addCell(new Label(3, i+1, "R" + (bl.get(i).get(0).receivingTrackNo+1)));
				writeSheet.addCell(new Label(4, i+1, Integer.toString(tmpBlockName.size())));
				writeSheet.addCell(new Label(5, i+1, Integer.toString(bl.get(i).size())));
				writeSheet.addCell(new Label(6, i+1, destinationOfBlocks));
				writeSheet.addCell(new Label(7, i+1, changeToFormatTime(bl.get(i).get(0).timeStartHump)));
				writeSheet.addCell(new Label(8, i+1, changeToFormatTime(bl.get(i).get(0).timeEndHump)));
				
				destinationOfBlocks = null;
				tmpBlockName = null;
			}
			
			
			writeWorkBook.write();
			writeWorkBook.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
		
	}
	
	/* 
	 * Solution block to track output
	 */
	public static void outPutBlockToTrack(Track[] t){
		try{
			jxl.Workbook readWorkBook = jxl.Workbook.getWorkbook(new File("Output_File.xls"));   //唯讀的Excel工作薄物件
			jxl.write.WritableWorkbook writeWorkBook = Workbook.createWorkbook(new File("Output_File.xls"), readWorkBook);   //寫入
			jxl.write.WritableSheet writeSheet = writeWorkBook.createSheet("block_to_track_assignment", 1);   //建立一個工作表名字，然後0表示為第一頁		
			
			writeSheet.addCell(new Label(0, 0, "Track"));
			writeSheet.addCell(new Label(1, 0, "Block"));
			writeSheet.addCell(new Label(2, 0, "Day No"));
			writeSheet.addCell(new Label(3, 0, "Starting Time"));
			writeSheet.addCell(new Label(4, 0, "Ending time"));		
			
			for(int i=0 ; i<t.length ; i++){
				for(BlockToTrack bt : t[i].btt){
					writeSheet.addCell(new Label(0, i+1, "C" + (bt.trackNo + 1)));
					writeSheet.addCell(new Label(1, i+1, bt.blockName));
					writeSheet.addCell(new Label(2, i+1, Integer.toString(bt.dayNo)));
					writeSheet.addCell(new Label(3, i+1, changeToFormatTime(bt.startingTime)));
					writeSheet.addCell(new Label(4, i+1, changeToFormatTime(bt.endingTime)));
				}
			}
			
			writeWorkBook.write();
			writeWorkBook.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		}
		
	}
	
	/* 
	 * outbound train information
	 */
	public static void outPutOutboundTrainInfo(ArrayList<ArrayList<Block>> bl){
		try{
			jxl.Workbook readWorkBook = jxl.Workbook.getWorkbook(new File("Output_File.xls"));   //唯讀的Excel工作薄物件
			jxl.write.WritableWorkbook writeWorkBook = Workbook.createWorkbook(new File("Output_File.xls"), readWorkBook);   //寫入
			jxl.write.WritableSheet writeSheet = writeWorkBook.createSheet("outbound_train_info", 2);   //建立一個工作表名字，然後0表示為第一頁		
			
			writeSheet.addCell(new Label(0, 0, "outbound Train No"));
			writeSheet.addCell(new Label(1, 0, "Day No"));
			writeSheet.addCell(new Label(2, 0, "Pullback engine no"));
			writeSheet.addCell(new Label(3, 0, "Starting time of pullback job"));
			writeSheet.addCell(new Label(4, 0, "ending time of pullback job"));
			writeSheet.addCell(new Label(5, 0, "departure time at departure area"));
			writeSheet.addCell(new Label(6, 0, "Departure track No"));
			writeSheet.addCell(new Label(7, 0, "number of blocks"));
			writeSheet.addCell(new Label(8, 0, "number of railcars"));
			writeSheet.addCell(new Label(9, 0, "destination of blocks(associated number of cars)"));
			
			
			for(int i=0 ; i<bl.size() ; i++){
				ArrayList<ArrayList<String>> tmpBlockName = countBlocks(bl.get(i));
				String destinationOfBlocks = new String();
				for(ArrayList<String> tb : tmpBlockName){
					destinationOfBlocks += tb.get(0) + "(" + tb.size() + ");";
				}
				writeSheet.addCell(new Label(0, i+1, "o" + (bl.get(i).get(0).outboundTrainNo+1)));
				writeSheet.addCell(new Label(1, i+1, Integer.toString(bl.get(i).get(0).departureDay)));
				writeSheet.addCell(new Label(2, i+1, Integer.toString(bl.get(i).get(0).pullBackEngineNo)));
				writeSheet.addCell(new Label(3, i+1, changeToFormatTime(bl.get(i).get(0).timeStartPullBack)));
				writeSheet.addCell(new Label(4, i+1, changeToFormatTime(bl.get(i).get(0).timeEndPullBack)));
				writeSheet.addCell(new Label(5, i+1, changeToFormatTime(bl.get(i).get(0).timeDepartureAtDepartureArea)));
				writeSheet.addCell(new Label(6, i+1, "D" + Integer.toString(bl.get(i).get(0).departureTrackNo)));
				writeSheet.addCell(new Label(7, i+1, Integer.toString(tmpBlockName.size())));
				writeSheet.addCell(new Label(8, i+1, Integer.toString(bl.get(i).size())));
				writeSheet.addCell(new Label(9, i+1, destinationOfBlocks));
				
				destinationOfBlocks = null;
				tmpBlockName = null;
			}		
			
			writeWorkBook.write();
			writeWorkBook.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		}
	}

	/* 
	 * outbound train information
	 */
	public static void outPutRailcarItinerary(ArrayList<ArrayList<Block>> bl){
		try{
			jxl.Workbook readWorkBook = jxl.Workbook.getWorkbook(new File("Output_File.xls"));   //唯讀的Excel工作薄物件
			jxl.write.WritableWorkbook writeWorkBook = Workbook.createWorkbook(new File("Output_File.xls"), readWorkBook);   //寫入
			jxl.write.WritableSheet writeSheet = writeWorkBook.createSheet("railcar_itinerary", 3);   //建立一個工作表名字，然後0表示為第一頁		
			
			writeSheet.addCell(new Label(0, 0, "Car No"));
			writeSheet.addCell(new Label(1, 0, "Destination of block"));
			writeSheet.addCell(new Label(2, 0, "Inbound Train No"));
			writeSheet.addCell(new Label(3, 0, "ArrivalDay No"));
			writeSheet.addCell(new Label(4, 0, "arriving time at receiving area"));
			writeSheet.addCell(new Label(5, 0, "Receiving track No"));
			writeSheet.addCell(new Label(6, 0, "Starting time of humping job"));
			writeSheet.addCell(new Label(7, 0, "Ending time of humping job"));
			writeSheet.addCell(new Label(8, 0, "Departure Day No"));
			writeSheet.addCell(new Label(9, 0, "Pull back engine No"));
			writeSheet.addCell(new Label(10, 0, "Starting time of pull back job"));
			writeSheet.addCell(new Label(11, 0, "Ending time of pull back job"));
			writeSheet.addCell(new Label(12, 0, "Departure time at departure area"));
			writeSheet.addCell(new Label(13, 0, "outbound Train No"));
			writeSheet.addCell(new Label(14, 0, "Departure track No"));
			
			int count=1;
			for(int i=0 ; i<bl.size() ; i++){
				for(int j=0 ; j<bl.get(i).size() ; j++, count++){
					writeSheet.addCell(new Label(0, count, Integer.toString(bl.get(i).get(j).carNo)));
					writeSheet.addCell(new Label(1, count, bl.get(i).get(j).blockName));
					writeSheet.addCell(new Label(2, count, "i" + bl.get(i).get(j).trainId));
					writeSheet.addCell(new Label(3, count, Integer.toString(bl.get(i).get(j).arrivalDay)));
					writeSheet.addCell(new Label(4, count, changeToFormatTime(bl.get(i).get(j).timeAtReceivingArea)));
					writeSheet.addCell(new Label(5, count, "R" + bl.get(i).get(j).receivingTrackNo));
					writeSheet.addCell(new Label(6, count, changeToFormatTime(bl.get(i).get(j).timeStartHump)));
					writeSheet.addCell(new Label(7, count, changeToFormatTime(bl.get(i).get(j).timeEndHump)));
					writeSheet.addCell(new Label(8, count, Integer.toString(bl.get(i).get(j).departureDay)));
					writeSheet.addCell(new Label(9, count, Integer.toString(bl.get(i).get(j).pullBackEngineNo)));
					writeSheet.addCell(new Label(10, count, changeToFormatTime(bl.get(i).get(j).timeStartPullBack)));
					writeSheet.addCell(new Label(11, count, changeToFormatTime(bl.get(i).get(j).timeEndPullBack)));
					writeSheet.addCell(new Label(12, count, changeToFormatTime(bl.get(i).get(j).timeDepartureAtDepartureArea)));
					writeSheet.addCell(new Label(13, count, "o" + bl.get(i).get(j).outboundTrainNo));
					writeSheet.addCell(new Label(14, count, "D" + bl.get(i).get(j).departureTrackNo));
				}
			}		
			
			writeWorkBook.write();
			writeWorkBook.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 計算同台火車有多少個[blockName]的細節
	 */
	private static ArrayList<ArrayList<String>> countBlocks(ArrayList<Block> bl){
		ArrayList<ArrayList<String>> tmpBlockName = new ArrayList<>();
		boolean ifFind = false;
		for(Block b : bl){
			ifFind = false;
			ArrayList<String>tmp = new ArrayList<String>();
			for(ArrayList<String> t : tmpBlockName){
				if(t.contains(b.blockName)==true){
					t.add(b.blockName);
					ifFind = true;
					break;
				}
			}
			if(ifFind == false){
				tmp.add(b.blockName);
				tmpBlockName.add(tmp);
			}
			tmp = null;
		}
		
		return tmpBlockName;
	}
	
	/**
	 * //將日期與到達時間整合後，統一轉成double型態
	 */
	public static double arrivalTimePlusDay(int day, double arrivalTime){
		return arrivalTime + (day-1)*24f;
	}
	
	/*
	 * 換算出標準格式的時間
	 */
	public static String changeToFormatTime(double t){
		int day = ((int)t-1)/24+1;
		int hour = (int)(t-1)%24;
		double minute = (t-1.0f*(int)t)*60;
		
		return "Day" + Integer.toString(day) + "," + Integer.toString(hour) + ":" + Integer.toString((int)minute);
	}
}
