package booleanRetrieval;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import booleanRetrieval.Node;
import booleanRetrieval.InveList;

/*
 * The queryEvaluation function use recursive method to execute the query
 * and return the query result with socre for ranking
 */
public class evalQuery {

	static Node currentNode;
	static ArrayList<InveList> finalResult;

	evalQuery() {//when starting query evaluation, start from root node
		currentNode = main.queryTree.get(0).getSonNode().get(0);
		finalResult = new ArrayList<InveList>();
	}

	public static ArrayList<InveList> queryEvaluation (ArrayList<Node> queryTree, int queryID, String IsRanked) throws IOException {
		ArrayList<InveList> result = new ArrayList<InveList>();


		if (currentNode.getIsOperator() == 0) {
			//use recursive method
			//if current node is not an operator
			//return its inverted list
			return retrieval(currentNode.getName());
		}
		else if (currentNode.getIsOperator() == 1) {
			//if current node is an operator
			//traverse all its son node and do corresponding operation
			for (Node childNode : currentNode.getSonNode()) {
				ArrayList<InveList> ret = new ArrayList<InveList>();

				Node tmp = currentNode;
				currentNode = childNode;
				ret = queryEvaluation(queryTree, queryID,IsRanked);
				currentNode = tmp;

				if (result.size() == 0)
					//for the initial situation,
					//copy the first son node's inverted list to the temp result
					result = (ArrayList<InveList>) ret.clone();

				//do operation according to the operator type
				if (currentNode.getName().equals("#OR"))
					result = orOperate(result ,ret);
				else if (currentNode.getName().equals("#AND"))
					result = andOperate(result ,ret);
				else if (currentNode.getName().substring(0, 6).equals("#NEAR/"))
					result = nearOperate(result ,ret);
			}
		}
		//the final result save in this ArrayList
		finalResult = (ArrayList<InveList>) result.clone();
		return result;
	}

	public static ArrayList<InveList> retrieval (String nodeName) throws IOException {
		//get inveted list for certain query term from local file
		ArrayList<InveList> retrievalResult = new ArrayList<InveList>();
		String filePath = System.getProperty("user.dir") + "\\clueweb09_wikipedia_15p_invLists\\" + nodeName +".inv";

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			return null;
		}

		String line = br.readLine();
		line = br.readLine();
		if (line == null)
			return retrievalResult;

		//get every inverted list item and store it into the InveList structure
        for (; line != null; line = br.readLine()) {
        	String[] para = line.split(" ");
        	InveList temp= new InveList();

        	temp.setDocID(Integer.parseInt(para[0]));
        	temp.setTf(Integer.parseInt(para[1]));
        	temp.setDocLen(Integer.parseInt(para[2]));
        	for (int i = 3; i < temp.getTf() + 3; i++)
        		temp.Pos.add(Integer.parseInt(para[i]));

        	retrievalResult.add(temp);
        }
        br.close();

