import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is the used to compute the following and write it to the output file
 * 1. Compute the top requests for the input log file passed and write it to output file
 * 2. Compute the top requests for every hour and write it to a file 
 * 3. Compute the top resources which take longer response time and write it to the output file
 * 4. Compute the host or IP which have 3 failed login attempts in 20 seconds and block them for 5 minutes and write it to o/p file
 * 
 * Pass 2 command line arguments two run this main class 
 * 1. Input file path 2. no of top requests or resources to be written to the o/p file
 * 
 * @author 
 *
 */
public class LogParser {

	final static Logger logger = Logger.getLogger(LogParser.class.getName());
	
	private static final String OUTPUT_PATH = "./../log_output/";
	private static final String LOG_FILE_PATH="./../log_output/processLOG.log";
	private static final String STATUS_FILE_NAME="blocked.txt";
	private static final String HOURLY_REPORT_FILE_NAME="hours.txt";
	private static final String TOP_HOST_FILE_NAME="hosts.txt";
	private static final String TOP_RESOURCE_FILE_NAME="resource.txt";
	private static final String PATTERN = "([0-9._A-Za-z-]+).*\\[(.*)\\][ ]+\"(.*)\"[ ]+([0-9]+)[ ]+([0-9-]+)";
	
	private static final String DATE_PATTERN="dd/MMM/yyyy:HH:mm:ss";
	private static final String SEPARATER=",";

