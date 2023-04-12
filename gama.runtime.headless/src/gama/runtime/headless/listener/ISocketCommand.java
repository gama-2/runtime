package gama.runtime.headless.listener;

import org.java_websocket.WebSocket;

import gama.core.util.IMap;
import gama.runtime.headless.core.GamaServerMessage;

public interface ISocketCommand {
 

	public GamaServerMessage execute(final WebSocket socket, final IMap<String, Object> map);

	
}
