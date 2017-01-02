/*
Copyright 2016 Institut National de la Recherche Agronomique

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

package org.bibliome.alvisnlp.converters.expression;

import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.queryParser.TokenMgrError;
import org.bibliome.alvisnlp.converters.expression.parser.ExpressionParser;
import org.bibliome.alvisnlp.converters.expression.parser.ParseException;

import alvisnlp.converters.ConverterException;
import alvisnlp.converters.lib.Converter;
import alvisnlp.converters.lib.SimpleParamConverter;
import alvisnlp.corpus.expressions.Expression;

@Converter(targetType=Expression.class)
public class ExpressionParamConverter extends SimpleParamConverter<Expression> {
	@Override
	public String[] getAlternateAttributes() {
		return null;
	}

	@Override
	protected Expression convertTrimmed(String stringValue) throws ConverterException {
		Reader r = new StringReader(stringValue);
		ExpressionParser parser = new ExpressionParser(r);
		try {
			return parser.expression();
		}
		catch (ParseException|TokenMgrError e) {
			cannotConvertString(stringValue, e.getMessage());
			return null;
		}
	}
}
