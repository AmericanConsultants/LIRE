package net.semanticmetadata.lire.searchers;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import net.semanticmetadata.lire.aggregators.Aggregator;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;

public class CompositeImageSearcher extends AbstractImageSearcher {
	
//	private BitSamplingImageSearcher globalSearcher;
	private ImageSearcher globalSearcher;
	private GenericFastImageSearcher localSearcher;
	
	
	public CompositeImageSearcher(int maxTotalHits, Class<? extends GlobalFeature> globalFeatureClass,  Class<? extends LocalFeatureExtractor> localFeatureExtractor, Aggregator aggregator, int codebookSize, IndexReader reader, String codebooksDir) throws Exception {
//		globalSearcher = new BitSamplingImageSearcher(1000, globalFeatureClass.newInstance(), 2000);
		globalSearcher = new GenericFastImageSearcher(maxTotalHits, globalFeatureClass, true, reader);
		localSearcher = new GenericFastImageSearcher(maxTotalHits, localFeatureExtractor, aggregator, codebookSize, false, reader, codebooksDir);
	}
	
	public CompositeImageSearcher(int maxTotalHits, ImageSearcher globalSearcher,  Class<? extends LocalFeatureExtractor> localFeatureExtractor, Aggregator aggregator, int codebookSize, IndexReader reader, String codebooksDir) throws Exception {
//		globalSearcher = new BitSamplingImageSearcher(1000, globalFeatureClass.newInstance(), 2000);
		this.globalSearcher = globalSearcher;
		localSearcher = new GenericFastImageSearcher(maxTotalHits, localFeatureExtractor, aggregator, codebookSize, false, reader, codebooksDir);
	}
	

	@Override
	public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
		ImageSearchHits searchHits = globalSearcher.search(image, reader);
//		printSearchHits(searchHits, reader);
		localSearcher.setImagesToSearch(searchHits);
		searchHits = localSearcher.search(image,  reader);
//		printSearchHits(searchHits, reader);
		return searchHits;
	}
	
	private void printSearchHits(ImageSearchHits searchHits, IndexReader reader ) throws IOException{
		for (int i = 0; i < searchHits.length(); i++) {
            String documentId = reader.document(searchHits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(searchHits.documentID(i)+" - "+documentId);
		}
	}

	@Override
	public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
