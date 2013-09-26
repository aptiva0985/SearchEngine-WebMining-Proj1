package booleanRetrieval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;

import booleanRetrieval.Node;
import booleanRetrieval.parseTree;
import booleanRetrieval.evalQuery.Myrankcomparator;
import booleanRetrieval.evalQuery.Myunrankcomparator;

/*
 * This is the main function
 * It will call all other functions to complete the retrieval procedure
 */
public class main {
	static int queryID = 0;
	static String inputPath = System.getProperty("user.dir") + "\\Structured-input.txt";
	//query file (a text file that store all queries)should be given by user
	static String outputPath = System.getProperty("user.dir") + "\\output.txt";
	//user can also edit the result output path and file name
	static String IsRanked = "Ranked";
	//We have two different approaches retrieving documents, Ranked and UnRanked.
	static ArrayList<String> queryInput = new ArrayList<String>();
	static ArrayList<Node> queryTree = new ArrayList<Node>();

	public static void main(String[] args) throws IOException {
		File inputFile = new File(inputPath);
		if (inputFile == null) return;

		queryInput = ReadFile(inputFile,queryInput);//get all query inputs
		for (String query:queryInput) {//traverse each query
			queryTree = new ArrayList<Node>();
			queryID = Integer.parseInt(query.split(":")[0]);

			//create the parse tree for current
			new parseTree();
			queryTree = parseTree.queryParse(query.split(":")[1], queryTree);

			//get evaluation result
			ArrayList<InveList> finalResult = new ArrayList<InveList>();
			new evalQuery();
			evalQuery.queryEvaluation(queryTree, queryID,IsRanked);
			finalResult = evalQuery.finalResult;

			//ranking evaluation result
			//for Ranked and UnRanked, there are different methods
			if (IsRanked == "Ranked") {
				Myrankcomparator comp1 = new Myrankcomparator();
				Collections.sort(finalResult,comp1);
			}
			else if (IsRanked == "UnRanked"){
				Myunrankcomparator comp2 = new Myunrankcomparator();
				Collections.sort(finalResult,comp2);
			}

			//output ranked result to appointed file
			File outputFile = new File(main.outputPath);
			if (outputFile == null) return;
			WriteFile(finalResult,outputFile);
			System.out.println(query.split(":")[1]);
		}

		System.out.println("Finished!");
	}

	public static ArrayList<String> ReadFile(File ReadFile, ArrayList<String> inputData) throws IOException{
		//Open input stream to get query input file

		FileInputStream IOStream = new FileInputStream(ReadFile);
		InputStreamReader read = new InputStreamReader(IOStream);
		BufferedReader reader = new BufferedReader(read);

		String str;
		while ((str = reader.readLine()) != null)//read date data line by line to the end of file
			inputData.add(str);

		//Close input stream
		reader.close();
		read.close();
		IOStream.close();
		return inputData;
	}

	public static void WriteFile(ArrayList<InveList> currentResult,File WriteFile) throws IOException{
		//write all ranked results to output file

		FileOutputStream IOStream = new FileOutputStream(WriteFile, true);
		OutputStreamWriter write = new OutputStreamWriter(IOStream);
		BufferedWriter writer = new BufferedWriter(write);
		String str = "";

		for (int i = 0; (i < 100) && (i < currentResult.size()); i++){
			InveList tmp = currentResult.get(i);

			////output string for trec_eval results
			if (IsRanked == "Ranked")
				str = queryID + " Q0 " + tmp.getDocID() + " " + (i+1) + " " + tmp.getTf() + " run-1";
			else if (IsRanked == "UnRanked")
				str = queryID + " Q0 " + tmp.getDocID() + " " + (i+1) + " 1 run-1";

//			//output string for Sample results
//			if (IsRanked == "Ranked")
//				str = (i+1) + " " + tmp.getDocID() + " "+ tmp.getTf();
//			else if (IsRanked == "UnRanked")
//				str = (i+1) + " " + tmp.getDocID() + " 1";


			writer.write(str);
			writer.newLine();
			writer.flush();
			write.flush();
		}

		writer.close();
		write.close();
		IOStream.close();
	}
}