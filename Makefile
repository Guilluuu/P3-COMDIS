SRC = $(wildcard ./src/*.java)

state: $(SRC)
	javac $(SRC) -d bin
	touch state

clean:
	rm -rf bin
	rm state

run_servidor: state
	java -cp "bin" Servidor

run_cliente: state
	java -cp "bin" ModernChatUI

run: state
	kitty --hold -d "${PWD}" &
	kitty --hold -d "${PWD}" &
	java -cp "bin:lib/*"  Servidor

