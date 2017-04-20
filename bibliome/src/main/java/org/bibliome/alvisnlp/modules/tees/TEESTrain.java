package org.bibliome.alvisnlp.modules.tees;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.bibliome.util.Files;
import org.bibliome.util.files.OutputFile;

import alvisnlp.corpus.Corpus;
import alvisnlp.corpus.expressions.ResolverException;
import alvisnlp.module.Module;
import alvisnlp.module.ModuleException;
import alvisnlp.module.ProcessingContext;
import alvisnlp.module.ProcessingException;
import alvisnlp.module.lib.AlvisNLPModule;
import alvisnlp.module.lib.External;
import alvisnlp.module.lib.Param;


/**
 * 
 * @author mba
 *
 */

@AlvisNLPModule
public abstract class TEESTrain extends TEESMapper {
	private String trainSetValue = "train";
	private String devSetValue = "dev";
	private String testSetValue = "test";

	@Override
	public void process(ProcessingContext<Corpus> ctx, Corpus corpus) throws ModuleException {
	
		Logger logger = getLogger(ctx);
//		EvaluationContext evalCtx = new EvaluationContext(logger);

		try {

			JAXBContext jaxbContext = JAXBContext.newInstance(CorpusTEES.class);
			logger.info("Accessing the corpora");
			Marshaller jaxbm = jaxbContext.createMarshaller();
			jaxbm.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			// marshaling
			prepareTEESCorpora(ctx, corpus);
			TEESTrainExternal teesTrainExt = new TEESTrainExternal(ctx);
			jaxbm.marshal(this.corpora.get(this.getTrainSetValue()), teesTrainExt.getTrainInput());
			jaxbm.marshal(this.corpora.get(this.getDevSetValue()), teesTrainExt.getDevInput());
			jaxbm.marshal(this.corpora.get(this.getTestSetValue()), teesTrainExt.getTestInput());

			logger.info("TEES training ");
			callExternal(ctx, "run-tees-train", teesTrainExt, INTERNAL_ENCODING, "tees-train.sh");


		}
		catch (JAXBException|IOException e) {
			rethrow(e);
		}
	}
	
	
	/**
	 * Build TEES corpus from Alvis corpus
	 * @param ctx
	 * @param corpusAlvis
	 * @throws ProcessingException 
	 */
	public void prepareTEESCorpora(ProcessingContext<Corpus> ctx, Corpus corpusAlvis) throws ProcessingException{
		Logger logger = getLogger(ctx);
//		EvaluationContext evalCtx = new EvaluationContext(logger);
		
//		logger.info("preparing the train, dev, test corpus");
		this.corpora.put(this.getTrainSetValue(), new CorpusTEES());
		this.corpora.put(this.getDevSetValue(), new CorpusTEES());
		this.corpora.put(this.getTestSetValue(), new CorpusTEES());
		
		logger.info("creating the train, dev, test corpus");
		createTheTeesCorpus(ctx, corpusAlvis);
		
		if(this.corpora.get(this.getTrainSetValue()).getDocument().size()==0 || this.corpora.get(this.getTrainSetValue()).getDocument().size()==0 || this.corpora.get(this.getTrainSetValue()).getDocument().size()==0){
			processingException("could not do training : train, dev or test is empty");
		}
	}

	
	/**
	 * object resolver and feature handler
	 */
	@Override
	protected SectionResolvedObjects createResolvedObjects(ProcessingContext<Corpus> ctx) throws ResolverException {
		return new SectionResolvedObjects(ctx, this);
	}

	@Override
	protected String[] addLayersToSectionFilter() {
		return new String[] {
				getTokenLayerName(),
				getSentenceLayerName(),
				getNamedEntityLayerName()
		};
	}

	@Override
	protected String[] addFeaturesToSectionFilter() {
		return null;
	}

	@Param
	public String getTrainSetValue() {
		return trainSetValue;
	}

	public void setTrainSetValue(String train) {
		this.trainSetValue = train;
	}

	@Param
	public String getDevSetValue() {
		return devSetValue;
	}

	public void setDevSetValue(String dev) {
		this.devSetValue = dev;
	}

	@Param
	public String getTestSetValue() {
		return testSetValue;
	}

	public void setTestSetValue(String test) {
		this.testSetValue = test;
	}

	/**
	 * 
	 * @author mba
	 *
	 */
	private class TEESTrainExternal implements External<Corpus> {
		private final OutputFile trainInput;
		private final OutputFile devInput;
		private final OutputFile testInput;
		private final ProcessingContext<Corpus> ctx;
		public final File baseDir;
		private final File script;

		private TEESTrainExternal(ProcessingContext<Corpus> ctx) throws IOException {
			super();
			this.ctx = ctx;
			File tmp = getTempDir(ctx);
			baseDir = tmp;
			this.trainInput = new OutputFile(tmp.getAbsolutePath(), "train-o" + ".xml");
			this.devInput = new OutputFile(tmp.getAbsolutePath(), "devel-o" + ".xml");
			this.testInput = new OutputFile(tmp.getAbsolutePath(), "test-o" + ".xml");
			
			//
			script = new File(tmp, "train.sh");
			// same ClassLoader as this class
			InputStream is = TEESTrain.class.getResourceAsStream("train.sh");
			Files.copy(is, script, 1024, true);
			script.setExecutable(true);
		}

		@Override
		public Module<Corpus> getOwner() {
			return TEESTrain.this;
		}
		
		@Override
		public String[] getCommandLineArgs() throws ModuleException {
			List<String> clArgs = new ArrayList<String>();
			clArgs.addAll(Arrays.asList(
					script.getAbsolutePath()));
			return clArgs.toArray(new String[clArgs.size()]);
		}

		@Override
		public String[] getEnvironment() throws ModuleException {
			return new String[] {
					"TEES_DIR=" + getTeesHome().getAbsolutePath(),
					"TEES_PRE_EXE=" + getTeesHome().getAbsolutePath() + "/Detectors/Preprocessor.py",
					"TEES_TRAIN_EXE=" + getTeesHome().getAbsolutePath() + "/train.py",
					"TEES_TRAIN_IN="  + this.trainInput.getAbsolutePath(),
					"TEES_TRAIN_OUT=" + this.baseDir.getAbsolutePath() + "/train_pre.xml",
					"TEES_DEV_IN="  + this.devInput.getAbsolutePath(),
					"TEES_DEV_OUT=" + this.baseDir.getAbsolutePath() + "/dev_pre.xml",
					"OMITSTEPS=" + getOmitSteps().toString(),
					"TEES_TEST_IN="  + this.testInput.getAbsolutePath(),
					"TEES_TEST_OUT=" + this.baseDir.getAbsolutePath() + "/test_pre.xml",
					"WORKDIR=" + this.baseDir.getAbsolutePath(),
					"MODEL=" + getModel().getAbsolutePath()
				};
		}

		@Override
		public File getWorkingDirectory() throws ModuleException {
			return this.baseDir;
		}

		@Override
		public void processOutput(BufferedReader out, BufferedReader err) throws ModuleException {
			Logger logger = getLogger(ctx);
			try {
				logger.fine("TEES standard error:");
				for (String line = err.readLine(); line != null; line = err.readLine()) {
					logger.fine("    " + line);
				}
				logger.fine("end of TEES classifier error");
			}
			catch (IOException ioe) {
				rethrow(ioe);
			}
		}


		public OutputFile getTrainInput() {
			return trainInput;
		}

		public OutputFile getDevInput() {
			return devInput;
		}

		public OutputFile getTestInput() {
			return testInput;
		}

	}
}