		return retrievalResult;
	}

	public static ArrayList<InveList> orOperate (ArrayList<InveList>result , ArrayList<InveList>ret){
		//this function can execute "#OR" operator
		int i = 0;
		int j = 0;
		//two pointers for two inverted list of two documents
		ArrayList<InveList> tempResult = new ArrayList<InveList>();

		while ((i < result.size()) || (j < ret.size())) {

			//for the situation that either inverted lists has not reach its end
			//but the other has not

			if ((i == result.size()) && (j < ret.size())){
				while (j < ret.size()){
					InveList tmp = new InveList(ret.get(j).getDocID());
					tmp.setTf(ret.get(j).getTf());
					tempResult.add(tmp);
					j++;
				}
			}
			else if ((i < result.size()) && (j == ret.size())){
				while (i < result.size()){
					InveList tmp = new InveList(result.get(i).getDocID());
					tmp.setTf(result.get(i).getTf());
					tempResult.add(tmp);
					i++;
				}
			}
			if ((i == result.size()) && (j == ret.size()))
				break;

			int ID1 = result.get(i).getDocID();
			int ID2 = ret.get(j).getDocID();

			//for the current situation
			//if two pointers point to same document
			//add it into result (score should be bigger Tf)
			//move both pointer
			if (ID1 == ID2) {
				InveList tmp = new InveList(result.get(i).getDocID());
				tmp.setTf(Math.max(result.get(i).getTf(), ret.get(j).getTf()));
				tempResult.add(tmp);

				i++;
				j++;
			}
			else if (ID1 < ID2) {
				//if two pointers point to different document
				//add it into result (score should be Tf)
				//move the smaller pointer
				InveList tmp = new InveList(result.get(i).getDocID());
				tmp.setTf(result.get(i).getTf());
				tempResult.add(tmp);

				i++;
			}
			else if (ID1 > ID2) {
				//if two pointers point to different document
				//add it into result (score should be Tf)
				//move the smaller pointe
				InveList tmp = new InveList(ret.get(j).getDocID());
				tmp.setTf(ret.get(j).getTf());
				tempResult.add(tmp);

				j++;
			}
		}
		return tempResult;
	}

	public static ArrayList<InveList> andOperate (ArrayList<InveList>result, ArrayList<InveList>ret){
		//this function can execute "#AND" operator
		int i = 0;
		int j = 0;
		//two pointers for two inverted list of two documents
		ArrayList<InveList> tempResult = new ArrayList<InveList>();

		while ((i < result.size()) && (j < ret.size())) {
			int ID1 = result.get(i).getDocID();
			int ID2 = ret.get(j).getDocID();

			if (ID1 == ID2) {
				//if two pointers point to same document
				//add it into result (score should be smaller Tf)
				//move both pointer
				int newTf = Math.min(result.get(i).getTf(), ret.get(j).getTf());

				InveList tmp = new InveList(result.get(i).getDocID());
				tmp.setTf(newTf);
				tempResult.add(tmp);

				i++;
				j++;
			}
			else if (ID1 < ID2) {
				//if two pointers point to different document
				//move the smaller pointer
				i++;
			}
			else if (ID1 > ID2) {
				//if two pointers point to different document
				//move the smaller pointer
				j++;
			}
		}
		return tempResult;
	}

	public static ArrayList<InveList> nearOperate (ArrayList<InveList>result , ArrayList<InveList>ret){
		int n = Integer.parseInt(currentNode.getName().substring(6, 7));
		int i = 0;
		int j = 0;
		//two pointers for two inverted list of two documents
		//and the near range is stored in variable n
		ArrayList<InveList> tempResult = new ArrayList<InveList>();

		while ((i < result.size()) && (j < ret.size())) {
			int ID1 = result.get(i).getDocID();
			int ID2 = ret.get(j).getDocID();

			if (ID1 == ID2) {
				//if two pointers point to same document
				//get two position lists
				ArrayList<Integer> posList1 = result.get(i).getPos();
				ArrayList<Integer> posList2 = ret.get(j).getPos();
				ArrayList<Integer> samePosList = new ArrayList<Integer>();
				int posPointer1 = 0;
				int posPointer2 = 0;
				//have two new pointer for position lists
				int sameCount = 0;

				while ((posPointer1 < posList1.size()) && (posPointer2 < posList2.size())) {
					if (posList2.get(posPointer2) - posList1.get(posPointer1) > n) {
						posPointer1++;
					}
					else if (posList2.get(posPointer2) - posList1.get(posPointer1) < 0) {
						posPointer2++;
					}
					else if (posList2.get(posPointer2) - posList1.get(posPointer1) <= n){
						//only document with word1 prior to words within n-1 words can be treat as a match
						sameCount++;
						samePosList.add(posList2.get(posPointer2));
						posPointer1++;
						posPointer2++;
					}
				}
				if (sameCount != 0){
					//if this document has position matches
					//add it into the result
					//score will be match time
					InveList tmp = new InveList(result.get(i).getDocID());
					tmp.setTf(sameCount);
					tmp.setPos(samePosList);
					tempResult.add(tmp);
				}
				i++;
				j++;
			}
			else if (ID1 > ID2)
				//if two pointers point to different document
				//move the smaller pointer
				j++;
			else if (ID1 < ID2)
				//if two pointers point to different document
				//move the smaller pointer
				i++;
		}


		return tempResult;
	}

	public static class Myrankcomparator implements Comparator<Object>{
		//Comparator for Ranked approach
		//the first sort key is score
		//the second sort key is DocID

	    public int compare(Object o1,Object o2) {
	    	InveList result1 = (InveList)o1;
	    	InveList result2 = (InveList)o2;
	    	if(result1.getTf() < result2.getTf())
	    		return 1;
	    	else if (result1.getTf() > result2.getTf())
	    		return -1;
	    	else if (result1.getTf() == result2.getTf()) {
	    		if (result1.getDocID() < result2.getDocID())
	        	   return 1;
	    	}
	    	return -1;
	    }
	}

	public static class Myunrankcomparator implements Comparator<Object>{
		//Comparator for UnRanked approach
		//the only sort key is DocID

	    public int compare(Object o1,Object o2) {
	    	InveList result1=(InveList)o1;
	    	InveList result2=(InveList)o2;

	    	if (result1.getDocID() < result2.getDocID())
	    		return 1;
	    	else
	    		return -1;
	    }
	}

}
