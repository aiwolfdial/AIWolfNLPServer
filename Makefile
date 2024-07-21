all: build run

build:
	@javac -d ./out  \
	-classpath './lib/*':./src/ \
	-sourcepath ./src/ \
	-encoding UTF-8 \
	-proc:none \
	./src/starter/AutoGameStarter.java
	@cp ./src/log4j2.xml ./out/

clean:
	@rm -r ./out/*

clean-log:
	@rm -r ./log

run:
	@java -cp './out':'./lib/*' \
	-Dlog4j.configurationFile=./out/log4j2.xml \
	starter.AutoGameStarter