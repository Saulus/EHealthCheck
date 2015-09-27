package models;

public class Knr {
	String name;
	double praev;
	int feature_no;

	public Knr(String name, double praev, int feature_no) {
		this.name=name;
		this.praev=praev;
		this.feature_no=feature_no;
	}
	
	public String getName() {
		return name;
	}
	
	public double getPraev() {
		return praev;
	}

	public int getFeature_no() {
		return feature_no;
	}

}
