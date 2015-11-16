BamQC
=====

BamQC is an application which takes a BAM file containing mapped
data and runs a series of tests on it to generate a comprehensive 
QC report.  This will help you to understand your data and will 
tell you if there is anything unusual the files you have analysed.
Each test is flagged as a pass, warning or fail depending on how 
far it departs from what you'd expect from a normal large dataset
with no significant biases.  It's important to stress that warnings 
or even failures do not necessarily mean that there is a problem 
with your data, only that it is unusual.  It is possible that the 
biological nature of your sample means that you would expect this 
particular bias in your results.


Interactive Graphical or Command Line
=

BamQC can be run either as an interactive graphical application 
which allows you to view results for multiple files in a single
application.  Alternatively you can run the program in a non
interactive way (say as part of a pipeline) which will generate
an HTML report for each file you process.


Cross-platform Java
=

BamQC is a cross-platform application, written in java.  In theory it
should run on any platform which has a suitable java runtime environment.
It's been tested on Windows, MacOSX 10.6 and Linux running java v1.6, 1.8 and 1.8.


Download
=
Check out of GitHub and using git:

    git clone https://github.com/s-andrews/BamQC.git


Compile
=
To compile the source code of BamQC, JDK 1.6+ and the package Ant are required. 
Ant can be downloaded at the web site https://ant.apache.org/ . 
Once Ant is installed, BamQC can be easily built using the following commands: 

	# Move to the folder of BamQC
	cd Path/To/BamQC/SourceCode/
	
	# Type the command "ant". This will automatically process 
	# the BamQC file build.xml 
	ant
	
	# Move to the bin folder which was created in the previous step.
	cd bin
	
	# Change permission to the file bamqc in order to execute it
	chmod 755 bamqc
	
	# Test 
	./bamqc

Install
=

Further instructions for installing and running the program can be found in the INSTALL.txt
file in the BamQC distribution.


Comments
=

If you have any comments about BamQC we would like to hear them.  You
can either enter them as a new issue on github.

Or email them directly to:

simon.andrews at babraham.ac.uk
