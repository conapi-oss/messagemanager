.PHONY:

clean: .PHONY
	mvn -o clean

build: .PHONY
	mvn -o clean package

install:
	mvn -o install

installjar:
	cp target/messagemanager-solace-3.1-SNAPSHOT.jar ~/Library/Application\ Support/MessageManager/plugins/3.1-SNAPSHOT/messagemanager-solace.jar

all: clean build install installjar

