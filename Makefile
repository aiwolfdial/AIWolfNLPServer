all: build run

optimized: optimized-build optimized-run

build:
	@javac -d ./out  \
	-classpath './lib/*':./src/ \
	-sourcepath ./src/ \
	-encoding UTF-8 \
	-proc:none \
	./src/launcher/Launcher.java

optimized-build:
	@javac -d ./out  \
	-classpath './lib/*':./src/ \
	-sourcepath ./src/ \
	-encoding UTF-8 \
	-proc:none \
	-O \
	./src/launcher/OptimizedLauncher.java

clean:
	@rm -r ./out/*

clean-log:
	@rm -r ./log

run:
	@java -cp './out':'./lib/*' \
	-Dlog4j.configurationFile=./config/log4j2.xml \
	launcher.Launcher

optimized-run:
	@java -cp './out':'./lib/*' \
	-Dlog4j.configurationFile=./config/log4j2.xml \
	launcher.OptimizedLauncher