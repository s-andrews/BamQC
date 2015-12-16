/*
 * Changelog: 
 * - Piero Dalle Pezze: Moved in a separate class. 
 * - Simon Andrews: Class creation.
 */
package uk.ac.babraham.BamQC.AnnotationParsers;

import java.util.ArrayList;

import uk.ac.babraham.BamQC.DataTypes.Genome.Feature;
import uk.ac.babraham.BamQC.DataTypes.Genome.Location;
import uk.ac.babraham.BamQC.DataTypes.Genome.SplitLocation;

/**
 * The Class featureGroup.
 * @author Simon Andrews
 * @author Piero Dalle Pezze
 */
public class FeatureGroup {

	/** The feature. */
	protected Feature feature;

	/** The sub locations. */
	protected ArrayList<Location> subLocations = new ArrayList<Location>();
	
	/** The location */
	protected Location location = null;

	/**
	 * Instantiates a new feature group.
	 * 
	 * @param feature the feature
	 * @param strand the strand
	 * @param location the location
	 */
	public FeatureGroup (Feature feature) {
		this.feature = feature;
	}

	/**
	 * Adds a sublocation.
	 * 
	 * @param location the location
	 */
	public void addSublocation (Location location) {
		subLocations.add(location);
	}

	/**
	 * Feature.
	 * 
	 * @return the feature
	 */
	public Feature getFeature () {
			if (subLocations.size() == 0) {
				feature.setLocation(location);					
			}
			else if (subLocations.size() == 1) {
				feature.setLocation(subLocations.get(0));					
			}
			else {
				feature.setLocation(new SplitLocation(subLocations.toArray(new Location[0])));
			}
		return feature;
	}


}
