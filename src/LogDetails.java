import java.io.Serializable;

/**
 * @author 
 *
 *This is a POJO class used to store the details of each record in the file
 */
public class LogDetails implements Serializable{

	
	private static final long serialVersionUID = 2256754952311962944L;
	private String time;
	private String request;
	private String status;
	private int responseTime;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getResponseTime() {
		return responseTime;
	}
	public void setResponseTime(int responseTime) {
		this.responseTime = responseTime;
	}
	@Override
	public String toString() {
		return "LogDetails [time=" + time + ", request=" + request + ", status=" + status + ", responseTime="
				+ responseTime + "]";
	}
	
}
