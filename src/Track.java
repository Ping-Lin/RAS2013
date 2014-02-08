import java.util.ArrayList;

/**
 * 此Class為ReceivingArea的軌道
 * 
 * @author Ping
 */
public class Track {
	boolean ifEmpty;   //判斷軌道是否為空
	ArrayList<Block> train;   //track上面的火車
	
	public Track(){
		ifEmpty = true;
		train = new ArrayList<Block>();
	}
}