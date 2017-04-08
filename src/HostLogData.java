import java.io.Serializable;
import java.util.List;

/**
 * This is POJO class used to maitain data from the input file
 * @author 
 *
 */
public class HostLogData implements Serializable{

	private static final long serialVersionUID = -6423070240800122517L;
	protected int successRequest;
	protected int failureRequest;
	protected long totalResponseTimes;
	protected long totalNumberOfRequest;
	protected List<LogDetails> logDetails;
	
	public List<LogDetails> getLogDetails() {
		return logDetails;
	}
	public void setLogDetails(List<LogDetails> logDetails) {
		this.logDetails = logDetails;
	}
	
	public int getSuccessRequest() {
		return successRequest;
	}
	public void setSuccessRequest(int successRequest) {
		this.successRequest = successRequest;
	}
	public int getFailureRequest() {
		return failureRequest;
	}
	public void setFailureRequest(int failureRequest) {
		this.failureRequest = failureRequest;
	}
	public long getTotalResponseTimes() {
		return totalResponseTimes;
	}
	public void setTotalResponseTimes(long totalResponseTimes) {
		this.totalResponseTimes = totalResponseTimes;
	}
	public long getTotalNumberOfRequest() {
		return totalNumberOfRequest;
	}
	public void setTotalNumberOfRequest(long totalNumberOfRequest) {
		this.totalNumberOfRequest = totalNumberOfRequest;
	}
	@Override
	public String toString() {
		return "HostLogData [successRequest=" + successRequest + ", failureRequest=" + failureRequest
				+ ", totalResponseTimes=" + totalResponseTimes + ", totalNumberOfRequest=" + totalNumberOfRequest
				+ ", logDetails=" + logDetails + "]";
	}
}
