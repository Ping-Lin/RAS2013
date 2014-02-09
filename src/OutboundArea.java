
public class OutboundArea {
	Track[] outboundTrack;
	
	public OutboundArea(){
		outboundTrack = new Track[Constants.DEPARTURE_TRACKS_NUMBER];
		for(int i=0 ; i<outboundTrack.length ; i++)   //new 空間
			outboundTrack[i] = new Track();
	}
}
