package fr.insarennes.fafdti.builder.nodebuilder;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.NullArgumentException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper.Context;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.namesinfo.AttrSpec;
import fr.insarennes.fafdti.builder.namesinfo.AttrType;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoreLeftDistribution;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.namesinfo.TextAttrSpec;
import fr.insarennes.fafdti.builder.nodebuilder.INodeBuilder;
import fr.insarennes.fafdti.builder.gram.FGram;
import fr.insarennes.fafdti.builder.gram.GramType;
import fr.insarennes.fafdti.builder.gram.SGram;
import fr.insarennes.fafdti.hadoop.QuestionScoreLeftDistribution;
import fr.insarennes.fafdti.visitors.QuestionExample;
import fr.insarennes.fafdti.Pair;

public class NodeBuilderFast implements INodeBuilder {

	private String[][] database;
	private Criterion criterion;
	private DotNamesInfo namesInfo;
	private String id;
	private ScoredDistributionVector parentDistribution;
	private Question bestQuestion;
	private ScoreLeftDistribution bestSLDist;
	private Map<Question, ScoredDistributionVector> questionDistribution;

	/*
	 * Make it thread safe private String[][] database; private
	 * ScoredDistributionVector parentDist; private String id; private Question
	 * bestQuestion; private ScoreLeftDistribution bestSLDist; private
	 * Map<Question, ScoredDistributionVector> questionDistribution = new
	 * HashMap<Question, ScoredDistributionVector>();
	 */

	public NodeBuilderFast(Criterion criterion, DotNamesInfo namesInfo,
			String[][] database, ScoredDistributionVector parentDistribution,
			String id) throws FAFException {
		this.criterion = criterion;
		this.namesInfo = namesInfo;
		this.id = id;
		this.database = database;
		this.questionDistribution = new HashMap<Question, ScoredDistributionVector>();
		if(parentDistribution == null) {
			parentDistribution = new ScoredDistributionVector(
					namesInfo.numOfLabel());
			for (String[] example : database) {
				String label = example[example.length - 1];
				parentDistribution.incrStat(namesInfo.indexOfLabel(label));
			}
			parentDistribution.rate(criterion);
		}
		this.parentDistribution = parentDistribution;
	}

	@Override
	public QuestionScoreLeftDistribution buildNode() throws IOException,
			InterruptedException, ClassNotFoundException, FAFException {
		System.out.println("Database size: " + database.length);
		// Set database and parent distribution
		if (parentDistribution == null) {
			throw new NullArgumentException("parentDistribution cannot be null");
		}
		QuestionScoreLeftDistribution bestQSLDist = null;
		// Iterate over all continuous attributes
		for (int i = 0; i < namesInfo.numOfAttr(); ++i) {
			AttrType attrType = namesInfo.getAttrSpec(i).getType();
			if (attrType == AttrType.CONTINUOUS) {
				generateContinuousQuestion(i);
			}
		}
		// Iterate over all examples of the database to
		// generate discrete and text question
		for (String[] example : database) {
			generateDiscreteTextQuestions(example);
		}
		computeQuestionDistribution();
		System.out.println("Question: " + bestQuestion);
		return new QuestionScoreLeftDistribution(bestQuestion, bestSLDist);
	}

	private void generateDiscreteTextQuestions(String[] example)
			throws FAFException, IOException, InterruptedException {
		// Parse example
		int labelIndex = namesInfo.indexOfLabel(example[example.length - 1]);
		// Generate all text and discrete questions
		for (int i = 0; i < example.length - 1; ++i) {
			AttrSpec attrSpec = namesInfo.getAttrSpec(i);
			AttrType attrType = attrSpec.getType();
			if (attrType == AttrType.DISCRETE) {
				Question q = new Question(i, attrType, example[i]);
				addQuestion(q, labelIndex);
			} else if (attrType == AttrType.TEXT) {
				String[] words = example[i].split("\\s+");
				TextAttrSpec textAttr = (TextAttrSpec) attrSpec;
				if (textAttr.getExpertType() == GramType.SGRAM) {
					Set<SGram> sGramSet = GramGenerator.generateSGram(textAttr,
							words);
					for (SGram sGram : sGramSet) {
						Question q = new Question(i, attrType, sGram);
						addQuestion(q, labelIndex);
					}
				} else {
					Set<FGram> fGramSet = GramGenerator.generateAllNFGram(
							textAttr, words);
					for (FGram fGram : fGramSet) {
						Question q = new Question(i, attrType, fGram);
						addQuestion(q, labelIndex);
					}
				}
			}
		}
	}

