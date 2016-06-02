package org.metaborg.meta.lang.dynsem.interpreter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleRegistry;
import org.metaborg.meta.lang.dynsem.interpreter.nodes.rules.RuleResult;
import org.metaborg.meta.lang.dynsem.interpreter.terms.ITerm;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.api.vm.PolyglotEngine.Builder;
import com.oracle.truffle.api.vm.PolyglotEngine.Value;

/**
 * Abstract class of an entrypoint to a {@link DynSemLanguage}. This class is responsible for instantiating the VM
 * properly and provides an interface for evaluating source code in that language.
 */
public abstract class DynSemEntryPoint {
	private final IDynSemLanguageParser parser;
	private final RuleRegistry ruleRegistry;
	private final ITermRegistry termRegistry;

	public DynSemEntryPoint(IDynSemLanguageParser parser, ITermRegistry termRegistry, RuleRegistry ruleRegistry) {
		this.parser = parser;
		this.termRegistry = termRegistry;
		this.ruleRegistry = ruleRegistry;
	}

	public RuleResult evaluate(String file, InputStream input, OutputStream output, OutputStream error)
			throws Exception {
		return getCallable(file, input, output, error).call();
	}

	public Callable<RuleResult> getCallable(String file, InputStream input, OutputStream output, OutputStream error) {
		PolyglotEngine vm = buildPolyglotEngine(input, output, error);
		assert vm.getLanguages().containsKey(getMimeType());
		try {
			Value interpreter = vm
					.eval(Source.fromReader(new InputStreamReader(getSpecificationTerm()), "Evaluate to interpreter")
							.withMimeType(getMimeType()));
			IStrategoTerm term = getParser().parse(Source.fromFileName(file));
			ITerm programTerm = getTermRegistry().parseProgramTerm(term);
			return new Callable<RuleResult>() {
				@Override
				public RuleResult call() throws Exception {
					return interpreter.execute(programTerm).as(RuleResult.class);
				}
			};
		} catch (IOException ioex) {
			throw new RuntimeException("Eval failed", ioex);
		}
	}

	/**
	 * Build and configure the {@link PolyglotEngine}. Uses {@link Builder#config(String, String, Object)} for injecting
	 * dependencies: the {@link IDynSemLanguageParser parser} to be used, the {@link ITermRegistry term registry} and
	 * the {@link InputStream} of the DynSem specification term.
	 * 
	 * @param input
	 *            The {@link InputStream} of the VM.
	 * @param output
	 *            The {@link OutputStream} of the VM for standard output.
	 * @param error
	 *            The {@link OutputStream} of the VM for errors.
	 * @return The configured {@link PolyglotEngine}.
	 */
	public PolyglotEngine buildPolyglotEngine(InputStream input, OutputStream output, OutputStream error) {
		assert DynSemContext.LANGUAGE != null : "DynSemContext.LANGUAGE must be set for creating the RuleRegistry";
		return PolyglotEngine.newBuilder().setIn(input).setOut(output).setErr(error)
				.config(getMimeType(), DynSemLanguage.TERM_REGISTRY, getTermRegistry())
				.config(getMimeType(), DynSemLanguage.RULE_REGISTRY, getRuleRegistry()).build();
	}

	public IDynSemLanguageParser getParser() {
		return parser;
	}

	public ITermRegistry getTermRegistry() {
		return termRegistry;
	}

	public RuleRegistry getRuleRegistry() {
		return ruleRegistry;
	}

	public abstract String getMimeType();

	public abstract InputStream getSpecificationTerm();
}
