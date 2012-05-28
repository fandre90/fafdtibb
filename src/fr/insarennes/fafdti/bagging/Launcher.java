package fr.insarennes.fafdti.bagging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.Chrono;
import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.ParseException;
import fr.insarennes.fafdti.builder.StatBuilder;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.nodebuilder.FatNodeBuilderFactory;
import fr.insarennes.fafdti.builder.nodebuilder.NodeBuilder;
import fr.insarennes.fafdti.builder.nodebuilder.NodeBuilderFast;
import fr.insarennes.fafdti.builder.nodebuilder.NodeBuilderFurious;
import fr.insarennes.fafdti.builder.scheduler.Scheduler;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.builder.treebuilder.LimitModeTreeBuilderFactory;
import fr.insarennes.fafdti.cli.FAFExitCode;
import fr.insarennes.fafdti.tree.DecisionTree;
import fr.insarennes.fafdti.tree.DecisionTreeHolder;
import fr.insarennes.fafdti.visitors.XmlConst;
import fr.insarennes.fafdti.visitors.XmlExporter;

/**
 * Classe permettant de lancer le processus de construction d'un
 * {@link BaggingTrees} De plus, lorsque ceci est fini, on check le résultat et
 * on l'exporte en xml.
 * 
 * Remarque : on peut construire un arbre de décision ordinaire en précisant le
 * nombre d'arbre à construire égal à 1 (dans ce cas on ne fera plus de
 * bagging).
 */

public class Launcher implements Observer {
	private Logger log = Logger.getLogger(Launcher.class);

	// Number of trees constructed by bagging algorithm
	private int nbBagging;
	// Number of tree fully constructed
	private int baggingDone;
	// List of trees' roots
	private List<DecisionTreeHolder> roots;
	// Output path with filename (without extension)
	private String outXml;
	// Rate of initial data to build each trees of bagging (between 0 and 1)
	private double baggingPercent;
	// Directory name where generated splitted data files
	private final String baggingDir = "bagging-data";
	// Comments to add in xml output
	private Map<String, String> comment;
	// Timer to get time to make process fully
	private Chrono chrono;
	// Le .names
	private DotNamesInfo dotNamesInfo;

	public Launcher(String inputNames,
			String inputData,
			String outputDir, // working dir
			String xmlOutput, List<StoppingCriterion> stoppingList,
			Criterion criterion, int nbBagging, double baggingPercent,
			Map<String, String> comment, double limitmode)
			throws ParseException, IOException {
		// attributes initialization
		this.roots = new ArrayList<DecisionTreeHolder>(nbBagging);
		this.outXml = xmlOutput;
		this.nbBagging = nbBagging;
		this.baggingDone = 0;
		this.baggingPercent = baggingPercent;
		this.comment = comment;
		// launch timer
		chrono = new Chrono();
		chrono.start();
		// launch process
		// DotNamesInfos creation
		Configuration conf = new Configuration();
		FileSystem fileSystem = null;
		this.dotNamesInfo = null;
		try {
			fileSystem = FileSystem.get(conf);
			this.dotNamesInfo = new DotNamesInfo(new Path(inputNames),
					fileSystem);
		} catch (IOException e1) {
			log.error(e1.getMessage());
			log.error("Could not parse .names file. Aborting.");
			System.exit(FAFExitCode.EXIT_ERROR);
		}
		// get data split
		List<String> datasplit = splitData(inputData, outputDir, fileSystem);
		log.info("Data file splitting done");
		// launch every tree
		for (int i = 0; i < nbBagging; i++) {
			// statistiques object creation
			StatBuilder stats = new StatBuilder(1);
			stats.addObserver(this);
			// data file
			String data = datasplit.get(i);
			// NodeBuilder creation
			roots.add(i, new DecisionTreeHolder());
			LimitModeTreeBuilderFactory tbm = new LimitModeTreeBuilderFactory(
					limitmode);
			FatNodeBuilderFactory nodeBuilderFactory = new FatNodeBuilderFactory(
					criterion, dotNamesInfo);
			Runnable tb = tbm.makeTreeBuilder(this.dotNamesInfo, outputDir,
					criterion, roots.get(i).getNodeSetter(), stoppingList,
					stats, nodeBuilderFactory, String.valueOf(i), data, tbm, Scheduler.INSTANCE);
			// launch first node
			Scheduler.INSTANCE.execute(tb);
		}
	}

    /** Génère une liste triée d'entiers aléatoires compris entre 0 et max
     * @param size la taille de la liste a générée
     * @param max le max
     * @return la liste générée
     */
    public static int[] getSortedRandomIntList(int size, int max){
            int[] res = new int[size];
            for(int i=0 ; i<size ; i++){
                    res[i] = (int)(Math.random() * max); 
            }
            Arrays.sort(res);
            return res;
    }

