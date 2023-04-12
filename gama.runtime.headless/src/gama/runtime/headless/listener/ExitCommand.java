package gama.runtime.headless.listener;

import org.java_websocket.WebSocket;

import gama.core.util.IMap;
import gama.runtime.headless.core.GamaServerMessageType;

public class ExitCommand implements ISocketCommand {
	@Override
	public CommandResponse execute(final WebSocket socket, IMap<String, Object> map) {
		//TODO: just for compilation purposes, but makes no sense
		System.exit(0);
		return new CommandResponse(GamaServerMessageType.CommandExecutedSuccessfully, "" , map, false);
	}
}
