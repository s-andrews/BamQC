/**
 * Copyright 2010-15 Simon Andrews
 *
 *    This file is part of BamQC.
 *
 *    BamQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    BamQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with BamQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package uk.ac.babraham.BamQC.DataTypes;

import uk.ac.babraham.BamQC.BamQCException;
import uk.ac.babraham.BamQC.DataTypes.Genome.Chromosome;
import uk.ac.babraham.BamQC.DataTypes.Probes.Probe;
import uk.ac.babraham.BamQC.DataTypes.Sequence.HiCHitCollection;
import uk.ac.babraham.BamQC.Utilities.LongSorter.LongSetSorter;


/**
 * A replicate set is a way to group together data stores
 * which are biological replicates of each other.  Unlike
 * DataStores replicate sets do not store their own
 * quantitations but simply aggregate the distribution of
 * quantitated values from their component members.
 * 
 */
public class ReplicateSet extends DataStore implements HiCDataStore {

	/** The data stores. */
	private DataStore [] dataStores;

	
	/**
	 * Instantiates a new replicate set.
	 * 
	 * @param name the name
	 * @param dataStores the data stores
	 */
	public ReplicateSet (String name, DataStore [] dataStores) {
		super(name);
		this.dataStores = dataStores;
	}
		
	/**
	 * Data stores.
	 * 
	 * @return the data store[]
	 */
	public DataStore [] dataStores () {
		return dataStores;
	}
	
	
	/**
	 * Sets the data sets.
	 * 
	 * @param sets the new data sets
	 */
	public void setDataStores (DataStore [] stores) {
		dataStores = stores;
		if (collection() != null) {
			collection().replicateSetStoresChanged(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.DataStore#setName(java.lang.String)
	 */
	@Override
	public void setName (String name) {
		super.setName(name);
		if (collection() != null) {
			collection().replicateSetRenamed(this);
		}
	}

	
	/**
	 * Contains data store.
	 * 
	 * @param s the s
	 * @return true, if successful
	 */
	public boolean containsDataStore (DataStore s) {
		for (int i=0;i<dataStores.length;i++) {
			if (dataStores[i]==s)
				return true;
		}
		return false;
	}
	
	/**
	 * Removes a data store.
	 * 
	 * @param s the s
	 */
	public void removeDataStore (DataStore s) {
		if (! containsDataStore(s)) return;
		
		DataStore [] newSet = new DataStore[dataStores.length-1];
		int j=0;
		for (int i=0;i<dataStores.length;i++) {
			if (dataStores[i] == s) continue;
			newSet[j] = dataStores[i];
			j++;
		}
		
		dataStores = newSet;
		
		if (collection() != null) {
			collection().replicateSetStoresChanged(this);
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.DataStore#getReadCountForChromosome(uk.ac.babraham.BamQC.DataTypes.Genome.Chromosome)
	 */
	@Override
	public int getReadCountForChromosome(Chromosome c) {
		int count = 0;
		for (int i=0;i<dataStores.length;i++) {
			count += dataStores[i].getReadCountForChromosome(c);
		}
		return count;
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.DataStore#getReadsForChromsome(uk.ac.babraham.BamQC.DataTypes.Genome.Chromosome)
	 */
	@Override
	public long[] getReadsForChromosome(Chromosome c) {
		long [][] readsFromAllChrs = new long[dataStores.length][];
		
		for (int i=0;i<dataStores.length;i++) {
			readsFromAllChrs[i] = dataStores[i].getReadsForChromosome(c);
		}
		
		return LongSetSorter.sortLongSets(readsFromAllChrs);
	}

	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.DataStore#getTotalReadCount()
	 */
	@Override
	public int getTotalReadCount() {
		int count = 0;
		for (int i=0;i<dataStores.length;i++) {
			count += dataStores[i].getTotalReadCount();
		}
		return count;
	}
	
	@Override
	public int getTotalPairCount () {
		return getTotalReadCount()/2;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.DataStore#getReadCountForStrand()
	 */
	@Override
	public int getReadCountForStrand(int strand) {
		int count = 0;
		for (int i=0;i<dataStores.length;i++) {
			count += dataStores[i].getReadCountForStrand(strand);
		}
		return count;
	}


	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.DataStore#getTotalReadLength()
	 */
	@Override
	public long getTotalReadLength() {
		long count = 0;
		for (int i=0;i<dataStores.length;i++) {
			count += dataStores[i].getTotalReadLength();
		}
		return count;
	}
	
	@Override
	public int getMaxReadLength() {

		int max = 0;
		for (int i=0;i<dataStores.length;i++) {
			if (i==0 || dataStores[i].getMaxReadLength() > max) max = dataStores[i].getMaxReadLength();
		}

		return max;
	}

	@Override
	public int getMinReadLength() {
		int min = 0;
		for (int i=0;i<dataStores.length;i++) {
			if (i==0 || dataStores[i].getMinReadLength() < min) min = dataStores[i].getMinReadLength();
		}

		return min;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.babraham.BamQC.DataTypes.DataStore#getReadsForProbe(uk.ac.babraham.BamQC.DataTypes.Probes.Probe)
	 */
	@Override
	public long[] getReadsForProbe(Probe p) {
		long [][] returnReads = new long [dataStores.length][];
		for (int i=0;i<dataStores.length;i++) {
			returnReads[i] = dataStores[i].getReadsForProbe(p);
		}
		return LongSetSorter.sortLongSets(returnReads);
	}
	
	
	/**
	 * Checks if is quantitated.  Only true if all of the stores
	 * in this set are quantitated.
	 * 
	 * @return true, if is quantitated
	 */
	@Override
	public boolean isQuantitated () {
		
		if (dataStores.length == 0) return false;
		
		for (int i=0;i<dataStores.length;i++) {
			if (!dataStores[i].isQuantitated()) {
				return false;
			}
		}
		
		return true;	
	}
	
	/**
	 * Sets the value for probe.  You can't do this to a replicate set
	 * since it doesn't store probe values, so this will always throw
	 * an error.
	 * 
	 * @param p the p
	 * @param f the f
	 */
	@Override
	public void setValueForProbe (Probe p, float f) {
		throw new IllegalArgumentException("You can't set probe values for a replicate set");
	}
	
	/**
	 * Checks whether we have a value for this probe.  This is only
	 * true if all of the data stores in this replicate set have a
	 * value for this probe
	 * 
	 * @param p the p
	 * @return true, if successful
	 */
	@Override
	public boolean hasValueForProbe (Probe p) {

		for (int i=0;i<dataStores.length;i++) {
			if (!dataStores[i].hasValueForProbe(p)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Gets the mean value for this probe across all of the
	 * data stores underlying this set.
	 * 
	 * @param p the p
	 * @return the mean value for probe
	 * @throws BamQCException the seq monk exception
	 */
	@Override
	public float getValueForProbe(Probe p) throws BamQCException {
		
		if (! hasValueForProbe(p)) {
			throw new BamQCException("No quantitation for probe "+p+" in "+name());			
		}
		
		if (dataStores.length == 0) {
			return 0;
		}

		float total = 0;
		for (int i=0;i<dataStores.length;i++) {
			total += dataStores[i].getValueForProbe(p);
		}
		
		return total/dataStores.length;
		
	}

	@Override
	public boolean isValidHiC() {
		if (dataStores.length == 0) return false;
		
		for (int i=0;i<dataStores.length;i++) {
			if (! (dataStores[i] instanceof HiCDataStore  && ((HiCDataStore)dataStores[i]).isValidHiC())) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public HiCHitCollection getHiCReadsForProbe(Probe p) {
		
		HiCHitCollection collection = new HiCHitCollection(p.chromosome().name());
		for (int i=0;i<dataStores.length;i++) {
			if (dataStores[i] instanceof HiCDataStore) {
				HiCHitCollection thisCollection = ((HiCDataStore)dataStores[i]).getHiCReadsForProbe(p);
				collection.addCollection(thisCollection);				
			}
		}
		
		collection.sortCollection();
		
		return collection;	
	}

	@Override
	public HiCHitCollection getHiCReadsForChromosome(Chromosome c) {
		HiCHitCollection collection = new HiCHitCollection(c.name());
		for (int i=0;i<dataStores.length;i++) {
			if (dataStores[i] instanceof HiCDataStore) {
				HiCHitCollection thisCollection = ((HiCDataStore)dataStores[i]).getHiCReadsForChromosome(c);
				collection.addCollection(thisCollection);				
			}
		}
		
		collection.sortCollection();
		
		return collection;	
	}

	@Override
	public HiCHitCollection getExportableReadsForChromosome(Chromosome c) {
		HiCHitCollection collection = new HiCHitCollection(c.name());
		for (int i=0;i<dataStores.length;i++) {
			if (dataStores[i] instanceof HiCDataStore) {
				HiCHitCollection thisCollection = ((HiCDataStore)dataStores[i]).getExportableReadsForChromosome(c);
				collection.addCollection(thisCollection);				
			}
		}
		
		collection.sortCollection();
		
		return collection;	
	}

	
	@Override
	public float getCorrectionForLength(Chromosome c, int minDist, int maxDist) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int getCisCountForChromosome(Chromosome c) {
		int total = 0;
		for (int i=0;i<dataStores.length;i++) {
			if (dataStores[i] instanceof HiCDataStore) {
				total += ((HiCDataStore)dataStores[i]).getCisCountForChromosome(c);
			}
		}
		return total;
	}

	@Override
	public int getTransCountForChromosome(Chromosome c) {
		int total = 0;
		for (int i=0;i<dataStores.length;i++) {
			if (dataStores[i] instanceof HiCDataStore) {
				total += ((HiCDataStore)dataStores[i]).getTransCountForChromosome(c);
			}
		}
		return total;
	}

	@Override
	public int getCisCount() {
		int total = 0;
		for (int i=0;i<dataStores.length;i++) {
			if (dataStores[i] instanceof HiCDataStore) {
				total += ((HiCDataStore)dataStores[i]).getCisCount();
			}
		}
		return total;
	}

	@Override
	public int getTransCount() {
		int total = 0;
		for (int i=0;i<dataStores.length;i++) {
			if (dataStores[i] instanceof HiCDataStore) {
				total += ((HiCDataStore)dataStores[i]).getTransCount();
			}
		}
		return total;
	}
	
	@Override
	public int getHiCReadCountForProbe(Probe p) {
		int total = 0;
		for (int i=0;i<dataStores.length;i++) {
			if (dataStores[i] instanceof HiCDataStore) {
				total += ((HiCDataStore)dataStores[i]).getHiCReadCountForProbe(p);
			}
		}
		return total;
	}

	@Override
	public int getHiCReadCountForChromosome(Chromosome c) {
		int total = 0;
		for (int i=0;i<dataStores.length;i++) {
			if (dataStores[i] instanceof HiCDataStore) {
				total += ((HiCDataStore)dataStores[i]).getHiCReadCountForChromosome(c);
			}
		}
		return total;
	}

	
}