	private List<String> splitData(String inputData, String outputDir,
			FileSystem filesystem) {
		List<String> res = new ArrayList<String>();
		try {
			comment.put(XmlConst.FILESIZE,
					filesystem.getFileStatus(new Path(inputData)).getLen()
							+ "octets");
		} catch (IOException e1) {
			log.error(e1.getMessage());
		}

		if (nbBagging == 1) {
			comment.put(XmlConst.DATARATE, "1.0");
			res.add(inputData);
		} else {
			comment.put(XmlConst.DATARATE, String.valueOf(baggingPercent));
			// open data file
			FSDataInputStream in = null;
			try {
				in = filesystem.open(new Path(inputData));
			} catch (IOException e) {
				log.error("Cannot open " + inputData);
			}
			// count number of examples (=lines)
			LineReader lr = new LineReader(in);
			Text text = new Text();
			int total = 0;
			try {
				while (lr.readLine(text) != 0)
					total++;
			} catch (IOException e) {
				log.error("Error occured while counting number of lines of "
						+ inputData);
			}
			// number of lines by tree
			int nbLines = (int) (baggingPercent * total);
			// random lines for each bagging data files and associated writers
			// construction
			int[][] lines = new int[nbBagging][nbLines];
			// writers list creation
			List<FSDataOutputStream> writers = new ArrayList<FSDataOutputStream>();
			Path path = new Path(outputDir, baggingDir);
			// inputData without ".data"
			String data = inputData.substring(
					inputData.lastIndexOf(File.separatorChar) + 1,
					inputData.lastIndexOf('.'));
			for (int i = 0; i < nbBagging; i++) {
				lines[i] = getSortedRandomIntList(nbLines, total);
				FSDataOutputStream fw = null;
				try {
					Path p = new Path(path, data + i + ".data");
					fw = filesystem.create(p);
					res.add(p.toString());
				} catch (IOException e) {
					log.error("Error occured while files creation : "
							+ e.getMessage());
				}
				writers.add(fw);
			}
			// construction of map<Line, List<Writer>> to optimize file reading
			// warning : keep duplicate lines !
			Map<Integer, List<FSDataOutputStream>> map = new HashMap<Integer, List<FSDataOutputStream>>(
					total);
			for (int i = 0; i < nbBagging; i++) {
				for (int j = 0; j < nbLines; j++) {
					int key = lines[i][j];
					if (map.get(key) == null)
						map.put(key, new ArrayList<FSDataOutputStream>());
					map.get(key).add(writers.get(i));
				}
			}
			// raz inputData
			try {
				in = filesystem.open(new Path(inputData));
				lr = new LineReader(in);
			} catch (IOException e) {
				log.error("Error occured while raz of data file : "
						+ e.getMessage());
			}
			// data files writing
			Text txt = new Text();
			int lind = 0;
			try {
				while (lr.readLine(txt) != 0) {
					List<FSDataOutputStream> list = map.get(lind++);
					if (list != null) {
						Iterator<FSDataOutputStream> it2 = list.iterator();
						while (it2.hasNext())
							it2.next().writeBytes(txt.toString() + "\n");
					}
				}
			} catch (IOException e) {
				log.error("Error occured while reading " + inputData);
			}
			// close everything
			try {
				in.close();
				for (FSDataOutputStream w : writers) {
					w.flush();
					w.close();
				}
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
		return res;
	}

	public void update(Observable arg0, Object arg1) {
		if (((Integer) arg1).equals(0)) {
			synchronized (this) {
				baggingDone++;
			}
			log.info(baggingDone + " tree(s) fully constructed");
		}

		if (baggingDone == nbBagging) {
			try {
				chrono.stop();
			} catch (FAFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finish();
		}
	}

	private void finish() {
		// stop scheduler
		Scheduler.INSTANCE.shutdown();
		log.info("Construction process done");
		// get decision trees constructed
		List<DecisionTree> trees = new ArrayList<DecisionTree>();
		for (int i = 0; i < nbBagging; i++) {
			try {
				trees.add(roots.get(i).getRoot());
			} catch (FAFException e) {
				log.error(e.getMessage());
			}
		}
		// bagging trees construction
		BaggingTrees result = new BaggingTrees(trees);
		// check it and stats
		BaggingStatChecker check = new BaggingStatChecker(result);
		check.launch();
		if (!check.checkOK()) {
			log.error("Construction failed : pending found");
			return;
		}
		log.info("Validation tree resul : check OK");

		long timer = chrono.getTimeInMinutes();
		log.info("-------Stats-------");
		log.info("Process done in " + timer + " minutes");
		String[] str = check.toString().split("\n");
		for (String s : str)
			log.info(s);
		log.info("-------------------");
		// export xml
		comment.put(XmlConst.TIME, String.valueOf(timer) + "minutes");
		XmlExporter xml = new XmlExporter(result, comment,
				this.dotNamesInfo);
		xml.exportToFile(outXml);
		log.info("Tree resulting exports in " + outXml);
	}
}
