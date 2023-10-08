MAKEFILE_DIR := $(dir $(realpath $(firstword $(MAKEFILE_LIST))))
# 一つ上のディレクトリ
PARENT_DIR := $(shell dirname ${MAKEFILE_DIR})

build:
	@make build-gui
	@make build-game
	@make build-client
	@make build-common
	@make build-server
	@make build-starter
	@make build-automatic

clear:
	@make clear-gui
	@make clear-game
	@make clear-client
	@make clear-common
	@make clear-server
	@make clear-starter
	@make clear-automatic

build-gui:
	@javac $(MAKEFILE_DIR)net/kanolab/aiwolf/server/gui/*.java \
	-classpath '$(PARENT_DIR)/lib/*'
	@chmod 775 $(MAKEFILE_DIR)net/kanolab/aiwolf/server/gui/*.class

clear-gui:
	@rm $(MAKEFILE_DIR)net/kanolab/aiwolf/server/gui/*.class

build-game:
	@javac $(MAKEFILE_DIR)net/kanolab/aiwolf/server/game/*.java \
	-classpath '$(PARENT_DIR)/lib/*'
	@chmod 775 $(MAKEFILE_DIR)net/kanolab/aiwolf/server/game/*.class

clear-game:
	@rm $(MAKEFILE_DIR)net/kanolab/aiwolf/server/game/*.class

build-client:
	@javac $(MAKEFILE_DIR)net/kanolab/aiwolf/server/client/*.java \
	-classpath '$(PARENT_DIR)/lib/*'
	@chmod 775 $(MAKEFILE_DIR)net/kanolab/aiwolf/server/client/*.class

clear-client:
	@rm  $(MAKEFILE_DIR)net/kanolab/aiwolf/server/client/*.class

build-common:
	@javac $(MAKEFILE_DIR)net/kanolab/aiwolf/server/common/*.java \
	-classpath '$(PARENT_DIR)/lib/*'
	@chmod 775 $(MAKEFILE_DIR)net/kanolab/aiwolf/server/common/*.class

clear-common:
	@rm  $(MAKEFILE_DIR)net/kanolab/aiwolf/server/common/*.class

# (common,gui)
build-server:
	@javac $(MAKEFILE_DIR)net/kanolab/aiwolf/server/server/*.java \
	-classpath '$(PARENT_DIR)/lib/*':'$(PARENT_DIR)/src'
	@chmod 775 $(MAKEFILE_DIR)net/kanolab/aiwolf/server/server/*.class

clear-server:
	@rm $(MAKEFILE_DIR)net/kanolab/aiwolf/server/server/*.class

# (server,common,client)
build-starter:
	@javac $(MAKEFILE_DIR)net/kanolab/aiwolf/server/starter/*.java \
	-classpath '$(PARENT_DIR)/lib/*':'$(PARENT_DIR)/src' \
	-Xlint:none
	@chmod 775 $(MAKEFILE_DIR)net/kanolab/aiwolf/server/starter/*.class

clear-starter:
	@rm $(MAKEFILE_DIR)net/kanolab/aiwolf/server/starter/*.class

# ng(starter,client,aiwolf.agent)
build-automatic:
	@javac $(MAKEFILE_DIR)net/kanolab/aiwolf/server/automatic/*.java \
	-classpath '$(PARENT_DIR)/lib/*':'$(PARENT_DIR)/src' \
	-Xlint:none
	@chmod 775 $(MAKEFILE_DIR)net/kanolab/aiwolf/server/automatic/*.class

clear-automatic:
	@rm $(MAKEFILE_DIR)net/kanolab/aiwolf/server/automatic/*.class

run:
	java net/kanolab/aiwolf/server/automatic/AutoGameStarter

# -classpathについて
# https://kiririmode.hatenablog.jp/entry/20150121/1421766000
# https://qiita.com/maple_syrup/items/f5e17eaf5483d0f4a9a8