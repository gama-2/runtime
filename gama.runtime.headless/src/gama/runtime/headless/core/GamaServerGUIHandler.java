/*******************************************************************************************************
 *
 * GamaServerGUIHandler.java, in msi.gama.headless, is part of the source code of the GAMA modeling and simulation
 * platform (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.runtime.headless.core;

import org.java_websocket.WebSocket;

import gama.core.annotations.utils.DEBUG;
import gama.core.common.interfaces.IConsoleDisplayer;
import gama.core.common.interfaces.IStatusDisplayer;
import gama.core.kernel.experiment.IExperimentAgent;
import gama.core.kernel.experiment.ITopLevelAgent;
import gama.core.runtime.IScope;
import gama.core.runtime.NullGuiHandler;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.GamaColor;
import gama.core.util.file.json.DeserializationException;
import gama.core.util.file.json.Jsoner;

/**
 * Implements the behaviours to trigger when GUI events happen in a simulation run in GamaServer
 *
 */
public class GamaServerGUIHandler extends NullGuiHandler {

	/** The status. */
	IStatusDisplayer status;

	/**
	 * Send message.
	 *
	 * @param exp
	 *            the exp
	 * @param m
	 *            the m
	 * @param type
	 *            the type
	 */
	private static void sendMessage(final IExperimentAgent exp, final Object m, final GamaServerMessageType type) {

		try {

			if (exp == null) {
				DEBUG.OUT("No experiment, unable to send message: " + m);
				return;
			}

			var scope = exp.getScope();
			if (scope == null) {
				DEBUG.OUT("No scope, unable to send message: " + m);
				return;
			}
			var socket = (WebSocket) scope.getData("socket");
			if (socket == null) {
				DEBUG.OUT("No socket found, maybe the client is already disconnected. Unable to send message: " + m);
				return;
			}
			socket.send(Jsoner.serialize(new GamaServerMessage(type, m, (String) scope.getData("exp_id"))));

		} catch (Exception ex) {
			ex.printStackTrace();
			DEBUG.OUT("Unable to send message:" + m);
			DEBUG.OUT(ex.toString());
		}
	}

	/**
	 * Can send dialog messages.
	 *
	 * @param scope
	 *            the scope
	 * @return true, if successful
	 */
	private boolean canSendDialogMessages(final IScope scope) {
		if (scope != null && scope.getExperiment() != null && scope.getExperiment().getScope() != null) {
			return (scope.getExperiment().getScope().getData("dialog") != null) ? (boolean) scope.getExperiment().getScope().getData("dialog") : true;
		}
		return true;
	}

	@Override
	public void openMessageDialog(final IScope scope, final String message) {
		DEBUG.OUT(message);
		if (!canSendDialogMessages(scope)) return;
		sendMessage(scope.getExperiment(), message, GamaServerMessageType.SimulationDialog);
	}

	@Override
	public void openErrorDialog(final IScope scope, final String error) {
		DEBUG.OUT(error);
		if (!canSendDialogMessages(scope)) return;
		sendMessage(scope.getExperiment(), error, GamaServerMessageType.SimulationErrorDialog);
	}

	@Override
	public void runtimeError(final IScope scope, final GamaRuntimeException g) {
		DEBUG.OUT(g);
		if (!canSendDialogMessages(scope)) return;
		sendMessage(scope.getExperiment(), g, GamaServerMessageType.SimulationError);
	}

