import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;


public class Test {

	public static void main(String[] args) {
		 System.out.print("hi");

	}
	
	/*
	 * readExcelFile 為獨取Excel檔的function, excel檔格式副檔名須為xls
	 */
	private void readExcelFile(){
		try {
			InputStream excelFile = new FileInputStream("src/data_readme/Input_data_set_1.xls");   //要讀的檔案
			jxl.Workbook readWorkBook = Workbook.getWorkbook(excelFile);   //將其讀入workbook中
			Sheet readSheet = readWorkBook.getSheet("Inbound Train Info");   //讀sheet
			
			
			
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
