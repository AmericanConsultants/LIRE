package net.semanticmetadata.lire.indexers.parallel;

import java.io.File;
import java.util.LinkedList;

import net.semanticmetadata.lire.aggregators.BOVW;
import net.semanticmetadata.lire.aggregators.VLAD;
import net.semanticmetadata.lire.classifiers.Cluster;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.EdgeHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.JCD;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSiftExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;

public class IndexRunner {

	public static void main(String[] args) throws  Exception{

        String indexPath = null;
        String imageDirectory = null;
        File imageList = null;
        int numThreads = 10;
        int numOfClusters = 512;
        int numOfDocsForVocabulary = 10000;         		        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-i")) {  // index
                if ((i + 1) < args.length) {
                    indexPath = args[i + 1];
                }
            } else if (arg.startsWith("-n")) { // number of Threads
                if ((i + 1) < args.length) {
                    try {
                        numThreads = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException e) {
                        System.err.println("Could not read number of threads: " + args[i + 1] + "\nUsing default value " + numThreads);
                    }
                }
            } else if (arg.startsWith("-l")) { // list of images in a file ...
                imageDirectory = null;
                if ((i + 1) < args.length) {
                    imageList = new File(args[i + 1]);
                    if (!imageList.exists()) {
                        System.err.println(args[i + 1] + " does not exits!");
                        System.exit(-1);
                    }
                }
            }else if (arg.startsWith("-c")) { // number of clusters
                if ((i + 1) < args.length) {
                    try {
                        numOfClusters = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException e) {
                        System.err.println("Could not read number of clusters: " + args[i + 1] + "\nUsing default value " + numOfClusters);
                    }
                }
            }else if (arg.startsWith("-v")) { // number of docs for vocabulary
                if ((i + 1) < args.length) {
                    try {
                        numOfDocsForVocabulary = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException e) {
                        System.err.println("Could not read number of docs for vocabulary: " + args[i + 1] + "\nUsing default value " + numOfDocsForVocabulary);
                    }
                }
            } else if (arg.startsWith("-d")) { // image directory
                if ((i + 1) < args.length) {
                    imageDirectory = args[i + 1];
                }
            }
        }

        if (indexPath == null) {
            System.exit(-1);
        } else if (imageList == null && (imageDirectory == null || !new File(imageDirectory).exists())) {
            System.exit(-1);
        }
        

        ParallelIndexer parallelIndexer;
        
        if (imageList != null) {
        	parallelIndexer = new ParallelIndexer(numThreads, indexPath, imageList, numOfClusters, numOfDocsForVocabulary, VLAD.class);
        } else {
        	parallelIndexer = new ParallelIndexer(numThreads, indexPath, imageDirectory, numOfClusters, numOfDocsForVocabulary, VLAD.class);
        }
//        parallelIndexer.addExtractor(CEDD.class);
//        parallelIndexer.addExtractor(JCD.class);
//        parallelIndexer.addExtractor(EdgeHistogram.class);
//       	  parallelIndexer.addExtractor(CvSurfExtractor.class, Cluster.readClusters("/Users/greensod/usptoWork/TrademarkRefiles/data/lireData/lireLuceneIndex/lireIndexerDec06-c256-vlad-7813.config/CvSURF256"));
        parallelIndexer.addExtractor(CvSiftExtractor.class, new LinkedList<Cluster[]>());
//        parallelIndexer.addExtractor(CEDD.class, SimpleExtractor.KeypointDetector.CVSURF, Cluster.readClusters("/Users/greensod/usptoWork/TrademarkRefiles/data/lireData/lireLuceneIndex/lireIndexerNov29.config/SIMPLEdetCVSURFCEDD2000"));
//        parallelIndexer.addExtractor(CEDD.class, SimpleExtractor.KeypointDetector.CVSIFT, new LinkedList<Cluster[]>());
        
        parallelIndexer.run();
    		
	}
	
}
