/**
 * Copyright Copyright 2014 Simon Andrews
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
package uk.ac.babraham.BamQC.Modules;

import java.util.ArrayList;

public class ModuleFactory {

	public static QCModule [] getStandardModuleList () {

		// If a module is going to be skipped, we do not even compute it. 
		// It is safer not to invoke the method ignoreInReport() as this also checks 
		// the module data fields and can potentially throw null pointer exception if badly tested. 
		// Let's explicitly check the configuration instead.
		ArrayList<QCModule> selectedModules = new ArrayList<QCModule>();
		
		// The module VariantCallDetection is specific as its information is used by other modules and IS naturally ignored in the Report
		// We test this here: 
		VariantCallDetection variantCallDetection = null;
		if(ModuleConfig.getParam("VariantCallDetection", "ignore") == 0) {
			variantCallDetection = new VariantCallDetection();
			selectedModules.add(variantCallDetection);
		}
		
		// Now we check this for all the remaining modules
		// We always compute the module BasicStatistics
		selectedModules.add(new BasicStatistics(variantCallDetection));

		if(ModuleConfig.getParam("GenomeCoverage", "ignore") == 0) {
			selectedModules.add(new GenomeCoverage());
		}
		if(ModuleConfig.getParam("ChromosomeReadDensity", "ignore") == 0) {
			selectedModules.add(new ChromosomeReadDensity());
		}
		if(ModuleConfig.getParam("FeatureCoverage", "ignore") == 0) {
			selectedModules.add(new FeatureCoverage());
		}
		// TODO: this could also reuse varianCallDetection
		if(ModuleConfig.getParam("SoftClipDistribution", "ignore") == 0) {
			selectedModules.add(new SoftClipDistribution());
		}
		if(ModuleConfig.getParam("IndelFrequencies", "ignore") == 0) {
			selectedModules.add(new IndelFrequencies(variantCallDetection));
		}
		if(ModuleConfig.getParam("SNPFrequencies", "ignore") == 0) {
			selectedModules.add(new SNPFrequencies(variantCallDetection));
		}
		if(ModuleConfig.getParam("SNPFrequenciesByType", "ignore") == 0) {
			selectedModules.add(new SNPFrequenciesByType(variantCallDetection));
		}
		if(ModuleConfig.getParam("SequenceQualityDistribution", "ignore") == 0) {
			selectedModules.add(new SequenceQualityDistribution());
		}
		if(ModuleConfig.getParam("MappingQualityDistribution", "ignore") == 0) {
			selectedModules.add(new MappingQualityDistribution());
		}
		if(ModuleConfig.getParam("InsertLengthDistribution", "ignore") == 0) {
			selectedModules.add(new InsertLengthDistribution());
		}
		// TODO: this module is not ready yet
//		if(ModuleConfig.getParam("RpkmReference", "ignore") == 0) {
//			selectedModules.add(new RpkmReference());
//		}
		
		return selectedModules.toArray(new QCModule[0]);
	}
	
}