	/**
	 * The main method used to launch the log parser
	 * @param args
	 * @throws NumberFormatException
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws NumberFormatException, IOException, ParseException {
		
		// This block configure the logger with handler and formatter 
		FileHandler fh = new FileHandler(LOG_FILE_PATH);
		logger.addHandler(fh);
		SimpleFormatter formatter = new SimpleFormatter();  
		fh.setFormatter(formatter);  
		logger.info("Logger Name: "+logger.getName()+" STARTED");
		
		//setting the second command line argument for top 10 records
		int top=Integer.valueOf(args[1]);

		Map<String,HostLogData> logMap=new HashMap<String,HostLogData>();
		//opening the input file path 

		BufferedReader br = new BufferedReader(new FileReader(args[0]));

		String currentLine=null;
		String hostName=null;
		String time=null;
		String request=null;
		String status;
		int responseTime;

		Pattern pattern=Pattern.compile(PATTERN);

		LogDetails logDetails=null;
		HostLogData hostLogData=null;
		List<LogDetails> logDetailsList=null;
		long totalResponseTimes=0;
		long totalRequests=0;

		//map used to maintain the resources that consumed the maximum time
		Map<String, Integer> topResourceMap=new LinkedHashMap<>();
		String lastKey="";

		SimpleDateFormat dateFormat=new SimpleDateFormat(DATE_PATTERN);

		//map used to track the failed login attempts within 20 seconds
		Map<String, List<Calendar>> failedLoginMap=new LinkedHashMap<String,List<Calendar>>();
		List<Calendar> dateList=null;

		BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_PATH+STATUS_FILE_NAME));
		BufferedWriter bwHourly = new BufferedWriter(new FileWriter(OUTPUT_PATH+HOURLY_REPORT_FILE_NAME));

		boolean initialFlag=false;
		Map<String,Integer> hourMap=new LinkedHashMap<String,Integer>();

		Calendar endCal=null;
		Calendar curCal=null;
		Calendar initialCal=null;
		
		logger.info("Logger Name: "+logger.getName()+" STARTED READING FILE at "+args[0]);
		
		while ((currentLine = br.readLine()) != null) {

			Matcher m=pattern.matcher(currentLine);
			while(m.find()){
				hostName=m.group(1);
				time=m.group(2);
				request=m.group(3);
				status=m.group(4);

				curCal=Calendar.getInstance();
				curCal.setTime(dateFormat.parse(time));
				//Finding out the initial time from the file
				if(!initialFlag){
					initialCal=Calendar.getInstance();
					initialCal.setTime(dateFormat.parse(time));
					initialCal.set(Calendar.MINUTE, 0);
					initialCal.set(Calendar.SECOND, 0);
					endCal=Calendar.getInstance();
					endCal.setTime(dateFormat.parse(time));
					endCal.set(Calendar.MINUTE, 0);
					endCal.set(Calendar.SECOND, 0);
					endCal.add(Calendar.HOUR, 1);
					initialFlag=true;
				}
				//computing the number of hosts for each hours and then storing it in a map
				if((initialCal.compareTo(curCal)<=0) && (endCal.compareTo(curCal)>0)){
					if(hourMap.containsKey(hostName)){
						int val=hourMap.get(hostName);
						val+=1;
						hourMap.put(hostName, val);
					}else{
						hourMap.put(hostName, 1);
					}
				}else{
					//writing the top hosts for previous hour and writing it file
					hourMap=sortByValue(hourMap);
					int count=0;
					for(Map.Entry<String, Integer> entry:hourMap.entrySet()){
						if(count<top){
							logger.info("Logger Name: "+logger.getName()+"Writing For hourly report at "+OUTPUT_PATH+HOURLY_REPORT_FILE_NAME);
							bwHourly.write(dateFormat.format(initialCal.getTime()));
							bwHourly.write(SEPARATER);
							bwHourly.write(dateFormat.format(endCal.getTime()));
							bwHourly.write(SEPARATER);
							bwHourly.write(entry.getKey());
							bwHourly.write(SEPARATER);
							//System.out.println(String.valueOf(entry.getValue()));
							bwHourly.write(String.valueOf(entry.getValue()));
							bwHourly.newLine();
							count++;
						}
					}
					initialCal.add(Calendar.HOUR, 1);
					endCal.add(Calendar.HOUR, 1);
					hourMap=new HashMap<String,Integer>();
					hourMap.put(hostName, 1);
				}

				//code to compute the 3 failed request within 20 seconds
				if(status.startsWith("401")){				
					Calendar cal=Calendar.getInstance();
					cal.setTime(dateFormat.parse(time));
					if(failedLoginMap.containsKey(hostName)){
						dateList=failedLoginMap.get(hostName);
						dateList.add(cal);
					}else{
						dateList=new ArrayList<Calendar>();
						dateList.add(cal);
						failedLoginMap.put(hostName,dateList);
					}

				}
				//adding resources which took maximum time to respond in topResourceMap
				if(m.group(5).equals("-")){
					responseTime=0;
				}else{
					responseTime=Integer.valueOf(m.group(5));
				}
				String requestArr[] = request.split("\\s+");

				if(requestArr.length>=3){
					if(topResourceMap.size()<top){
						if(topResourceMap.containsKey(requestArr[1])){
							int value=topResourceMap.get(requestArr[1]);
							if(value<responseTime){
								topResourceMap.put(requestArr[1], responseTime);
							}
						}else{
							topResourceMap.put(requestArr[1], responseTime);
						}

						if(topResourceMap.size()==top){
							topResourceMap=sortByValue(topResourceMap);
							for(Map.Entry<String, Integer> entry:topResourceMap.entrySet()){
								lastKey=entry.getKey();
							}
							//lastKey=requestArr[1];
						}
					}else if(topResourceMap.size()>=top){
						//System.out.println("Last key"+lastKey);
						if(topResourceMap.containsKey(requestArr[1])){
							int value=topResourceMap.get(requestArr[1]);
							if(value<responseTime){
								topResourceMap.put(requestArr[1], responseTime);
							}
						}else if(topResourceMap.get(lastKey)<responseTime){
							topResourceMap.remove(lastKey);
							topResourceMap.put(requestArr[1], responseTime);
							topResourceMap=sortByValue(topResourceMap);
							for(Map.Entry<String, Integer> entry:topResourceMap.entrySet()){
								lastKey=entry.getKey();
							}
						}
					}
				}
				
				
				topResourceMap=sortByValue(topResourceMap);

				//finding the top hosts for the entire log file
				if(logMap.containsKey(hostName)){
					hostLogData=logMap.get(hostName);
					logDetailsList=hostLogData.getLogDetails();
					logDetails=new LogDetails();
					logDetails.setRequest(request);
					logDetails.setResponseTime(responseTime);
					logDetails.setStatus(status);
					logDetails.setTime(time);
					logDetailsList.add(logDetails);
					hostLogData.setLogDetails(logDetailsList);

					//total response time
					totalResponseTimes=hostLogData.getTotalResponseTimes();
					totalResponseTimes+=responseTime;
					hostLogData.setTotalResponseTimes(totalResponseTimes);

					//total requests
					totalRequests=hostLogData.getTotalNumberOfRequest()+1;
					hostLogData.setTotalNumberOfRequest(totalRequests);

					//total success & failure response
					if(status.startsWith("4") || status.startsWith("5")){
						hostLogData.setFailureRequest(hostLogData.getFailureRequest()+1);
					}else{
						hostLogData.setSuccessRequest(hostLogData.getSuccessRequest()+1);
					}

					logMap.put(hostName, hostLogData);
				}else{
					hostLogData=new HostLogData();
					logDetailsList=new ArrayList<LogDetails>();
					logDetails=new LogDetails();
					logDetails.setRequest(request);
					logDetails.setResponseTime(responseTime);
					logDetails.setStatus(status);
					logDetails.setTime(time);


					logDetailsList.add(logDetails);

					//initial values
					hostLogData.setTotalNumberOfRequest(1);
					//intial response time 
					hostLogData.setTotalResponseTimes(responseTime);

					//counting success and failure response
					if(status.startsWith("4") || status.startsWith("5")){
						hostLogData.setFailureRequest(1);
						hostLogData.setSuccessRequest(0);
					}else{
						hostLogData.setFailureRequest(0);
						hostLogData.setSuccessRequest(1);
					}

					hostLogData.setLogDetails(logDetailsList);
					logMap.put(hostName, hostLogData);
				}
			}
		}
		//code block to compute whether three failed login attempts 
		//where there in 3 seconds and then blocking for 5 minutes
		//and writing to a file
		Calendar endTime;
		for(Map.Entry<String, List<Calendar>> entry:failedLoginMap.entrySet()){
			hostName=entry.getKey();
			List<Calendar> hostDateList=entry.getValue();
			Calendar blockedTime=null;
			for(int i=0;i<hostDateList.size()-2;i++){
				endTime=hostDateList.get(i);
				endTime.add(Calendar.SECOND, 20);
				if((endTime.compareTo(hostDateList.get(i+1))>=0) && (endTime.compareTo(hostDateList.get(i+2))>=0)){
					if(blockedTime==null || (blockedTime.compareTo(hostDateList.get(i))<0)){
						logger.info("Logger Name: "+logger.getName()+"Writing blocked IP or Host "+OUTPUT_PATH+STATUS_FILE_NAME);
						bw.write(hostName);
						bw.write(SEPARATER);
						bw.write(dateFormat.format(hostDateList.get(i+2).getTime()));
						bw.write(SEPARATER);
						hostDateList.get(i+2).add(Calendar.MINUTE, 5);
						bw.write(dateFormat.format(hostDateList.get(i+2).getTime()));
						blockedTime=hostDateList.get(i+2);
						i+=2;
						bw.newLine();
					}
				}
			}
		}
		bw.close();
		logger.info("Logger Name: "+logger.getName()+"Completed blocked IP or Host "+OUTPUT_PATH+STATUS_FILE_NAME);
		if(!hourMap.isEmpty()){
			hourMap=sortByValue(hourMap);
			int count=0;
			for(Map.Entry<String, Integer> entry:hourMap.entrySet()){
				if(count<top){
					logger.info("Logger Name: "+logger.getName()+"Started final write for hourly report at "+OUTPUT_PATH+HOURLY_REPORT_FILE_NAME);
					bwHourly.write(dateFormat.format(initialCal.getTime()));
					bwHourly.write(SEPARATER);
					bwHourly.write(dateFormat.format(endCal.getTime()));
					bwHourly.write(SEPARATER);
					bwHourly.write(entry.getKey());
					bwHourly.write(SEPARATER);
					bwHourly.write(String.valueOf(entry.getValue()));
					bwHourly.newLine();
					count++;
				}
			}
		}
		bwHourly.close();
		logger.info("Logger Name: "+logger.getName()+"Completed final for hourly report at "+OUTPUT_PATH+HOURLY_REPORT_FILE_NAME);
		br.close();
		//writing top requests to a file
		Map<String,HostLogData> sortedByRequestMap=sortByTotalRequests(logMap);
		
		logger.info("Logger Name: "+logger.getName()+"Started writing top resources the file at "+OUTPUT_PATH+TOP_RESOURCE_FILE_NAME);
		
		writeToFile(sortedByRequestMap,TOP_HOST_FILE_NAME,top);

		logger.info("Logger Name: "+logger.getName()+"Completed writing top resources the file at "+OUTPUT_PATH+TOP_RESOURCE_FILE_NAME);
		
		logger.info("Logger Name: "+logger.getName()+"Started writing top resources the file at "+OUTPUT_PATH+TOP_RESOURCE_FILE_NAME);
		
		//writing top resources to a file
		writeToFile(topResourceMap,TOP_RESOURCE_FILE_NAME);
		
		logger.info("Logger Name: "+logger.getName()+"Completed writing top resources the file at "+OUTPUT_PATH+TOP_RESOURCE_FILE_NAME);
		
		logger.info("Logger Name: "+logger.getName()+"Completed Log Parser Job");
	}

	/**
	 * This method is used to sort total records by the requests
	 * @param logMap
	 * @return
	 */
	private static Map<String, HostLogData> sortByTotalRequests(Map<String, HostLogData> logMap) {
		List<Map.Entry<String,HostLogData>> entries = new ArrayList<Map.Entry<String,HostLogData>>(logMap.entrySet());
		Collections.sort(entries, new TotalRequestsComparator());

		Map<String, HostLogData> sortedMap = new LinkedHashMap<String, HostLogData>();
		for (Map.Entry<String, HostLogData> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	/**
	 * This method is used to write the top requests to a file
	 * @param sortedByRequestMap
	 * @param fileName
	 * @param top
	 * @throws IOException
	 */
	public static void writeToFile(Map<String, HostLogData> sortedByRequestMap,String fileName,int top) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_PATH+fileName));
		//Collections.sort(logMap,new TotalRequestsComparator());
		int i=0;
		for (Map.Entry<String, HostLogData> entry : sortedByRequestMap.entrySet()) {
			if(i>=top){
				break;
			}
			i++;
			String key = entry.getKey();
			HostLogData value = entry.getValue();

			bw.write(key);
			bw.write(SEPARATER);
			bw.write(String.valueOf(value.getTotalNumberOfRequest()));
			bw.newLine();
		}
		bw.close();
	}

	public static void writeToFile(Map<String, Integer> topResourceMap,String fileName) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_PATH+fileName));
		String[] resourceName;
		for (Map.Entry<String, Integer> entry : topResourceMap.entrySet()) {

			String key = entry.getKey();
			String value = String.valueOf(entry.getValue());

			resourceName = key.split(" ");
			//resourceName = resourceName[1].split("/");

			//bw.write(resourceName[1]);
			bw.write(key);
			bw.write(SEPARATER);
			bw.write(value);
			bw.newLine();
		}
		bw.close();
	}


	/**
	 * This method is used to sort by the value element in the hash map
	 * @param unsortMap
	 * @return
	 */
	private static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap)
	{

		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<String, Integer>>()
		{
			public int compare(Entry<String, Integer> o1,
					Entry<String, Integer> o2)
			{               
				return o2.getValue().compareTo(o1.getValue());

			}
		});

		// Maintaining insertion order with the help of LinkedList
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Entry<String, Integer> entry : list)
		{
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

}
