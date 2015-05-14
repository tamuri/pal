// HeightInformationUser.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: HeightInformationUser </p>
 * <p>Description: Something that uses height information </p>
 * @author Matthew Goode
 * @version 1.0
 */

public final class HeightInformationUser {
	private static final int INITIAL_SIZE = 10;
	private static final int INCREMENT_SIZE = 10;


	private String[] labels_ = new String[INITIAL_SIZE];
	private double[] heights_ = new double[INITIAL_SIZE];
	private int numberOfDatas_ = 0;

	public HeightInformationUser() {}

	private final void checkSize() {
		if(heights_.length==numberOfDatas_) {
			double[] newHeights = new double[numberOfDatas_+INCREMENT_SIZE];
		  String[] newLabels = new String[numberOfDatas_+INCREMENT_SIZE];
			System.arraycopy(heights_, 0, newHeights,0,numberOfDatas_);
			System.arraycopy(labels_, 0, newLabels,0,numberOfDatas_);

		  this.heights_ = newHeights;
			this.labels_ = newLabels;
		}
	}
	public void addHeight(String label, double height) {
		checkSize();
		labels_[numberOfDatas_]= label;
		heights_[numberOfDatas_++] = height;
	}
	public String[] getLabels() {
		String[] result = new String[numberOfDatas_];
		System.arraycopy(labels_,0,result,0,numberOfDatas_);
		return result;
	}
	public double[] getHeights() {
		double[] result = new double[numberOfDatas_];
		System.arraycopy(heights_,0,result,0,numberOfDatas_);
		return result;

	}
}