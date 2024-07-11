build:
	@make build-gui
	@make build-game
	@make build-client
	@make build-common
	@make build-server
	@make build-starter
	@make build-automatic

clean:
	@make clean-gui
	@make clean-game
	@make clean-client
	@make clean-common
	@make clean-server
	@make clean-starter
	@make clean-automatic

build-gui:
	@javac -d ./out ./src/gui/*.java \
	-classpath './lib/*':./src/
	@chmod 775 ./out/gui/*.class

clean-gui:
	@rm -r ./out/gui

build-game:
	@javac -d ./out ./src/game/*.java \
	-classpath './lib/*':./src/
	@chmod 775 ./out/game/*.class

clean-game:
	@rm -r ./out/game

build-client:
	@javac -d ./out ./src/client/*.java \
	-classpath './lib/*':./src/
	@chmod 775 ./out/client/*.class

clean-client:
	@rm -r ./out/client

build-common:
	@javac -d ./out ./src/common/*.java \
	-classpath './lib/*':./src/
	@chmod 775 ./out/common/*.class

clean-common:
	@rm -r ./out/common

build-server:
	@javac -d ./out ./src/server/*.java \
	-classpath './lib/*':./src/
	@chmod 775 ./out/server/*.class

clean-server:
	@rm -r ./out/server

build-starter:
	@javac -d ./out ./src/starter/*.java \
	-classpath './lib/*':./src/ \
	-Xlint:none
	@chmod 775 ./out/starter/*.class

clean-starter:
	@rm -r ./out/starter

build-automatic:
	@javac -d ./out ./src/automatic/*.java \
	-classpath './lib/*':./src/ \
	-Xlint:none
	@chmod 775 ./out/automatic/*.class

clean-automatic:
	@rm -r ./out/automatic

clean-log:
	@rm -r ./log

run:
	java --class-path ./out/:'./lib/*' automatic/AutoGameStarter
