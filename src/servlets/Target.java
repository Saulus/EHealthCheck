package servlets;

import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.BinomialModelPrediction;

public class Target {
	String name;
	double praevalenz;
	double auc_train;
	double auc_test;
	hex.genmodel.GenModel rawModel;
	EasyPredictModelWrapper model;
	

	public Target(String name, String mean, String auc_train, String auc_test, String model_id) throws Exception {
		this.name=name;
		this.praevalenz=Double.parseDouble(mean);
		this.auc_train=Double.parseDouble(auc_train);
		this.auc_test=Double.parseDouble(auc_test);
		this.rawModel = (hex.genmodel.GenModel) Class.forName(model_id).newInstance();
		this.model = new EasyPredictModelWrapper(rawModel);
	}
	
	public EasyPredictModelWrapper getModel() {
		return model;
	}
	
	public double getPraevalenz() {
		return praevalenz;
	}
	
	public double getAuc(boolean forTest) {
		if (forTest) return auc_test;
		else return auc_train;
	}
	
	
	public double getRelativeRiskFactor (RowData features) throws Exception {
		BinomialModelPrediction p = model.predictBinomial(features);
		double risk = p.classProbabilities[1]/this.praevalenz;
		return risk;
	}
	
	

	

}
