/*******************************************************************************************************
 *
 * JobPlan.java, in msi.gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.runtime.headless.job;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gama.core.annotations.common.interfaces.IKeyword;
import gama.core.kernel.model.IModel;
import gama.runtime.headless.core.GamaHeadlessException;
import gama.runtime.headless.core.HeadlessSimulationLoader;
import gaml.core.descriptions.ExperimentDescription;

/**
 * The Class JobPlan.
 */
public class JobListFactory {

	public record JobPlanExperimentID(String modelName, String experimentName) {}

	/**
	 * Construct all jobs.
	 *
	 * @param seeds
	 *            the seeds
	 * @param finalStep
	 *            the final step
	 * @return the list
	 * @throws GamaHeadlessException
	 * @throws IOException
	 */
	public static List<IExperimentJob> constructAllJobs(final String modelPath, final long[] seeds,
			final long finalStep) throws IOException, GamaHeadlessException {
		IModel model = HeadlessSimulationLoader.loadModel(new File(modelPath), null);
		Map<JobPlanExperimentID, IExperimentJob> originalJobs = new LinkedHashMap<>();
		for (final ExperimentDescription expD : model.getDescription().getExperiments()) {
			if (!IKeyword.BATCH.equals(expD.getLitteral(IKeyword.TYPE))) {
				final IExperimentJob tj = ExperimentJob.loadAndBuildJob(expD, model.getFilePath(), model);
				// TODO AD Why 12 ??
				tj.setSeed(12);
				originalJobs.put(new JobPlanExperimentID(tj.getModelName(), tj.getExperimentName()), tj);
			}
		}
		final List<IExperimentJob> jobs = new ArrayList<>();
		for (final IExperimentJob locJob : originalJobs.values()) {
			jobs.addAll(constructJobWithName(locJob, seeds, finalStep, null, null));
		}
		return jobs;
	}

	/**
	 * Construct job with name.
	 *
	 * @param originalExperiment
	 *            the original experiment
	 * @param seeds
	 *            the seeds
	 * @param finalStep
	 *            the final step
	 * @param in
	 *            the in
	 * @param out
	 *            the out
	 * @return the list
	 */
	private static List<IExperimentJob> constructJobWithName(final IExperimentJob originalExperiment,
			final long[] seeds, final long finalStep, final List<Parameter> in, final List<Output> out) {
		final List<IExperimentJob> res = new ArrayList<>();
		for (final long sd : seeds) {
			final IExperimentJob job = new ExperimentJob((ExperimentJob) originalExperiment);
			job.setSeed(sd);
			job.setFinalStep(finalStep);
			if (in != null) { for (final Parameter p : in) { job.setParameterValueOf(p.getName(), p.getValue()); } }
			if (out != null) {
				final List<String> availableOutputs = job.getOutputNames();
				for (final Output o : out) {
					job.setOutputFrameRate(o.getName(), o.getFrameRate());
					availableOutputs.remove(o.getName());
				}
				for (final String s : availableOutputs) { job.removeOutputWithName(s); }
			}

			res.add(job);
		}
		return res;
	}

}
