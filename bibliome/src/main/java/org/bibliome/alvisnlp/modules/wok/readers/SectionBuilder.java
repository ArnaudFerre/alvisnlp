/*
Copyright 2016, 2017 Institut National de la Recherche Agronomique

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


package org.bibliome.alvisnlp.modules.wok.readers;

import org.bibliome.alvisnlp.modules.wok.FieldReader;
import org.bibliome.alvisnlp.modules.wok.WoKReaderStatus;

import alvisnlp.corpus.Document;
import alvisnlp.corpus.Section;
import alvisnlp.corpus.creators.SectionCreator;

public class SectionBuilder implements FieldReader {
	private final String sectionName;
	private final StringBuilder contents = new StringBuilder();

	public SectionBuilder(String sectionName) {
		super();
		this.sectionName = sectionName;
	}

	@Override
	public void start(WoKReaderStatus status) {
	}

	@Override
	public void addLine(WoKReaderStatus status, String line) {
		if (contents.length() > 0) {
			contents.append('\n');
		}
		contents.append(line);
	}

	@Override
	public void finish(WoKReaderStatus status) {
		Document doc = status.getDocument();
		SectionCreator sc = status.getOwner();
		new Section(sc, doc, sectionName, contents.toString());
	}
}
