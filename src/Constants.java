/**
 * Constants 介面定義了一些常數的數值
 * @author Ping
 *
 */
public interface Constants {
	/**
	 * case 1
	 */
	int RECEIVING_TRACKS_NUMBER = 4;
	int RECEIVING_TRACKS_CAPACITY = 40;
	int CLASSIFICATION_TRACKS_NUMBER = 6;
	int CLASSIFICATION_TRACKS_CAPACITY = 50;
	int DEPARTURE_TRACKS_NUMBER = 4;
	int DEPARTURE_TRACKS_CAPACITY = 40;
	double HUMP_RATE = (1*1.0f/2.5)/60;   // 每拉一節車要多久，單位:小時
	double HUMP_INTERVAL = 8*1.0f/60; //單位:小時
	double TECHNICAL_INSPECTION_TIME = 30*1.0f/60;   //單位:小時
	double COMBINING_BLOCKS_PER_TRACK = 3*1.0f/60;   //一個track的每有一block結合所需時間,單位:小時
	/*
	 * case 5
	 */
/*	int RECEIVING_TRACKS_NUMBER = 10;
	int RECEIVING_TRACKS_CAPACITY = 185;
	int CLASSIFICATION_TRACKS_NUMBER = 42;
	int CLASSIFICATION_TRACKS_CAPACITY = 60;
	int DEPARTURE_TRACKS_NUMBER = 7;
	int DEPARTURE_TRACKS_CAPACITY = 207;
	double HUMP_RATE = 1*1.0f/60;   // 每拉一台車，單位:小時
	double HUMP_INTERVAL = 2*1.0f/60; //單位:小時
	double TECHNICAL_INSPECTION_TIME = 45*1.0f/60;   //單位:小時
*/}
