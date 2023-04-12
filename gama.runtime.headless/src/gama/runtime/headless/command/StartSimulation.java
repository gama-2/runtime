/*******************************************************************************************************
 *
 * StartSimulation.java, in msi.gama.headless, is part of the source code of the GAMA modeling and simulation platform
 * (v.1.9.0).
 *
 * (c) 2007-2022 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.runtime.headless.command;

import java.io.File;
import java.io.IOException;

import gama.core.annotations.precompiler.IConcept;
import gama.core.annotations.precompiler.ISymbolKind;
import gama.core.annotations.precompiler.GamlAnnotations.doc;
import gama.core.annotations.precompiler.GamlAnnotations.facet;
import gama.core.annotations.precompiler.GamlAnnotations.facets;
import gama.core.annotations.precompiler.GamlAnnotations.inside;
import gama.core.annotations.precompiler.GamlAnnotations.symbol;
import gama.core.kernel.model.IModel;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.runtime.headless.core.Experiment;
import gama.runtime.headless.core.GamaHeadlessException;
import gama.runtime.headless.core.HeadlessSimulationLoader;
import gaml.core.descriptions.IDescription;
import gaml.core.operators.Cast;
import gaml.core.statements.AbstractStatement;
import gaml.core.types.IType;

/**
 * The Class StartSimulation.
 */

/**
 * The Class StartSimulation.
 */

/**
 * The Class StartSimulation.
 */
@symbol (
		name = IKeywords.STARTSIMULATION,
		kind = ISymbolKind.SEQUENCE_STATEMENT,
		with_sequence = true,
		doc = @doc ("Allows to run an experiment on a different model"),
		concept = { IConcept.HEADLESS })
@inside (
		kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SINGLE_STATEMENT, ISymbolKind.SPECIES, ISymbolKind.MODEL })
@facets (
		value = { @facet (
				name = IKeywords.MODEL,
				type = IType.STRING,
				doc = @doc ("The path to the model containing the experiment"),
				optional = false),
				@facet (
						name = IKeywords.EXPERIMENT,
						type = IType.STRING,
						doc = @doc ("The name of the experiment to run"),
						optional = false),
				@facet (
						name = IKeywords.WITHSEED,
						type = IType.INT,
						doc = @doc ("The seed to use for initializing the random number generator of the new experiment"),
						optional = true),
				@facet (
						name = IKeywords.WITHPARAMS,
						type = IType.MAP,
						doc = @doc ("The parameters to pass to the new experiment"),
						optional = true) },
		omissible = IKeywords.EXPERIMENT)
public class StartSimulation extends AbstractStatement {

	/**
	 * Instantiates a new start simulation.
	 *
	 * @param desc
	 *            the desc
	 */
	public StartSimulation(final IDescription desc) {
		super(desc);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Retrieve model file absolute path.
	 *
	 * @param scope
	 *            the scope
	 * @param filename
	 *            the filename
	 * @return the string
	 */
	private String retrieveModelFileAbsolutePath(final IScope scope, final String filename) {
		if (filename.charAt(0) == '/') return filename;
		return new File(scope.getModel().getFilePath()).getParentFile().getAbsolutePath() + "/" + filename;
	}

	/**
	 * Private execute in.
	 *
	 * @param scope
	 *            the scope
	 * @return the object
	 * @throws GamaRuntimeException
	 *             the gama runtime exception
	 */
	@Override
	protected Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		int seed = 0;
		final String expName = Cast.asString(scope, getFacetValue(scope, IKeywords.EXPERIMENT));
		String modelPath = Cast.asString(scope, getFacetValue(scope, IKeywords.MODEL));
		if (modelPath != null && !modelPath.isEmpty()) {
			modelPath = retrieveModelFileAbsolutePath(scope, modelPath);
		} else {
			// no model specified, this caller model path is used.
			modelPath = scope.getModel().getFilePath();
		}

		if (this.hasFacet(IKeywords.WITHSEED)) { seed = Cast.asInt(scope, getFacetValue(scope, IKeywords.WITHSEED)); }

		final long lseed = seed;

		IModel mdl = null;
		try {
			mdl = HeadlessSimulationLoader.loadModel(new File(modelPath), null, null, GAMA.isInHeadLessMode());
		} catch (final IOException e) {
			throw GamaRuntimeException.error("Sub model file not found!", scope);
		} catch (final GamaHeadlessException e) {
			throw GamaRuntimeException.error("Sub model file cannot be loaded", scope);
		}
		final Experiment exp = new Experiment(mdl);
		exp.setup(expName, lseed);
		final String varName = exp.toString();
		scope.addVarWithValue(varName, exp);
		return varName;
	}

}
