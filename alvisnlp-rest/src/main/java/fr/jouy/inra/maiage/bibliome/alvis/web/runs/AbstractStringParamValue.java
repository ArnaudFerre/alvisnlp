package fr.jouy.inra.maiage.bibliome.alvis.web.runs;

import java.io.IOException;
import java.net.URISyntaxException;

import org.bibliome.util.service.UnsupportedServiceException;
import org.bibliome.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import alvisnlp.converters.ConverterException;
import alvisnlp.corpus.Corpus;
import alvisnlp.module.ParameterException;
import alvisnlp.module.Sequence;
import alvisnlp.plan.PlanException;
import alvisnlp.plan.PlanLoader;

public abstract class AbstractStringParamValue extends ParamValue<String> {
	protected AbstractStringParamValue(String method, String name, String value) {
		super(method, name, value);
	}

	protected abstract String getConcreteValue();

	@Override
	public void setParam(PlanLoader<Corpus> planLoader, Sequence<Corpus> plan) throws ParameterException, UnsupportedServiceException, PlanException, ConverterException, SAXException, IOException, URISyntaxException {
		Document doc = XMLUtils.docBuilder.newDocument();
		Element elt = XMLUtils.createRootElement(doc, getName());
		elt.setTextContent(getConcreteValue());
		planLoader.setParam(elt, plan);
	}

	@Override
	protected void fillXMLValue(Element elt) {
		elt.setTextContent(getValue());
	}
}
