all: build run

build:
	@javac -d ./out  \
	-classpath './lib/*':./src/ \
	-sourcepath ./src/ \
	-encoding UTF-8 \
	-proc:none \
	./src/starter/AutoGameStarter.java

clean:
	@rm -r ./out/*

clean-log:
	@rm -r ./log

run:
	@java -cp './out':'./lib/*' \
	-Dlog4j.configurationFile=./config/log4j2.xml \
	starter.AutoGameStarter