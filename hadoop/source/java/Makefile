HADOOP = ${HOME}/hadoop-2.2.0/bin/hadoop
APP = FoF
SRC = FoF.java 
 
$(APP): $(SRC) 
	javac -classpath `$(HADOOP) classpath` $(SRC) 
	jar -cf $(APP).jar $(APP)*.class
 
clean: 
	rm -rf *.jar *.class