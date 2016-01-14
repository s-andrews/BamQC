/**
 * Copyright Copyright 2010-14 Simon Andrews
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
/*
 * Changelog: 
 * - Piero Dalle Pezze: Class creation.
 */
package uk.ac.babraham.BamQC.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import uk.ac.babraham.BamQC.Preferences.BamQCPreferences;
import uk.ac.babraham.BamQC.Network.DownloadableGenomes.DownloadableGenomeSet;
import uk.ac.babraham.BamQC.Network.DownloadableGenomes.GenomeAssembly;
import uk.ac.babraham.BamQC.Network.DownloadableGenomes.GenomeSpecies;



/** 
 * A simple class for listing the genomes available at the Babraham servers.
 * @author Piero Dalle Pezze
 */
public class BamQCListGenomes {
	
	private static Logger log = Logger.getLogger(BamQCListGenomes.class);

	/**
	 * Return the list of genomes available on the Babraham server or null if this list cannot be downloaded.
	 * Each species will be shown as follows: species [assembly1, assembly2, ... assemblyN].
	 * @param regex a regular expression string used as filter. (* is equivalent to unfiltered)
	 * @return the list of available genomes or null
	 */
	public static GenomeSpecies[] listAvailableGenomes(String regex) {
		
		System.out.println("List of genomes (species [ assemblies ]) retrieved from the " 
				 + "Babraham Servers:");
		
		GenomeSpecies[] gs = null;
		
		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = null;
			String[] allSpeciesAssemblies = null;
			
			DownloadableGenomeSet dgs = new DownloadableGenomeSet();		

			gs = dgs.species();
			if(gs.length > 0) {
				allSpeciesAssemblies = new String[gs.length];
				
				// Store all species and assemblies in the array allSpeciesAssemblies
				for(int i=0;i<gs.length;i++) {
					allSpeciesAssemblies[i] = gs[i].name() + " [ ";
					GenomeAssembly[] ga = gs[i].assemblies();
					for(int j=0;j<ga.length;j++) {
						allSpeciesAssemblies[i] += ga[j].assembly();
						if(j < ga.length-1) {
							allSpeciesAssemblies[i] += " | ";
						}
					}
					allSpeciesAssemblies[i] += " ]";
				}
				
				// Now print them using the matcher filter.
				for(int i=0; i<allSpeciesAssemblies.length; i++) {
					matcher = pattern.matcher(allSpeciesAssemblies[i]);
					if(matcher.find()) {
						System.out.println(allSpeciesAssemblies[i]);
					}
				}
				
			} else {
				System.out.println("Something went wrong. No species was retrieved from the " 
								 + "Babraham Servers. Is your Internet connection working?");
			}
			
		} catch (IOException e) {
			log.error(e, e);
		} catch (PatternSyntaxException e) {
			log.error("The regular expression " + regex + " is not valid.", e);
			System.out.println("The regular expression " + regex + " is not valid.");
		}
		return gs;
	}

	
	
	
	/**
	 * Return the list of downloaded genomes or null if this is empty
	 * @return the list of downloaded genomes or null
	 */
	public static File[] listSavedGenomes() {
		File[] genomes = null;
		try {
			genomes = BamQCPreferences.getInstance().getGenomeBase().listFiles();
			if (genomes == null) {
				throw new FileNotFoundException();
			}
		} 
		catch (FileNotFoundException e) {
			System.out.println("Could not find the folder containing your genomes. "
					         + "Please check your file preferences.");
			return genomes;
		}
		System.out.println("Downloaded genomes:");
		for(int i=0; i<genomes.length; i++) {
			System.out.println(genomes[i]);
		}
		return genomes;
	}
	
	
	
	
	
	/**
	 * Converts a standard POSIX Shell globbing pattern into a regular expression
	 * pattern. The result can be used with the standard {@link java.util.regex} API to
	 * recognize strings which match the glob pattern.
	 * <p/>
	 * See also, the POSIX Shell language:
	 * http://pubs.opengroup.org/onlinepubs/009695399/utilities/xcu_chap02.html#tag_02_13_01
	 * 
	 * @author Neil Traft (http://stackoverflow.com/questions/1247772/is-there-an-equivalent-of-java-util-regex-for-glob-type-patterns)
	 * @author Piero Dalle Pezze (small edits at the end).
	 * 
	 * @param pattern A glob pattern.
	 * @param matchWholeString true if the whole string is matched, false if only a substring is matched.
	 * @return A regex pattern to recognize the given glob pattern.
	 */
	public static final String convertGlobToRegex(String pattern, boolean matchWholeString) {
	    StringBuilder sb = new StringBuilder(pattern.length());
	    int inGroup = 0;
	    int inClass = 0;
	    int firstIndexInClass = -1;
	    char[] arr = pattern.toCharArray();
	    for (int i = 0; i < arr.length; i++) {
	        char ch = arr[i];
	        switch (ch) {
	            case '\\':
	                if (++i >= arr.length) {
	                    sb.append('\\');
	                } else {
	                    char next = arr[i];
	                    switch (next) {
	                        case ',':
	                            // escape not needed
	                            break;
	                        case 'Q':
	                        case 'E':
	                            // extra escape needed
	                            sb.append('\\');
	                        default:
	                            sb.append('\\');
	                    }
	                    sb.append(next);
	                }
	                break;
	            case '*':
	                if (inClass == 0)
	                    sb.append(".*");
	                else
	                    sb.append('*');
	                break;
	            case '?':
	                if (inClass == 0)
	                    sb.append('.');
	                else
	                    sb.append('?');
	                break;
	            case '[':
	                inClass++;
	                firstIndexInClass = i+1;
	                sb.append('[');
	                break;
	            case ']':
	                inClass--;
	                sb.append(']');
	                break;
	            case '.':
	            case '(':
	            case ')':
	            case '+':
	            case '|':
	            case '^':
	            case '$':
	            case '@':
	            case '%':
	                if (inClass == 0 || (firstIndexInClass == i && ch == '^'))
	                    sb.append('\\');
	                sb.append(ch);
	                break;
	            case '!':
	                if (firstIndexInClass == i)
	                    sb.append('^');
	                else
	                    sb.append('!');
	                break;
	            case '{':
	                inGroup++;
	                sb.append('(');
	                break;
	            case '}':
	                inGroup--;
	                sb.append(')');
	                break;
	            case ',':
	                if (inGroup > 0)
	                    sb.append('|');
	                else
	                    sb.append(',');
	                break;
	            default:
	                sb.append(ch);
	        }
	    }
	    
	    if(matchWholeString) {
	    	return "^" + sb.toString() + "$";
	    }
	    return sb.toString();
	}
	

	public static void main(String[] args) {
		
	    Properties properties = System.getProperties();
	    
	    if(properties.getProperty("bamqc.saved_genomes") != null && 
	       properties.getProperty("bamqc.saved_genomes").equals("true")) {
			BamQCListGenomes.listSavedGenomes();	
	    
	    } else if(properties.getProperty("bamqc.available_genomes") != null && 
	    		  properties.getProperty("bamqc.available_genomes").equals("true")) {
	    	
	    	// If it is preferred to match the whole string, then set matchWholeString to true.
			boolean matchWholeString = false;
			String pattern = "*", regex = "";
			if(properties.getProperty("bamqc.genome_pattern") != null && 
	    	   !properties.getProperty("bamqc.genome_pattern").equals("")) {
				pattern = properties.getProperty("bamqc.genome_pattern");
			}			
			regex = convertGlobToRegex(pattern, matchWholeString);
			BamQCListGenomes.listAvailableGenomes(regex);			
	    
	    } else {
			System.out.println("Please, use the option '--saved-genomes' or '--available-genomes'");
	    }	    
	    
	}
	
}
