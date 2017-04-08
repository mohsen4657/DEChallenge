import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;  

/**
 * This is the comparator class for HostLogData class & used to sort based the total number of requests field 
 * @author 
 *
 */
public class TotalRequestsComparator implements Comparator<Map.Entry<String, HostLogData>>{  
	public int compare(Entry<String, HostLogData> o1, Entry<String, HostLogData> o2){  
		HostLogData hostLogData1=o1.getValue();
		HostLogData hostLogData2=o2.getValue();
		if(hostLogData1.totalNumberOfRequest==hostLogData2.totalNumberOfRequest)  
			return 0;  
		else if(hostLogData1.totalNumberOfRequest>hostLogData2.totalNumberOfRequest)  
			return -1;  
		else  
			return 1;  
	}
}



