package gama.runtime.headless.listener;

import org.java_websocket.WebSocket;

import gama.core.annotations.utils.DEBUG;
import gama.core.util.IMap;
import gama.core.util.file.json.GamaJsonList;
import gama.runtime.headless.core.GamaServerMessageType;
import gama.runtime.headless.job.ManualExperimentJob;

public class ReloadCommand implements ISocketCommand {
	
	@Override
	public CommandResponse execute(final WebSocket socket, IMap<String, Object> map) {

		final String exp_id 	= map.get("exp_id") != null ? map.get("exp_id").toString() : "";
		final String socket_id	= map.get("socket_id") != null ? map.get("socket_id").toString() : ("" + socket.hashCode());
		final GamaWebSocketServer gamaWebSocketServer = (GamaWebSocketServer) map.get("server");
		DEBUG.OUT("reload");
		DEBUG.OUT(exp_id);
		

		if (exp_id == "" ) {
			return new CommandResponse(GamaServerMessageType.MalformedRequest, "For 'reload', mandatory parameter is: 'exp_id'", map, false);
		}

		var gama_exp = gamaWebSocketServer.get_listener().getExperiment(socket_id, exp_id); 
		if (gama_exp != null && gama_exp.getSimulation() != null) {

			gama_exp.params = (GamaJsonList) map.get("parameters");
			gama_exp.endCond = map.get("until") != null ? map.get("until").toString() : "";
			gama_exp.controller.userReload();			
			return new CommandResponse(GamaServerMessageType.CommandExecutedSuccessfully, "", map, false);
		}
		else {
			return new CommandResponse(GamaServerMessageType.UnableToExecuteRequest, "Unable to find the experiment or simulation", map, false);
		}	
	}
}
