package pal.misc;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public interface IndividualParameterHandler extends java.io.Serializable{
	public void setParameter(double value);
	public void setParameterSE(double value);
	public double getParameter();
	public double getLowerLimit();
	public double getUpperLimit();
	public double getDefaultValue();
	public static interface Listener {
	  public void parameterChanged(Parameterized base, int parameterIndex, double value);
	}
	public static final class Utils {
	  public static final IndividualParameterHandler createSimple(Parameterized base, int parameterIndex) {
		  return new SimpleParameter(base,parameterIndex);
		}
		public static final IndividualParameterHandler[] createSimple(Parameterized[] bases) {
			int total = 0;
			for(int i = 0 ; i < bases.length ; i++) {
			  total+=bases[i].getNumParameters();
			}
			IndividualParameterHandler[] result = new IndividualParameterHandler[total];
			int count = 0;
			for(int i = 0 ; i < bases.length ; i++) {
			  int numberOfParameters = bases[i].getNumParameters();
				for(int j = 0 ;  j < numberOfParameters ; j++) {
				  result[count++] = createSimple(bases[i],j);
				}
			}
		  return result;
		}
		public static final IndividualParameterHandler createSimple(Parameterized base, int parameterIndex, Listener listener) {
		  return new SimpleParameter(base,parameterIndex);
		}
		// =--=-=
		private static final class SimpleParameter implements IndividualParameterHandler {
			private final Parameterized base_;
			private final int parameterIndex_;
			private final Listener listener_;
			//
			// Serialization Code
			//
			private static final long serialVersionUID = -9932372342424L;

			public SimpleParameter(Parameterized base, int parameterIndex) {
			  this(base,parameterIndex,null);
			}
			public SimpleParameter(Parameterized base, int parameterIndex, Listener listener) {
				this.base_ = base;
				this.parameterIndex_ = parameterIndex;
				this.listener_ = listener;
			}
			public void setParameter(double value) {
				base_.setParameter(value,parameterIndex_);
				if(listener_!=null) {
				  listener_.parameterChanged(base_,parameterIndex_,value);
				}
			}
			public void setParameterSE(double value){
				base_.setParameterSE(value,parameterIndex_);
			}
			public double getParameter(){
				return base_.getParameter(parameterIndex_);
			}
			public double getLowerLimit(){
				return base_.getLowerLimit(parameterIndex_);
			}
			public double getUpperLimit(){
				return base_.getUpperLimit(parameterIndex_);
			}
			public double getDefaultValue(){
				return base_.getDefaultValue(parameterIndex_);
			}
		} //End of class SimpleParameter
	} //End of class Utils

} //End of interface IndividualParameterHandler