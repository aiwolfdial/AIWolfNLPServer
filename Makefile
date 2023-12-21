build:
	@make build-gui
	@make build-game
	@make build-client
	@make build-common
	@make build-server
	@make build-starter
	@make build-automatic

clear clean:
	@make clear-gui
	@make clear-game
	@make clear-client
	@make clear-common
	@make clear-server
	@make clear-starter
	@make clear-automatic

build-gui:
	@javac ./src/net/kanolab/aiwolf/server/gui/*.java \
	-classpath './lib/*':./src/
	@chmod 775 ./src/net/kanolab/aiwolf/server/gui/*.class

clear-gui clean-gui:
	@rm ./src/net/kanolab/aiwolf/server/gui/*.class

build-game:
	@javac ./src/net/kanolab/aiwolf/server/game/*.java \
	-classpath './lib/*':./src/
	@chmod 775 ./src/net/kanolab/aiwolf/server/game/*.class

clear-game clean-game:
	@rm ./src/net/kanolab/aiwolf/server/game/*.class

build-client:
	@javac ./src/net/kanolab/aiwolf/server/client/*.java \
	-classpath './lib/*':./src/
	@chmod 775 ./src/net/kanolab/aiwolf/server/client/*.class

clear-client clean-client:
	@rm  ./src/net/kanolab/aiwolf/server/client/*.class

build-common:
	@javac ./src/net/kanolab/aiwolf/server/common/*.java \
	-classpath './lib/*':./src/
	@chmod 775 ./src/net/kanolab/aiwolf/server/common/*.class

clear-common clean-common:
	@rm  ./src/net/kanolab/aiwolf/server/common/*.class

# (common,gui)
build-server:
	@javac ./src/net/kanolab/aiwolf/server/server/*.java \
	-classpath './lib/*':./src/
	@chmod 775 ./src/net/kanolab/aiwolf/server/server/*.class

clear-server clean-server:
	@rm ./src/net/kanolab/aiwolf/server/server/*.class

# (server,common,client)
build-starter:
	@javac ./src/net/kanolab/aiwolf/server/starter/*.java \
	-classpath './lib/*':./src/ \
	-Xlint:none
	@chmod 775 ./src/net/kanolab/aiwolf/server/starter/*.class

clear-starter clean-starter:
	@rm ./src/net/kanolab/aiwolf/server/starter/*.class

# (starter,client,aiwolf.agent)
build-automatic:
	@javac ./src/net/kanolab/aiwolf/server/automatic/*.java \
	-classpath './lib/*':./src/ \
	-Xlint:none
	@chmod 775 ./src/net/kanolab/aiwolf/server/automatic/*.class

clear-automatic clean-automatic:
	@rm ./src/net/kanolab/aiwolf/server/automatic/*.class

clear-log clean-log delete-log:
	@rm ./log/*.log

run:
	java --class-path ./src/:'./lib/*' net/kanolab/aiwolf/server/automatic/AutoGameStarter