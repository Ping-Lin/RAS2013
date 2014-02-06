import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import jxl.Sheet;
import jxl.Workbook;
import jxl.*;
import jxl.read.biff.BiffException;
import java.util.ArrayList;

public class Test1 {

	public static void main(String[] args) {
		ArrayList<Block> blockList = new ArrayList<Block>();
		readExcelFile(blockList);
		
		for(Block a : blockList){
			System.out.println(a.arrivalDay + " " + a.trainId + " " + a.timeAtReceivingArea + " " + a.blockName + " " + a.carNo);
		}
	}

	/*
	 * readExcelFile 為獨取Excel檔的function, excel檔格式副檔名須為xls
	 */
	private static void readExcelFile(ArrayList<Block> blockList){
		
		try {
			InputStream excelFile = new FileInputStream("src/data_readme/Input_data_set_1.xls");   //要讀的檔案
			jxl.Workbook readWorkBook = Workbook.getWorkbook(excelFile);   //將其讀入workbook中
			Sheet readSheet = readWorkBook.getSheet("Inbound Train Info");   //讀sheet

			for(int i=1 ; i<readSheet.getRows() ; i++){
				Block tmp = new Block();   //temp for block to save into
				
				tmp.arrivalDay = Integer.parseInt(readSheet.getCell(0,i).getContents());
				tmp.trainId = Integer.parseInt(readSheet.getCell(1,i).getContents());
				NumberCell nc = (NumberCell)readSheet.getCell(2,i);
				tmp.timeAtReceivingArea = nc.getValue();
				tmp.blockName = readSheet.getCell(3,i).getContents();
				tmp.carNo = Integer.parseInt(readSheet.getCell(4,i).getContents());
				
				blockList.add(tmp);
				tmp = null;
			}
			
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
}
