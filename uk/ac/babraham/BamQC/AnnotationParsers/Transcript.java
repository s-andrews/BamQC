package uk.ac.babraham.BamQC.AnnotationParsers;


import uk.ac.babraham.BamQC.DataTypes.Genome.Feature;


/**
 * The Class Transcript.
 */
public class Transcript extends FeatureGroup {

	private int startCodon;
	private int stopCodon;

	/**
	 * Instantiates a new feature group.
	 * 
	 * @param feature the feature
	 * @param strand the strand
	 * @param location the location
	 */
	public Transcript (Feature feature) {
		super(feature);
	}

	public void addStartCodon (int startCodon) {
		this.startCodon = startCodon;
	}

	public void addStopCodon (int stopCodon) {
		this.stopCodon = stopCodon;
	}

}