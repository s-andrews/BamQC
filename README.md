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

Cross-platform Java 6 or 7
=

BamQC is a cross-platform application, written in java.  In theory it
should run on any platform which has a suitable java runtime environment.
Having said that we've only tested in on Windows, MacOSX 10.6 and Linux
running the Sun v1.6 and 1.7 JREs.  Please let us know what happened if
you try running it on other platforms / JREs.


Install
=
Check out of GitHug and using git:

    git clone https://github.com/s-andrews/BamQC.git

and then follow the instructions in the INSTALL.txt file. 

Comments
=

If you have any comments about BamQC we would like to hear them.  You
can either enter them in our bug tracking system at:

http://www.bioinformatics.bbsrc.ac.uk/bugzilla/

..or send them directly to 

simon.andrews at babraham.ac.uk
