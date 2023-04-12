/*******************************************************************************************************
 *
 * RichExperiment.java, in msi.gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.runtime.headless.core;

import gama.core.kernel.model.IModel;
import gama.core.outputs.AbstractOutputManager;
import gama.core.outputs.FileOutput;
import gama.core.outputs.IOutput;
import gama.core.outputs.LayeredDisplayOutput;
import gama.core.outputs.MonitorOutput;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.runtime.headless.common.DataType;
import gama.runtime.headless.common.DataTypeFactory;
import gama.runtime.headless.job.ListenedVariable;
import gama.runtime.headless.job.ExperimentJob.OutputType;

/**
 * The Class RichExperiment.
 */
public class RichExperiment extends Experiment implements IRichExperiment {

	/**
	 * Instantiates a new rich experiment.
	 *
	 * @param mdl
	 *            the mdl
	 */
	public RichExperiment(final IModel mdl) {
		super(mdl);
	}

	@Override
	public OutputType getTypeOf(final String name) {
		if (currentExperiment == null) return OutputType.OUTPUT;
		if (currentExperiment.hasVar(name)) return OutputType.EXPERIMENT_ATTRIBUTE;
		if (currentExperiment.getModel().getSpecies().hasVar(name)) return OutputType.SIMULATION_ATTRIBUTE;
		return OutputType.OUTPUT;
	}

	@Override
	public RichOutput getRichOutput(final ListenedVariable v) {
		final String parameterName = v.getName();
		if (getSimulation() == null || getSimulation().dead()) return null;
		final IOutput output =
				((AbstractOutputManager) getSimulation().getOutputManager()).getOutputWithOriginalName(parameterName);
		if (output == null)
			throw GamaRuntimeException.error("Output unresolved", currentExperiment.getExperimentScope());
		output.update();

		Object val = null;
		DataType tpe = null;

		if (output instanceof MonitorOutput) {
			val = ((MonitorOutput) output).getLastValue();
			tpe = DataTypeFactory.getObjectMetaData(val);
		} else if (output instanceof LayeredDisplayOutput) {
			val = ((LayeredDisplayOutput) output).getImage(v.width, v.height);
			tpe = DataType.DISPLAY2D;
		} else if (output instanceof FileOutput) {
			val = ((FileOutput) output).getFile();
			tpe = DataType.DISPLAY2D;
		}
		return new RichOutput(parameterName, this.currentStep, val, tpe);
	}

}
