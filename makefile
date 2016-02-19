default:
	javac Server.java Client.java 

clean:
	$(RM) *.class

run:
	java Main
