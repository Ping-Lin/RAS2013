import java.util.ArrayList;

/**
 * 此Class為ReceivingArea的軌道
 * 
 * @author Ping
 */
public class Track {
	boolean ifEmpty;   //判斷軌道是否為空
	ArrayList<Block> train;   //track上面的火車
	boolean ifPullBackPull;   //只有combination會用到
	double humpTime;
	ArrayList<BlockToTrack> btt;
	
	public Track(){
		ifEmpty = true;
		train = new ArrayList<Block>();
		ifPullBackPull = false;
		humpTime=0.0f;
		btt = new ArrayList<BlockToTrack>();
	}
}