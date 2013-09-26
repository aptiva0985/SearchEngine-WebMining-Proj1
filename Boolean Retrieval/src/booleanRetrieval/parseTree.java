package booleanRetrieval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import booleanRetrieval.Node;

/*
 * This class and queryParse function will turn query into parse tree
 * and return it to the main function
 */

public class parseTree {
	static int opNum ;
	static int nodeNum;
	static Node root= new Node();
	static Node currentNode;
	static Node lastNode;
	static int pointer;
	static int operatorLevel;

	parseTree() {
		opNum = 0;
		nodeNum = 0;
		root= new Node("#root");
		currentNode = root;
		lastNode = root;
		pointer = 0;
		operatorLevel = 0;
	}


	static ArrayList <Character> punc = new ArrayList <Character> ();
	static ArrayList <String> stopword = new ArrayList <String> ();


	public static ArrayList<Node> queryParse(String queryInput, ArrayList<Node> queryTree) throws IOException {
		char[] tmp = {' ', ')', '-', '\\', '/', ':', ';', ',', '!', '?', '@', '#', '%'};
		for (Character k :tmp )
			punc.add(k);
		//prepare the punctuation list

		String stopFilePath = System.getProperty("user.dir") + "\\stoplist.txt";
		File stopFile = new File(stopFilePath);
		if (stopFile == null) return queryTree;
		main.ReadFile(stopFile, stopword);
		//prepare the stopword list



		if (nodeNum == 0) {
			//there is a virtual #root tree for every parse tree
			queryTree.add(root);
			nodeNum++;
		}


		while (pointer != queryInput.length() - 1) {
			if (queryInput.charAt(pointer) != '#' && operatorLevel == 0){
				//forquery has no explicit operator
				//add a "OR" operator for it and move on
				Node newNode = new Node("#OR");
				newNode.setIsOperator(1);
				queryTree.add(newNode);

				currentNode.addSonNode(newNode);
				lastNode = currentNode;
				currentNode = newNode;
				opNum++;
				pointer+=2;
				operatorLevel++;
			}
			else if (queryInput.charAt(pointer) == '#') {
				//query has explicit operator
				pointer++;
				if (queryInput.charAt(pointer) == 'A') {
					//we get an "#AND" operator
					//put it in the parse tree and move on
					Node newNode = new Node("#AND");
					newNode.setIsOperator(1);
					queryTree.add(newNode);

					currentNode.addSonNode(newNode);
					lastNode = currentNode;
					currentNode = newNode;
					opNum++;
					pointer+=3;
					operatorLevel++;
				}
				else if (queryInput.charAt(pointer) == 'O') {
					//we get an "#OR" operator
					//put it in the parse tree and move on
					Node newNode = new Node("#OR");
					newNode.setIsOperator(1);
					queryTree.add(newNode);

					currentNode.addSonNode(newNode);
					lastNode = currentNode;
					currentNode = newNode;
					opNum++;
					pointer+=2;
					operatorLevel++;
				}
				else if (queryInput.charAt(pointer) == 'N') {
					//we get an "#NEAR/n" operator
					//put it in the parse tree and move on
					pointer+=5;
					char n = queryInput.charAt(pointer);
					String temp = "#NEAR/" + n;
					Node newNode = new Node(temp);
					newNode.setIsOperator(1);
					queryTree.add(newNode);

					currentNode.addSonNode(newNode);
					lastNode = currentNode;
					currentNode = newNode;
					opNum++;
					pointer++;
					operatorLevel++;
				}
			}
			else {
				while (queryInput.charAt(pointer) != ')') {
					pointer++;
					if (queryInput.charAt(pointer) == '#') {
						queryParse(queryInput, queryTree);
						//if we have nested query
						//recursive call queryParse function
					}
					else {//if we get a query term, add it as a leaf node
						String subString = "";
						while (punc.indexOf(queryInput.charAt(pointer)) == -1) {
							subString+=queryInput.charAt(pointer);
							pointer++;
						}

						//get the query term's field
						//for term without field, default value is .title
						String queryWord = "";
						String queryField = "";
						if (subString.indexOf('.') == -1) {
							queryWord = subString;
							queryField = "body";
						}
						else {
							queryWord = subString.split("\\.")[0];
							queryField = subString.split("\\.")[1];
						}

						//drop stopwords
						if (stopword.indexOf(queryWord) != -1)
							continue;

						//ingore terms that doesn;'t appear in index
						if (evalQuery.retrieval(queryWord) == null)
							continue;

						//add current leaf node to the parse tree
						Node newNode = new Node();
						if (queryField.equals("body"))
							 newNode.setName(queryWord);
						else newNode.setName(queryWord + "." + queryField);

						newNode.setIsOperator(0);
						newNode.setField(queryField);
						queryTree.add(newNode);

						currentNode.addSonNode(newNode);
					}
				}
				operatorLevel--;//a ")" implies the end of an operator's field ends
				if (pointer != queryInput.length() - 1){
					pointer++;
				}

				currentNode = lastNode;

			}
		}
		return queryTree;
		// return the parse tree
	}
}