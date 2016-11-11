package org.metaborg.meta.lang.dynsem.interpreter.nodes.rules;

public enum RuleKind {
	// DYNAMIC rules are those added dynamically to build the dispatch chain
	DYNAMIC,
	// classic rules reducing on a constructor pattern
	TERM,
	// rules reducing on an entire sort
	SORT,
	// rules reducing on the most generic AST type
	AST,
	// rules that consume lists
	LIST,
	// rules that consume maps
	MAP,
	// rules that consume primitives
	PRIMITIVE,
	// rules that reduce on native data types
	NATIVETYPE
}