	private void generateContinuousQuestion(int attrIdx)
			throws FAFException {
		SortedMap<Double, ScoredDistributionVector> valueDistMap = new TreeMap<Double, ScoredDistributionVector>();
		for (String[] example : database) {
			double curValue = Double.parseDouble(example[attrIdx]);
			ScoredDistributionVector curDist = null;
			if (valueDistMap.containsKey(curValue)) {
				curDist = valueDistMap.get(curValue);
			} else {
				curDist = new ScoredDistributionVector(namesInfo.numOfLabel());
				valueDistMap.put(curValue, curDist);
			}
			String label = example[example.length - 1];
			curDist.incrStat(namesInfo.indexOfLabel(label));
		}
		ThresholdComputer thresholdComputer = new ThresholdComputer(namesInfo,
				parentDistribution, criterion);
		if (valueDistMap.size() >= 2) {
			Pair<Double, ScoreLeftDistribution> p = thresholdComputer
					.computeThreshold(valueDistMap);
			double threshold = p.getFirst();
			ScoreLeftDistribution sLDist = p.getSecond();
			AttrSpec attrSpec = namesInfo.getAttrSpec(attrIdx);
			AttrType attrType = attrSpec.getType();
			Question q = new Question(attrIdx, attrType, threshold);
			bestQuestionCandidate(q, sLDist);
		}
	}

	private void addQuestion(Question q, int labelIndex) {
		// Modify distribution of the generated question
		if (!questionDistribution.containsKey(q)) {
			questionDistribution.put(q,
					new ScoredDistributionVector(namesInfo.numOfLabel()));
		}
		ScoredDistributionVector dist = questionDistribution.get(q);
		dist.incrStat(labelIndex);
	}

	private void computeQuestionDistribution() {
		for (Map.Entry<Question, ScoredDistributionVector> e : questionDistribution
				.entrySet()) {
			Question q = e.getKey();
			ScoredDistributionVector leftDist = e.getValue();
			leftDist.rate(criterion);
			ScoredDistributionVector rightDist = parentDistribution
					.computeRightDistribution(leftDist);
			rightDist.rate(criterion);
			ScoreLeftDistribution sLDist = new ScoreLeftDistribution(leftDist,
					rightDist);
			bestQuestionCandidate(q, sLDist);
		}
	}

	private void bestQuestionCandidate(Question q, ScoreLeftDistribution sLDist) {
		// System.out.println("Candidate: " + q + " " + sLDist.getScore());
		if (bestQuestion == null || sLDist.getScore() < bestSLDist.getScore()) {
			bestSLDist = sLDist;
			bestQuestion = q;
		}
	}

	@Override
	public fr.insarennes.fafdti.Pair<Path, Path> getSplitPath()
			throws IOException {
		return null;
	}

	@Override
	public Pair<String[][], String[][]> getSplitData() throws FAFException {
		System.out.println("Split: " + bestQuestion);
		ArrayList<String[]> leftDatabaseList = new ArrayList<String[]>();
		ArrayList<String[]> rightDatabaseList = new ArrayList<String[]>();
		for (String[] example : database) {
			if (bestQuestion.ask(example)) {
				leftDatabaseList.add(example);
			} else {
				//System.out.println(Arrays.toString(example));
				rightDatabaseList.add(example);
			}
		}
		String[][] leftDatabase = stringArrayArrayListToArrayArray(leftDatabaseList);
		String[][] rightDatabase = stringArrayArrayListToArrayArray(rightDatabaseList);
		System.out.println("Database : " + database.length + " Left: "
				+ leftDatabase.length + " Right: " + rightDatabase.length);
		return new Pair<String[][], String[][]>(leftDatabase, rightDatabase);
	}

	@Override
	public void cleanUp() {
		// We need to do nothing here
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public ScoredDistributionVector getDistribution() throws FAFException {
		return this.parentDistribution;
	}

	public static String[][] stringArrayArrayListToArrayArray(
			ArrayList<String[]> arrayList) {
		String[][] array = new String[arrayList.size()][];
		for (int i = 0; i < arrayList.size(); ++i) {
			array[i] = arrayList.get(i);
		}
		return array;
	}
}
