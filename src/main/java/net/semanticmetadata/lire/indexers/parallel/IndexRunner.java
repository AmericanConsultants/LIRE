package net.semanticmetadata.lire.indexers.parallel;

import java.io.File;
import java.util.LinkedList;

import net.semanticmetadata.lire.aggregators.BOVW;
import net.semanticmetadata.lire.classifiers.Cluster;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.EdgeHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.JCD;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSiftExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;

public class IndexRunner {

	public static void main(String[] args) {

        String indexPath = null;
        String imageDirectory = null;
        File imageList = null;
        int numThreads = 10;
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
        int numOfClusters = 512;
        int numOfDocsForVocabulary = 1000;         		
        if (imageList != null) {
        	parallelIndexer = new ParallelIndexer(numThreads, indexPath, imageList, numOfClusters, numOfDocsForVocabulary, BOVW.class);
        } else {
        	parallelIndexer = new ParallelIndexer(numThreads, indexPath, imageDirectory, numOfClusters, numOfDocsForVocabulary, BOVW.class);
        }
//        p.addExtractor(ACCID.class);
        parallelIndexer.addExtractor(CEDD.class);
        parallelIndexer.addExtractor(JCD.class);
        parallelIndexer.addExtractor(EdgeHistogram.class);
        parallelIndexer.addExtractor(CvSurfExtractor.class, new LinkedList<Cluster[]>());
        parallelIndexer.addExtractor(CvSiftExtractor.class, new LinkedList<Cluster[]>());
        parallelIndexer.addExtractor(CEDD.class, SimpleExtractor.KeypointDetector.CVSURF, new LinkedList<Cluster[]>());
        parallelIndexer.addExtractor(CEDD.class, SimpleExtractor.KeypointDetector.CVSIFT, new LinkedList<Cluster[]>());
        
        parallelIndexer.run();
    		
	}
	
}