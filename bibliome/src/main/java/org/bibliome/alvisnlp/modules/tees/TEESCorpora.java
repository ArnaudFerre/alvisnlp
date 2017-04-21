package org.bibliome.alvisnlp.modules.tees;

import java.util.HashMap;

import org.bibliome.util.defaultmap.DefaultMap;

class TEESCorpora extends DefaultMap<String,CorpusTEES> {
	TEESCorpora() {
		super(true, new HashMap<String,CorpusTEES>());
	}

	@Override
	protected CorpusTEES defaultValue(String key) {
		return new CorpusTEES();
	}
}
