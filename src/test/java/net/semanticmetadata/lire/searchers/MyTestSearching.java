package net.semanticmetadata.lire.searchers;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;

import junit.framework.TestCase;
import net.semanticmetadata.lire.aggregators.AbstractAggregator;
import net.semanticmetadata.lire.aggregators.BOVW;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.classifiers.Cluster;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.EdgeHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.JCD;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSiftExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;

public class MyTestSearching  extends TestCase {
	
    Class<? extends GlobalFeature> globalFeatureClass = CEDD.class;
    Class<? extends LocalFeatureExtractor> localFeatureClass = CvSurfExtractor.class;
    SimpleExtractor.KeypointDetector keypointDetector = SimpleExtractor.KeypointDetector.CVSURF;
    Class<? extends AbstractAggregator> aggregatorClass = BOVW.class;
    String codebookPath = "./src/test/resources/codebooks/";
    String imageToSearch = "/Users/greensod/usptoWork/TrademarkRefiles/data/tsdrImages/Jan-2011/85230163.jpg";

    private final String indexPath = "/Users/greensod/usptoWork/TrademarkRefiles/data/lireData/lireLuceneIndex/testIndex";
//    private final String indexPathSeparate = "test-separate";
    private final String testExtensive = "/Users/greensod/usptoWork/TrademarkRefiles/data/tsdrImages/Jan-2011";

    private int numOfDocsForVocabulary = 500;
    private int numOfClusters = 256;

    public void testIndexing() throws IOException, IllegalAccessException, InstantiationException {
        Cluster[] cvsurf512 = Cluster.readClusters(codebookPath + "CvSURF512");
        Cluster[] simpleceddcvsurf512 = Cluster.readClusters(codebookPath + "SIMPLEdetCVSURFCEDD512");    	
    	
        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive, numOfClusters, numOfDocsForVocabulary, aggregatorClass);
        parallelIndexer.addExtractor(globalFeatureClass);
        parallelIndexer.addExtractor(localFeatureClass, cvsurf512);
        parallelIndexer.addExtractor(globalFeatureClass, keypointDetector, simpleceddcvsurf512);
        parallelIndexer.run();
    	
        BufferedImage image = ImageIO.read(new FileInputStream(imageToSearch));

        IndexReader reader = DirectoryReader.open(new RAMDirectory(FSDirectory.open(Paths.get(indexPath)), IOContext.READONCE));
        System.out.println("Documents in the reader: " + reader.maxDoc());

        GenericFastImageSearcher ceddSearcher = new GenericFastImageSearcher(10, globalFeatureClass, true, reader);
        ImageSearchHits ceddhits = ceddSearcher.search(image, reader);
        String hitFile;
        for (int y = 0; y < ceddhits.length(); y++) {
            hitFile = reader.document(ceddhits.documentID(y)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            hitFile = hitFile.substring(hitFile.lastIndexOf('\\') + 1);
            System.out.println(y + ". " + hitFile + " " + ceddhits.score(y));
        }
        System.out.println();
    }
    
    public void testIndexingAll() throws IOException, IllegalAccessException, InstantiationException {
//        Cluster[] cvsurf512 = Cluster.readClusters(codebookPath + "CvSURF512");
//        Cluster[] simpleceddcvsurf512 = Cluster.readClusters(codebookPath + "SIMPLEdetCVSURFCEDD512");    	
    	
    	String indexPath = "/Users/greensod/usptoWork/TrademarkRefiles/data/lireData/lireLuceneIndex/testIndexAll";
        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive, 256, 500, aggregatorClass);
        parallelIndexer.addExtractor(CEDD.class);
        parallelIndexer.addExtractor(JCD.class);
        parallelIndexer.addExtractor(EdgeHistogram.class);
        parallelIndexer.addExtractor(CvSurfExtractor.class, new LinkedList<Cluster[]>());
        parallelIndexer.addExtractor(CvSiftExtractor.class, new LinkedList<Cluster[]>());
        parallelIndexer.addExtractor(CEDD.class, SimpleExtractor.KeypointDetector.CVSURF, new LinkedList<Cluster[]>());
        parallelIndexer.addExtractor(CEDD.class, SimpleExtractor.KeypointDetector.CVSIFT, new LinkedList<Cluster[]>());
        
        parallelIndexer.run();    	
    }    
    
    public void testSearch() throws IOException, IllegalAccessException, InstantiationException {
        BufferedImage image = ImageIO.read(new FileInputStream(imageToSearch));

        IndexReader reader = DirectoryReader.open(new RAMDirectory(FSDirectory.open(Paths.get(indexPath)), IOContext.READONCE));
        System.out.println("Documents in the reader: " + reader.maxDoc());

        GenericFastImageSearcher ceddSearcher = new GenericFastImageSearcher(10, globalFeatureClass, true, reader);
        ImageSearchHits ceddhits = ceddSearcher.search(image, reader);
        String hitFile;
        for (int y = 0; y < ceddhits.length(); y++) {
            hitFile = reader.document(ceddhits.documentID(y)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            hitFile = hitFile.substring(hitFile.lastIndexOf('\\') + 1);
            System.out.println(y + ". " + hitFile + " " + ceddhits.score(y));
        }
        System.out.println();

        GenericFastImageSearcher cvsurfsearcher = new GenericFastImageSearcher(10, localFeatureClass, aggregatorClass.newInstance(), 256, true, reader, indexPath + ".config");
        ImageSearchHits cvsurfhits = cvsurfsearcher.search(image, reader);
        for (int y = 0; y < cvsurfhits.length(); y++) {
            hitFile = reader.document(cvsurfhits.documentID(y)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            hitFile = hitFile.substring(hitFile.lastIndexOf('\\') + 1);
            System.out.println(y + ". " + hitFile + " " + cvsurfhits.score(y));
        }
        System.out.println();

        GenericFastImageSearcher simpleceddcvsurfsearcher = new GenericFastImageSearcher(10, globalFeatureClass, keypointDetector, aggregatorClass.newInstance(), 256, true, reader, indexPath + ".config");
        ImageSearchHits simpleceddcvsurfhits = simpleceddcvsurfsearcher.search(image, reader);
        for (int y = 0; y < simpleceddcvsurfhits.length(); y++) {
            hitFile = reader.document(simpleceddcvsurfhits.documentID(y)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            hitFile = hitFile.substring(hitFile.lastIndexOf('\\') + 1);
            System.out.println(y + ". " + hitFile + " " + simpleceddcvsurfhits.score(y));
        }
        System.out.println();
    	
    }

}
