import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;

class Station{//地铁站点
	String station_name;
	Set<Integer> station_id;//中转站有不止一个站点编号
	String line_name;
	int line_id;
	Station(){
		station_name="";
		station_id=new TreeSet<>();
		line_name="";
		line_id=0;
	}
}
class Path{//路径
	int distance;//距离
	Station path_laststation;//路径最后一站
	Vector<Station>path_stationlist;//站点集合
	Path(){
		distance=0;
		path_laststation=new Station();
		path_stationlist=new Vector<>();
		
	}
}
class Map{//解析输入的txt以及最短距离的计算
	private Vector<String> subway_allstation =new Vector<>();
	private int maxdis=9999;
	private static HashMap<String,Station> name_to_station =new HashMap();
	private static HashMap<Integer,Station> id_to_station =new HashMap();
	private static HashMap<String,List<Station>> line_to_station =new HashMap();
	private static HashMap<Integer,String> lineid_to_linename =new HashMap();

	private static HashMap<Integer,Set<Station>> Lineid_to_transferstation =new HashMap();
	private static HashMap<String,Integer> transferstationname_to_distance =new HashMap();

	public void loadSubwayFile(String subway_file) {
		BufferedReader reader=null;
		File subway=new File(subway_file);
	try {
		reader=new BufferedReader(new FileReader(subway));
		String t=null;
		while((t=reader.readLine())!=null) {
			subway_allstation.add(t);
		}
	
	}catch (IOException e) {
        e.printStackTrace();
    } finally {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
	parseSubway();
//	for (String strSubwayLine: subway_allstation) {
//        System.out.println(strSubwayLine);
//    }
	
}
	//处理地铁线路内容，存入Map中
public void parseSubway(){
	for(int i=0;i<subway_allstation.size();i++) {
		String line=subway_allstation.get(i);
	
		//parseLine(line);
	//	public void parseLine(String line) {
			String[] arrIdAndStations=line.split("，");
			
			if(arrIdAndStations.length!=2) {
				System.out.println("地铁数据错误" + line);
		        return;
			}
			int lineid=i+1;
			
		//	System.out.print(lineid+" ");
			
			if(lineid==-1) {
				System.out.println("地铁线路号数据错误"+lineid);
			}
			
			String[]arrLineAndStations =arrIdAndStations[1].split("：");
			lineid_to_linename.put(lineid, arrLineAndStations[0]);
			String[]arrStationNames=arrLineAndStations[1].split(" ");
			for(int i1=0;i1<arrStationNames.length;i1++) {
				String Stationname=arrStationNames[i1];
				Station station=new Station();
				station.station_name=Stationname;
				int stationid=lineid*1000+i1+1;
				station.station_id.add(stationid);
				station.line_name=arrLineAndStations[0];
				station.line_id=lineid;
				
			//	line_to_station.put(station.line_name, station);
				if(!line_to_station.containsKey(station.line_name)) {
					List <Station> stations=new ArrayList<Station>();
					stations.add(station);
					line_to_station.put(station.line_name, stations);
				}
					
				else {
					List <Station> stations=line_to_station.get(station.line_name);
					stations.add(station);
					//System.out.println(line_to_station.size());
				}
				//存入map
				id_to_station.put(stationid, station);
				//注意中转站 要先判断再存
				if(!name_to_station.containsKey(arrStationNames[i1])) {
					name_to_station.put(Stationname, station);
				}
				else {
					Station stationExistedTransferStation =name_to_station.get(arrStationNames[i1]);
					stationExistedTransferStation.station_id.add(stationid);
					
					
					
					updateTransferStation(stationExistedTransferStation);
					
				}
		
			
		//}
	}
}

}

//更新中转站点
public void updateTransferStation(Station transferStation) {
	
	transferstationname_to_distance.put(transferStation.station_name, maxdis);
	for(int stationid:transferStation.station_id) {
		int line=transferStation.line_id;
		if(!Lineid_to_transferstation.containsKey(line)) {
			Set<Station> setstations=new HashSet<>();
			setstations.add(transferStation);
			
			Lineid_to_transferstation.put(line, setstations);
		}
		else {
			Set<Station> setstations=Lineid_to_transferstation.get(line);
			setstations.add(transferStation);
		}
	}
//	Set<Station> text=Lineid_to_transferstation.get(1);
//	for(Station k:text) {
//	System.out.println(k.station_name);
//	}
}



//根据地铁线路号得知地铁站点
void printStationsOfLine(String Line, String strOutFile) {
    StringBuffer strRst = new StringBuffer();
    strRst.append(Line+"\r\n");
        if (line_to_station.containsKey(Line)) {
        	//根据路线得到map的value
        	List<Station> stations=new ArrayList<Station>();
        	stations=line_to_station.get(Line);
        	//System.out.println(stations.size());
        	for(int i=0;i<stations.size();i++) {
        		Station s=stations.get(i);
        		strRst.append(s.station_name+ "\r\n");
        	//	System.out.println(s.station_name);
        	}
        }
        else {
        	strRst.append("地铁线不存在");
        }
     printFile(strRst.toString(), strOutFile);
}
//输出文件 文件中要写入的内容   文件名
void printFile(String strContent, String strOutFile) {
    try {
        File file = new File(strOutFile);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(strOutFile, false);

        fileWriter.write(strContent.toString());
        fileWriter.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

//最短路径

Path shortedPath(String startname,String endname) {
	Path shorted=new Path();
	shorted.distance=maxdis;
	if(!name_to_station.containsKey(startname)||!name_to_station.containsKey(endname)) {
		shorted.distance=-100;
		return shorted;
	}
		
	else {
	Station startstation=name_to_station.get(startname);
	Station endstation=name_to_station.get(endname);
	
	transferstationname_to_distance.put(endname, maxdis);
	
	Path pathstart=new Path();
	pathstart.distance=0;
	pathstart.path_laststation=startstation;
	pathstart.path_stationlist.addElement(startstation);
	
	Stack<Path> stackAllPaths=new Stack<>();
	stackAllPaths.push(pathstart);
	//如果两个站点之间需要中转，则遍历与起始站点在同一条线路的所有中转站，找到一条距离短且可以到达的路径
	while(!stackAllPaths.empty()) {
		Path pathcurrent=stackAllPaths.pop();
		
		  if (pathcurrent.distance> shorted.distance) {
              continue;
          }
		
		int enddistance=getStationDistance(pathcurrent.path_laststation,endstation);
		if(enddistance==0) {
			if(pathcurrent.distance<shorted.distance)
				shorted=pathcurrent;
			continue;//退出循环
		}
		for(String stationname:transferstationname_to_distance.keySet()) {
			Station stationtransfer=name_to_station.get(stationname);
			int currenttotransferdis =getStationDistance(pathcurrent.path_laststation,stationtransfer);
			int finaldis=pathcurrent.distance+currenttotransferdis;
			if(finaldis>=transferstationname_to_distance.get(stationname))
				continue;
			transferstationname_to_distance.put(stationname, finaldis);
			if(finaldis<1000&&finaldis<shorted.distance) {
				Path pathnew=new Path();
				pathnew.distance=finaldis;
				pathnew.path_laststation=stationtransfer;
				pathnew.path_stationlist=new Vector<>(pathcurrent.path_stationlist);
				pathnew.path_stationlist.addElement(stationtransfer);
				stackAllPaths.push(pathnew);
			}
		}
	}
		
	return shorted;
	}
}
//得到Station1 与 Station2 相同的地铁线路编号
int getLineNumber(Station S1, Station S2) {
    for (int nS1Id: S1.station_id) {
        int nS1LineNum = (nS1Id-1)/1000;

        for (int nS2Id: S2.station_id) {
            int nS2LineNumber = (nS2Id-1)/1000;

            if (nS1LineNum == nS2LineNumber) {
                return nS1LineNum;
            }
        }
    }
    return -1;
}
//打印路径
String printPath(Path path) {
	 StringBuffer strRst = new StringBuffer();
	 if(path.path_stationlist.size()==0) {
		 return "查询站点输入有误";
	 }
		 if(path.path_stationlist.size()==1)
		 return path.path_laststation.station_name;
	 
	 Vector<Station> listStations = path.path_stationlist;
	 for(int i=1;i<listStations.size();i++) {
		 Station stationstart=listStations.get(i-1);
		 Station stationend=listStations.get(i);
		 int linenumber=getLineNumber(stationstart,stationend);
		 String linename=lineid_to_linename.get(linenumber);
		 strRst.append(linename+"\r\n");
		 
		 if(i==1)
			 strRst.append(stationstart.station_name+"\r\n");
	 
	 for(String stationname: listStationsInArea(stationstart, stationend)) {
		 strRst.append(stationname+"\r\n");
	//	 System.out.print(stationname);
	 }
	 }
	return strRst.toString();
}
//得到两个Station之间的所有站点名称
Vector<String> listStationsInArea(Station stationStart, Station stationEnd) {
    Vector<String> listStations = new Vector<String>();
    int nLineNumber = getLineNumber(stationStart, stationEnd);

    int nStartId = 0;
    int nEndId = 0;

    for (int nId: stationStart.station_id) {
        if (Math.abs(nId-(nLineNumber*1000))<1000) {
            nStartId = nId;
        }
    }

    for (int nId: stationEnd.station_id) {
        if (Math.abs(nId-(nLineNumber*1000))<1000) {
            nEndId = nId;
        }
    }

    if (nStartId == nEndId) {
        return listStations;
    }

    int nStep = 1;

    if (nEndId < nStartId) {
        nStep = -1;
    }

    int nIndexId = nStartId + nStep;
    while (nIndexId != nEndId) {
        String strSName = id_to_station.get(nIndexId).station_name;
        listStations.addElement(strSName);
        nIndexId += nStep;
    }

    String strName = id_to_station.get(nEndId).station_name;
    listStations.addElement(strName);

    return listStations;
}


int getStationDistance(Station s1,Station s2) {
	int mindis=maxdis;
	Set<Integer> s1id=s1.station_id;
	Set<Integer> s2id=s2.station_id;
	for(int i:s1id) {
		for(int j:s2id) {
			int dis=Math.abs(i-j);
			if(dis<mindis)
				mindis=dis;
		}
	}
	
	return mindis;
}

}

public class Subway {
	 public static void main(String[] args) {
		 
		 String FileName=null;
		 String startStationName=null;
		 String endStationName=null;
		 String OutFileName=null;
		 String LineName=null;
		 for(int i=0;i<args.length;i++) {
			 String StrArgs=args[i];
			 if(StrArgs.equals("-map")) {
				 i=i+1;
				 if(i<args.length) {
					 FileName=args[i];
				 }else {
					 System.out.println("未输入地铁信息文件");
				 }
				
			 }
			 else if(StrArgs.equals("-a")) {
				 i=i+1;
				 LineName=args[i];
			 }
			 else if(StrArgs.equals("-o")) {
				 i=i+1;
				 OutFileName=args[i]; 
			 }
			 else if(StrArgs.equals("-b")) {
				 startStationName=args[i+1];
				 endStationName=args[i+2];
				 i=i+2;
			 }
			 else {
				 System.out.println("输入参数不正确，程序退出");
			 }
		 }
//		 FileName="D:\\Program Files (x86)\\eclipse-workspace\\PersonWork\\src\\subway.txt";
//		 LineName="二号线";
//		 OutFileName="D:\\Program Files (x86)\\eclipse-workspace\\PersonWork\\src\\c.txt";
		 Map map=new Map();
		 map.loadSubwayFile(FileName);
		 if(LineName!=null)
			 map.printStationsOfLine(LineName,OutFileName);
		 else if(startStationName!=null) {
			 Path text =map.shortedPath(startStationName, endStationName);
			 String s= map.printPath(text);
			 map.printFile(s, OutFileName);
	
		 }
		 }
}
