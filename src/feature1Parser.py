#List in python
import logging
import time
import codecs
from collections import Counter 

LOG_FILE_NAME = 'log_output/processLOG.log'
INPUT_FILE_NAME= 'log_input/log.txt'
OUTPUT_FILE_NAME='log_output/hosts.txt'
logging.basicConfig(filename=LOG_FILE_NAME,level=logging.DEBUG)

"""This method is used to form a list of list from the user input file.
The position in the list corresponding to the column text of the file.
"""
def formListOfList():
	collist=[]
	f = open(INPUT_FILE_NAME, 'r',  encoding='ascii', errors='replace')
	content = f.readlines()
	i=0;
	for c in content:
		strarr=c.split();
		j=0;
		i+=1;
		for str in strarr:
			if(i==1):
				collist.append([str])
			else:
				if(j>=len(collist)):
					collist.append([str])
				else:
					collist[j].append(str);
			j+=1;
	return collist	

"""This method is used get field number from the users"""
def getFieldNumberFromUser():
	#fieldnum=int(input("Enter the field number you want to retrieve from the space or tab seperated file "))
	fieldnum=1
	#top=int(input("Retrieve how many records from the top "))
	top=10
	return fieldnum,top

def writeOutputToFile(outputList,top):
	fin = open(OUTPUT_FILE_NAME, 'w')
	i=0
	for key,value in outputList.most_common():
		if(i<top):
			fin.write(str(key))
			fin.write(',')
			fin.write(str(value))
			fin.write('\n')
			i += 1
		else:
			break	
	fin.close()

def sortBasedOnFrequency(collist,fieldnum):
	outputList=None
	if(fieldnum>0 and fieldnum<=len(collist)):
		outputList=collist[fieldnum-1]
		countList = Counter(outputList)
		#print(countList)
		logging.info("[SUCCESS] Retrieved the records for the fields specified and then sorted them")
	else:
		print("Invalid field number")
		logging.error("[ERROR] Invalid field Number")
	return countList	

def sortList(collist,fieldnum):
	outputList=None
	if(fieldnum>0 and fieldnum<=len(collist)):
		outputList=collist[fieldnum-1]
		outputList.sort()
		logging.info("[SUCCESS] Retrieved the records for the fields specified and then sorted them")
	else:
		print("Invalid field number")
		logging.error("[ERROR] Invalid field Number")
	return outputList

logging.info("[Start] Start date & time " + time.strftime("%c"));
#forming list of list from the input file
collist=formListOfList()

#getting the input from user
fieldnum,top=getFieldNumberFromUser()

#form sorted list
countList=sortBasedOnFrequency(collist,fieldnum)

writeOutputToFile(countList,top)

#print(outputList)

logging.info("[END] End date & time " + time.strftime("%c"));

