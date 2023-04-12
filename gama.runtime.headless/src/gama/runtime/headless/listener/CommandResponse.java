package gama.runtime.headless.listener;

import gama.core.util.IMap;
import gama.core.util.file.json.Jsoner;
import gama.runtime.headless.core.GamaServerMessage;
import gama.runtime.headless.core.GamaServerMessageType;

public class CommandResponse  extends GamaServerMessage {

	public final IMap<String, Object> commandParameters;
	protected boolean isJson=false;
	
	public CommandResponse(final GamaServerMessageType t,final Object content, final IMap<String, Object> parameters, final boolean isJSON) {
		super(t, content);
		this.commandParameters = parameters;
		this.isJson=isJSON;
	}
	
	@Override
	public String toJson() {
		var params = commandParameters.copy(null);
		params.remove("server");
		return "{ "
				+ "\"type\": \"" + type + "\","
				+ "\"content\": " + ((isJson) ? content : Jsoner.serialize(content)) + ","

//				+ "\"content\": " + ((isJson) ? (content!=""?content:"\"\"") : Jsoner.serialize(content)) + ","
				+ "\"command\": " + Jsoner.serialize(params) 
				+ "}";
	}

}
