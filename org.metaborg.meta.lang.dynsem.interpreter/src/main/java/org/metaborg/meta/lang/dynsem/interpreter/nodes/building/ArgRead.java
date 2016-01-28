package org.metaborg.meta.lang.dynsem.interpreter.nodes.building;

import org.spoofax.interpreter.terms.IStrategoTerm;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;

public class ArgRead extends TermBuild {

	private final int index;

	public ArgRead(int index, SourceSection source) {
		super(source);
		this.index = index;
	}

	@Override
	public IStrategoTerm execute(VirtualFrame frame) {
		return (IStrategoTerm) frame.getArguments()[index];
	}

}
