default:

clean-app:
	mvn -o clean -pl messagemanager-app -am

make-app:
	mvn -o package -pl messagemanager-app -am

install-app:
	mvn -o install -pl messagemanager-app -am

install-fake:
	mvn -o package -pl messagemanager-fakemq -am
	cp messagemanager-fakemq/target/*.jar ~/Library/Application\ Support/MessageManager/plugins/3.1-SNAPSHOT/

run-app:
	java    -ea \
		-Dmm.forceInstallPlugins=true \
		-Dmm.forceMotdMessage=true \
		-Dmm.enableSwingDebug=true \
		-Dmm.developer=true \
		-Ddeveloper \
		-Djava.util.logging.config.file=logging.properties \
		-DSolace_JMS_Browser_Timeout_In_MS=1000 \
		-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n \
		-jar messagemanager-app/target/messagemanager-app-3.1-SNAPSHOT-jar-with-dependencies.jar

run-app-prod:
	java    -ea \
		-Dmm.forceInstallPlugins=true \
		-jar messagemanager-app/target/messagemanager-app-3.1-SNAPSHOT-jar-with-dependencies.jar

build-ws-app:
	mvn -Pcodesign -Dsubdir=v3/nightly/ -Dsuffix=NIGHTLY clean package -pl ws-app -am
	 
run-ws-app:
	 javaws -J-Ddeveloper=true \
		-J-Dmm.forceInstallPlugins=true \
		-J-Djava.util.logging.config.file=messagemanager-app/logging.properties \
		http://queuemanager.nl/v3/nightly/app/MessageManager.jnlp

run-ws-76:
	 javaws -J-Ddeveloper=true \
		-J-Dmm.forceInstallPlugins=true \
		-J-Djava.util.logging.config.file=messagemanager-app/logging.properties \
		http://queuemanager.nl/v3/nightly/7.6/SonicMessageManager.jnlp
	 
upload-ws-app:
	scp ws-app/target/jnlp/* neon:domains/queuemanager.nl/public_html/v3/nightly/app/

clean-plugins:
	rm -rf ~/Library/Application\ Support/MessageManager/plugins/

clean-jws:
	javaws -uninstall
	javaws -clearcache

clean-all: clean-plugins clean-jws
	mvn clean

run: clean-all build-ws-app upload-ws-app run-ws-app

