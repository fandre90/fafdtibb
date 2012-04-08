package fr.insarennes.fafdti.cli;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.insarennes.fafdti.visitors.XmlConst;

public class HtmlStater {
	private static Logger log = Logger.getLogger(HtmlStater.class);
	Map<String, String> buildopts;
	QueryStater stater;
	String output;
	
	public HtmlStater(Map<String,String> buildopts, QueryStater stater){
		this.buildopts = buildopts;
		this.stater = stater;
	}
	
	public void make(String output){
		this.output = output;
		this.launch();
	}
	
	private void launch(){
		Writer writer = null;
		try {
			writer = new FileWriter(output+".html");
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return;
		}
		PrintWriter print = new PrintWriter(writer);
		String date = (new Date()).toString();
		print.write(
		"<html>" +
				"<title>Report " + output + " generated on " + date +
				"</title>" +
				"<H1 align=center>Report generated on " + date +
				"<br>Database : " + buildopts.get(XmlConst.DATA) + "</H1><br>" +
				"<h3 align=center> with the following parameters :</h3><br>" +
				"<TABLE BORDER=\"1\">" +
				"<TR>" +
				"<TH> names </TH>" +
				"<TH> bagging </TH>" +
				"<TH> data rate </TH>" +
				"<TH> criterion </TH>" +
				"<TH> minimum gain</TH>" +
				"<TH> minimum examples by leaf </TH>" +
				"<TH> maximum depth</TH>" +
				"<TH> built in </TH>" +
				"<TH> pool size </TH>" +
				"<TH> data file size </TH>" +
				"</TR>" +
				"<TR>" +
				"<TD> " + buildopts.get(XmlConst.NAMES) + "</TD>" +
				"<TD> " + buildopts.get(XmlConst.BAGGING) + "</TD>" +
				"<TD> " + buildopts.get(XmlConst.DATARATE) + "</TD>" +
				"<TD> " + buildopts.get(XmlConst.CRITERION) + "</TD>" +
				"<TD> " + buildopts.get(XmlConst.GAINMIN) + "</TD>" +
				"<TD> " + buildopts.get(XmlConst.MINEX) + "</TD>" +
				"<TD> " + buildopts.get(XmlConst.MAXDEPTH) + "</TD>" +
				"<TD> " + buildopts.get(XmlConst.TIME) + "</TD>" +
				"<TD> " + buildopts.get(XmlConst.THREADS) + "</TD>" +
				"<TD> " + buildopts.get(XmlConst.FILESIZE) + "</TD>" +
				"</TR>" +
				"</TABLE>");
		
		Set<Entry<String, Integer>> searchByLabel = stater.getSearchByLabel().entrySet();
		Map<String, Integer> correctByLabel = stater.getCorrectByLabel();
		Map<String, Integer> errorByLabel = stater.getErrorByLabel();
		Map<String, Integer> foundByLabel = stater.getFoundByLabel();
		int totalSearch, totalFound, totalCorrect, totalError;
		double totalPrecision, totalRecall, nbLabels;
		totalCorrect=totalError=totalFound=totalSearch=0;
		totalPrecision=totalRecall=nbLabels=0;
		print.write(
				"<h3 align=center> errors statistics :</h3><br>" +
				"<TABLE BORDER=\"1\">" +
				"<TR>" +
				"<TH> label </TH>" +
				"<TH> tested </TH>" +
				"<TH> classified </TH>" +
				"<TH> correct </TH>" +
				"<TH> error </TH>" +
				"<TH> precision </TH>" +
				"<TH> recall </TH>" +
				"<TH> error rate </TH>" +
				"</TR>");
		for(Entry<String, Integer> e : searchByLabel){
			String key = e.getKey();
			int found = foundByLabel.get(key);
			int error = errorByLabel.get(key);
			int correct = correctByLabel.get(key);
			int search = e.getValue();
			totalCorrect+=correct;
			totalError+=error;
			totalFound+=found;
			totalSearch+=search;
			nbLabels++;
			double precision = (double)correct / (double)found;
			double recall = (double)correct / (double)search;
			totalPrecision+=precision;
			totalRecall+=recall;
			print.write(
					"<TR>" +
					"<TD> " + key + "</TD>" +
					"<TD> " + String.valueOf(search) + "</TD>" +
					"<TD> " + String.valueOf(found) + "</TD>" +
					"<TD> " + String.valueOf(correct) + "</TD>" +
					"<TD> " + String.valueOf(error) + "</TD>" +
					"<TD> " + String.valueOf(precision) + "</TD>" +
					"<TD> " + String.valueOf(recall) + "</TD>" +
					"<TD> " + String.valueOf((double)error / (double)search) + "</TD>" +
					"</TR>");
		}
		print.write(
				"<TR>" +
				"<TD> total </TD>" +
				"<TD>" + String.valueOf(totalSearch) + "</TD>" +
				"<TD>" + String.valueOf(totalFound) + "</TD>" +
				"<TD>" + String.valueOf(totalCorrect) + "</TD>" +
				"<TD>" + String.valueOf(totalError) + "</TD>" +
				"<TD>" + String.valueOf(totalPrecision / nbLabels) + "</TD>" +
				"<TD>" + String.valueOf(totalRecall / nbLabels) + "</TD>" +
				"<TD>" + String.valueOf((double)totalError / (double)totalSearch) + "</TD>");
		print.write("</TABLE>" +
					"</html>");
		print.flush();
		print.close();
	}
}