	@Override
	public IStatusDisplayer getStatus() {

		if (status == null) {
			status = new IStatusDisplayer() {

				private boolean canSendMessage(final IExperimentAgent exp) {
					if(exp==null) return false;
					var scope = exp.getScope();
					return scope != null && scope.getData("status") != null ? (boolean) scope.getData("status") : true;
				}

				@Override
				public void informStatus(final IScope scope, final String string) {

					if (!canSendMessage(scope.getExperiment())) return;

					try {
						sendMessage(scope.getExperiment(),
								Jsoner.deserialize("{" + "\"message\": \"" + string + "\"" + "}"),
								GamaServerMessageType.SimulationStatusInform);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(scope.getExperiment(), "{" + "\"message\": \"" + string + "\"" + "}",
								GamaServerMessageType.SimulationStatusInform);
					}

				}

				@Override
				public void errorStatus(final IScope scope, final String message) {

					if (!canSendMessage(scope.getExperiment())) return;

					try {
						sendMessage(scope.getExperiment(),
								Jsoner.deserialize("{" + "\"message\": \"" + message + "\"" + "}"),
								GamaServerMessageType.SimulationStatusError);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(scope.getExperiment(), "{" + "\"message\": \"" + message + "\"" + "}",
								GamaServerMessageType.SimulationStatusError);
					}

				}

				@Override
				public void setStatus(final IScope scope, final String msg, final GamaColor color) {

					if (!canSendMessage(scope.getExperiment())) return;

					try {
						sendMessage(
								scope.getExperiment(), Jsoner.deserialize("{" + "\"message\": \"" + msg + "\","
										+ "\"color\": " + Jsoner.serialize(color) + "" + "}"),
								GamaServerMessageType.SimulationStatus);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(scope.getExperiment(),
								"{" + "\"message\": \"" + msg + "\"," + "\"color\": " + Jsoner.serialize(color) + "}",
								GamaServerMessageType.SimulationStatus);
					}
				}

				@Override
				public void informStatus(final IScope scope, final String message, final String icon) {

					if (!canSendMessage(scope.getExperiment())) return;

					try {
						sendMessage(scope.getExperiment(),
								Jsoner.deserialize(
										"{" + "\"message\": \"" + message + "\"," + "\"icon\": \"" + icon + "\"" + "}"),
								GamaServerMessageType.SimulationStatusInform);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(scope.getExperiment(),
								"{" + "\"message\": \"" + message + "\"," + "\"icon\": \"" + icon + "\"" + "}",
								GamaServerMessageType.SimulationStatusInform);
					}

				}

				@Override
				public void setStatus(final IScope scope, final String msg, final String icon) {

					if (!canSendMessage(scope.getExperiment())) return;

					try {
						sendMessage(scope.getExperiment(),
								Jsoner.deserialize(
										"{" + "\"message\": \"" + msg + "\"," + "\"icon\":\"" + icon + "\"" + "}"),
								GamaServerMessageType.SimulationStatus);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(scope.getExperiment(),
								"{" + "\"message\": \"" + msg + "\"," + "\"icon\": \"" + icon + "\"" + "}",
								GamaServerMessageType.SimulationStatus);
					}
				}

				@Override
				public void neutralStatus(final IScope scope, final String string) {

					if (!canSendMessage(scope.getExperiment())) return;

					try {
						sendMessage(scope.getExperiment(),
								Jsoner.deserialize("{" + "\"message\": \"" + string + "\"" + "}"),
								GamaServerMessageType.SimulationStatusNeutral);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(scope.getExperiment(), "{" + "\"message\": \"" + string + "\"" + "}",
								GamaServerMessageType.SimulationStatusNeutral);
					}

				}

			};
		}
		return status;
	}

	@Override
	public IConsoleDisplayer getConsole() {
		if (console == null) {

			console = new IConsoleDisplayer() {

				private boolean canSendMessage(final IExperimentAgent exp) {
					var scope = exp.getScope();
					return scope != null && scope.getData("console") != null ? (boolean) scope.getData("console")
							: true;
				}

				@Override
				public void informConsole(final String s, final ITopLevelAgent root, final GamaColor color) {
					System.out.println(s);
					if (!canSendMessage(root.getExperiment())) return;

					try {
						sendMessage(
								root.getExperiment(), Jsoner.deserialize("{" + "\"message\": \"" + s + "\","
										+ "\"color\":" + Jsoner.serialize(color) + "}"),
								GamaServerMessageType.SimulationOutput);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(root.getExperiment(),
								"{" + "\"message\": \"" + s + "\"," + "\"color\":" + Jsoner.serialize(color) + "}",
								GamaServerMessageType.SimulationOutput);
					}
				}

				@Override
				public void debugConsole(final int cycle, final String s, final ITopLevelAgent root,
						final GamaColor color) {
					if (!canSendMessage(root.getExperiment())) return;
					try {
						sendMessage(root.getExperiment(),
								Jsoner.deserialize("{" + "\"cycle\":" + cycle + "," + "\"message\": \""
										+ Jsoner.escape(s) + "\"," + "\"color\":" + Jsoner.serialize(color) + "}"),
								GamaServerMessageType.SimulationDebug);
					} catch (DeserializationException e) {
						// If for some reason we cannot deserialize, we send it as text
						e.printStackTrace();
						sendMessage(root.getExperiment(),
								"{" + "\"cycle\":" + cycle + "," + "\"message\": \"" + Jsoner.escape(s) + "\","
										+ "\"color\":" + Jsoner.serialize(color) + "}",
								GamaServerMessageType.SimulationDebug);
					}
				}
			};
		}
		return console;

	}

}
