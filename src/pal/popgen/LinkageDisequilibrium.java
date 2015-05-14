// LinkageDisequilibrium.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.popgen;


import java.io.*;
import java.util.*;


import pal.alignment.*;
import pal.datatype.*;
import pal.statistics.*;
import pal.misc.*;

/**
 * This class calculates D' and r^2 estimates of linkage disequilibrium.  It also
 * calculates the significance of the LD by either Fisher Exact or the multinomial
 * permutation test.  This class can work with either normal alignments of annotated alignments.
 * The alignments should be stripped of invariable sites.
 *
 * 2 state estimates of D' and r^2 can be found reviewed and discussed in Weir 1996
 *
 * multi-state loci (>=3) require an averaging approach.  These should not be used for
 * popgen parameter estimates, unless you know specifically that it works for multistate loci.
 * The estimate of D' is the approach used by Farnir 2000 Genome Research 10:220-227
 * that follows Hedrick 1987.  r^2 was estimated in a similar way.
 *
 * @version $Id: LinkageDisequilibrium.java,v 1
 *
 * @author Ed Buckler
 */

public class LinkageDisequilibrium extends Thread implements Serializable, TableReport {
  /*this class converts the alignment into a matrix for internal use, as this is 2.5 times faster than
    making lots of calls to the original matrix
  */

  protected Alignment theAlignment;
  protected AnnotationAlignment theAnnotationAlignment=null;
  boolean annotated=false;
  Vector[] stateVector;
  boolean rapidPermute=true;
  int numberOfPermutations=1000;

  double[][] diseq, pDiseq;
  private double currentProgress;

      /**
	 * compute LD based on an alignment.  The default is to used used rapid permutations
         * that provides slightly biased P-values, and 1000 permutations to evaluate P-values.
	 *
         *  @param alignment  Alignment or AnnotationAlignment (this should only contain
         *                    polymorphic sites)
	 */
  public LinkageDisequilibrium(Alignment alignment) {
    this.theAlignment=alignment;
    if(theAlignment instanceof AnnotationAlignment)
      {annotated=true;
      theAnnotationAlignment=(AnnotationAlignment)theAlignment;
      }
  }
        /**
	 * compute LD based on an alignment
	 *
         *  @param alignment  Alignment or AnnotationAlignment (this should only contain
         *                    polymorphic sites)
         *  @param rapidPermute Use a rapid approach to P-value estimation (see Contigency Table)
         *  @param numberOfPermutations The number of permutations to determine P values
	 */
  public LinkageDisequilibrium(Alignment alignment, boolean rapidPermute, int numberOfPermutations) {
    this.theAlignment=alignment;
    this.rapidPermute=rapidPermute;
    this.numberOfPermutations=numberOfPermutations;
    if(theAlignment instanceof AnnotationAlignment)
      {annotated=true;
      theAnnotationAlignment=(AnnotationAlignment)theAlignment;
      }
  }

    /**
     * compute LD based on an site pattern, needs to be implemented
     *
     * @param sp site pattern
     */
//  public LinkageDisequilibrium(SitePattern sp) {
//  }

  /**
     * starts the thread to calculate LD
     */
  public void run() {
    Date theDate=new Date();
    System.out.println("Start LD run");
    calculateMultiProbDiseq();
    Date stopDate=new Date();
    System.out.println("Stop LD run Time="+(theDate.getTime()-stopDate.getTime()));
  }

  /**
   *Determines the number of states for each site, and creates a byte matrix will all
   *the states renumbered from 0 to states-1.  It also sets GAP and UNKNOWN_CHARACTERS to
   *-99.
   *
   * @return matrix of renumbered states
   */
  byte[][] determineNumberOfStates() {
    System.out.println("States starting to be loaded into S");
    stateVector = new Vector[theAlignment.getSiteCount()];
    byte[][] S=new byte[theAlignment.getSequenceCount()][theAlignment.getSiteCount()];
    char c;
    for(int i=0; i<theAlignment.getSiteCount(); i++)
      {stateVector[i]=new Vector();
      for(int j=0; j<theAlignment.getSequenceCount(); j++)
        {c=theAlignment.getData(j,i);
        if((c==Alignment.GAP)||(c==DataType.UNKNOWN_CHARACTER))
          {S[j][i]=-99;
          }
        else
        if(!stateVector[i].contains(new Character(c)))
          {stateVector[i].add(new Character(c));
          S[j][i]=(byte)stateVector[i].indexOf(new Character(c));
          }
        else
          {S[j][i]=(byte)stateVector[i].indexOf(new Character(c));
          }
        }
      }
    System.out.println("States loaded into S");
    return S;
  }

  private void calculateMultiProbDiseq() {  //only calculates disequilibrium for inbreds
     byte S[][];
     S=this.determineNumberOfStates();
     int rows, cols, n;
     ContigencyTable contigencyTable=new ContigencyTable(theAlignment.getSequenceCount()+10);
     FisherExact fisherExact=new FisherExact(theAlignment.getSequenceCount()+10);

     int[][] contig;

     diseq=new double[theAlignment.getSiteCount()][theAlignment.getSiteCount()];
     pDiseq=new double[theAlignment.getSiteCount()][theAlignment.getSiteCount()];

     for(int r=0; r<theAlignment.getSiteCount(); r++)
      {currentProgress=100*r*r/(theAlignment.getSiteCount()*theAlignment.getSiteCount());
       rows=stateVector[r].size();
//       System.out.println("r="+rows);
       pDiseq[r][r]=0.0;
       diseq[r][r]=1.0;
//       System.out.println(r+"'s states="+rows);
      for(int c=0; c<r; c++)
        {cols=stateVector[c].size();
        contig=new int[rows][cols];
        n=0;
        for(int sample=0; sample<theAlignment.getSequenceCount(); sample++)
          {//rChar=theAlignment.getData(sample,r);
          //cChar=theAlignment.getData(sample,c);
          if((S[sample][r]!=-99)&&(S[sample][c]!=-99))
            {//System.out.println("sample="+sample+"  S[sample][r]="+S[sample][r]+"  S[sample][c]="+S[sample][c]);
            contig[S[sample][r]][S[sample][c]]++;
            n++;
            }
          }//end of sample
        //System.out.println("r="+r+" c="+c+" AA="+contig[0][0]+" Aa="+contig[0][1]+" aA="+contig[1][0]+" aa="+contig[1][1]);
        if((rows==2)&&(cols==2))
          {diseq[r][c]=calculateRSqrDisequilibrium(contig[0][0],contig[1][0], contig[0][1], contig[1][1]);
          diseq[c][r]=calculateDisequilibrium(contig[0][0],contig[1][0], contig[0][1], contig[1][1]);
          pDiseq[r][c]=n;
          pDiseq[c][r]=fisherExact.getCumlativeP(contig[0][0],contig[1][0], contig[0][1], contig[1][1]);
          }
         else
          {diseq[r][c]=calculateRSqrDisequilibrium(contig, rows, cols);
          diseq[c][r]=calculateDisequilibrium(contig, rows, cols);
          contigencyTable.setMatrix(contig);
          pDiseq[r][c]=n;
          if(this.rapidPermute)
            {pDiseq[c][r]=contigencyTable.calcRapidMonteCarloExactTest(numberOfPermutations);}
           else
            {pDiseq[c][r]=contigencyTable.calcMonteCarloExactTest(numberOfPermutations);}
          }
//        System.out.println("pDiseq["+r+"]["+c+"]="+pDiseq[r][c]);
        } //end of c
      } //end of r
  }

  double calculateDisequilibrium(int countAA, int countAa, int countaA, int countaa) {
      //this is the normalized D' is Weir Genetic Data Analysis II 1986 p120
      double freqR, freqC, freq, countR, countC, nonmissingSampleSize;
      nonmissingSampleSize=countAA+countAa+countaA+countaa;
      countR=countaa+countAa;
      countC=countaa+countaA;
      freqR=(nonmissingSampleSize-countR)/nonmissingSampleSize;
      freqC=(nonmissingSampleSize-countC)/nonmissingSampleSize;
      if((freqR==0)||(freqC==0)||(freqR==1)||(freqC==1)) return -999;
      freq=((double)countAA/nonmissingSampleSize)-(freqR*freqC);
      if(freq<0)
          {return freq/Math.max(-freqR*freqC,-(1-freqR)*(1-freqC));}
         else
          {return freq/Math.min((1-freqR)*freqC,(1-freqC)*freqR);}  //check these equations
  }

  double calculateRSqrDisequilibrium(int countAB, int countAb, int countaB, int countab) {
        //this is the Hill & Robertson measure as used in Awadella Science 1999 286:2524
      double freqA, freqB, rsqr, nonmissingSampleSize;
      nonmissingSampleSize=countAB+countAb+countaB+countab;

      freqA=(double)(countAB+countAb)/nonmissingSampleSize;
      freqB=(double)(countAB+countaB)/nonmissingSampleSize;

      //Through missing data & incomplete datasets some alleles can be fixed this returns missing value
      if((freqA==0)||(freqB==0)||(freqA==1)||(freqB==1))
        return Double.NaN;

      rsqr=((double)countAB/nonmissingSampleSize)*((double)countab/nonmissingSampleSize);
      rsqr-=((double)countaB/nonmissingSampleSize)*((double)countAb/nonmissingSampleSize);
      rsqr*=rsqr;
      rsqr/=freqA*(1-freqA)*freqB*(1-freqB);
      return rsqr;
  }

  double calculateDisequilibrium(int[][] contig, int rows, int cols) {
    //this is the D' approach used by Farnir 2000 Genome Research 10:220-227
    //this follow Hedrick 1987
      double Dij, Dmax, D_prime_ij, D_prime=0, pi, qj, nonmissingSampleSize=0;
      double[] p_margin=new double[rows];
      double[] q_margin=new double[cols];
      for(int i=0; i<rows; i++)
        {for(int j=0; j<cols; j++)
          {nonmissingSampleSize+=contig[i][j];
          p_margin[i]+=contig[i][j];
          q_margin[j]+=contig[i][j];
          }}
      for(int i=0; i<rows; i++)
        {//Through missing data & incomplete datasets some alleles can be fixed this return missing value
        if(p_margin[i]==0) continue;
        pi=p_margin[i]/nonmissingSampleSize;
        for(int j=0; j<cols; j++)
          {//Through missing data & incomplete datasets some alleles can be fixed this return missing value
          if(q_margin[j]==0) continue;
          qj=q_margin[j]/nonmissingSampleSize;
           Dij=(contig[i][j]/nonmissingSampleSize)-(pi*qj);
           if(Dij<0)
            {Dmax=Math.min(pi*qj,(1-pi)*(1-qj));}
           else
            {Dmax=Math.min(pi*(1-qj),(1-pi)*qj);}  //check these equations
           D_prime_ij=Dij/Dmax;
          D_prime+=(pi*qj*Math.abs(D_prime_ij));
          }
        }
      return D_prime;
  }

    double calculateRSqrDisequilibrium(int[][] contig, int rows, int cols) {
    //this is the D' approach used by Farnir 2000 Genome Research 10:220-227
    //this follows Hedrick 1987
    //but I have really made this up myself fusing Garnir with Hill
      double r_sqrsum=0, pi, qj;
      int countAB, countAb, countaB, countab, nonmissingSampleSize=0;
      int[] p_margin=new int[rows];
      int[] q_margin=new int[cols];
      for(int i=0; i<rows; i++)
        {for(int j=0; j<cols; j++)
          {nonmissingSampleSize+=contig[i][j];
          p_margin[i]+=contig[i][j];
          q_margin[j]+=contig[i][j];
          }}
      for(int i=0; i<rows; i++)
        {//Through missing data & incomplete datasets some alleles can be fixed this return missing value
        if(p_margin[i]==0) continue;
        pi=(double)p_margin[i]/(double)nonmissingSampleSize;
        for(int j=0; j<cols; j++)
          {//Through missing data & incomplete datasets some alleles can be fixed this return missing value
          if(q_margin[j]==0) continue;
          qj=(double)q_margin[j]/(double)nonmissingSampleSize;
          countAB=contig[i][j];
          countAb=p_margin[i]-countAB;
          countaB=q_margin[j]-countAB;
          countab=nonmissingSampleSize-countAB-countAb-countaB;
          r_sqrsum+=pi*qj*calculateRSqrDisequilibrium(countAB, countAb, countaB, countab);
          }
        }
      return r_sqrsum;
  }

  /** Returns P-value estimate for a given pair of sites.  If there were only 2 alleles
   *  at each locus, then the Fisher Exact P-value (one-tail) is returned.  If more states
   *  then the permutaed Monte Carlo test is used.
   *  @param r is site 1
   *  @param c is site 2
   *  @return P-value
   */
  public double getP(int r, int c) {
    if(r<=c) return pDiseq[r][c];
    else return pDiseq[c][r];
  }

  /** Get number of gametes included in LD calculations (after missing data was excluded)
   *  @param r is site 1
   *  @param c is site 2
   *  @return number of gametes
   *
   */
  public int getN(int r, int c) {
    if(c<=r) return (int)pDiseq[r][c];
    else return (int)pDiseq[c][r];
  }

  /** Returns D' estimate for a given pair of sites
   *  @param r is site 1
   *  @param c is site 2
   *  @return D'
   */
  public double getDPrime(int r, int c) {
    if(r<=c) return diseq[r][c];
    else return diseq[c][r];
  }
  /** Returns r^2 estimate for a given pair of sites
   *  @param r is site 1
   *  @param c is site 2
   *  @return D'
   */
  public double getRSqr(int r, int c) {
    if(c<=r) return diseq[r][c];
    else return diseq[c][r];
  }

  /**
   * Returns the counts of the sites in the alignment
   */
  public int getSiteCount() {
    return theAlignment.getSiteCount();
  }

  /**
   * Returns an annotated aligment if one was used for this LD
   * this could be used to access information of locus position
   */
  public AnnotationAlignment getAnnotatedAlignment() {
    return theAnnotationAlignment;
  }

  /** returns representation of the LD results as a string */
  public String toString() {
    StringWriter sw = new StringWriter();
    print(this, new PrintWriter(sw));
    return sw.toString();
  }

  /** print the LD to the PrintWrite */
  public void print(LinkageDisequilibrium ld, PrintWriter out) {
      if(annotated)
        {out.print("LocusName1\t Chromosome1\t ChromoPosition1\t ");
        out.print("Site1\t NumberOfStates1\t States1\t Frequency1\t ");
        out.print("LocusName2\t Chromosome2\t ChromoPosition2\t ");
        out.print("Site2\t NumberOfStates2\t States2\t Frequency2\t ");
        out.print("R^2\t DPrime\t pDiseq\t N");
        out.println();
        for(int r=0; r<theAlignment.getSiteCount(); r++)
          {String rState=getStatesForPrint(r);
          for(int c=0; c<=r; c++)
            {String cState=getStatesForPrint(c);
            out.print(theAnnotationAlignment.getLocusName(r)+"\t"+theAnnotationAlignment.getChromosome(r)+"\t"+theAnnotationAlignment.getChromosomePosition(r)+"\t");
            out.print(theAnnotationAlignment.getLocusPosition(r)+"\t"+stateVector[r].size()+"\t"+rState+"\t"+"NotDone"+"\t");
            out.print(theAnnotationAlignment.getLocusName(c)+"\t"+theAnnotationAlignment.getChromosome(c)+"\t"+theAnnotationAlignment.getChromosomePosition(c)+"\t");
            out.print(theAnnotationAlignment.getLocusPosition(c)+"\t"+stateVector[c].size()+"\t"+cState+"\t"+"NotDone"+"\t");
            out.print(ld.getRSqr(r,c)+"\t"+ld.getDPrime(r,c)+"\t"+ld.getP(r,c)+"\t"+ld.getN(r,c));
            out.println();
            }
          //System.out.println("r="+r);
          }
        }
      else
        {out.print("Site1\t NumberOfStates1\t States1\t Frequency1\t ");
        out.print("Site2\t NumberOfStates2\t States2\t Frequency2\t ");
        out.print("R^2\t DPrime\t pDiseq\t N");
        out.println();
        for(int r=0; r<theAlignment.getSiteCount(); r++)
          {String rState=getStatesForPrint(r);
          for(int c=0; c<=r; c++)
            {String cState=getStatesForPrint(c);
            out.print(r+"\t"+stateVector[r].size()+"\t"+rState+"\t"+"NotDone"+"\t");
            out.print(c+"\t"+stateVector[c].size()+"\t"+cState+"\t"+"NotDone"+"\t");
            out.print(ld.getRSqr(r,c)+"\t"+ld.getDPrime(r,c)+"\t"+ld.getP(r,c)+"\t"+ld.getN(r,c));
            out.println();
            }
          //System.out.println("r="+r);
          }
        }
      out.println();
      out.println("Taxa Included:");
      for (int i = 0; i < theAlignment.getSequenceCount(); i++) {
        out.println(theAlignment.getIdentifier(i).getName());
        }

  }


//This could be removed if the Datatype class had a toString method that chooses between outputting states and characters
  private String getStatesForPrint(int site) {
    String s="[";
    Character C;
    DataType dt;
    if(annotated)
      {dt=theAnnotationAlignment.getDataType(site);}
    else
      {dt=theAlignment.getDataType();}
    if(dt.getDescription().equals("Numeric"))
      {for(int i=0; i<stateVector[site].size(); i++)
        {C=(Character)stateVector[site].get(i);
        s+=dt.getState(C.charValue())+" ";
        }
      }
    else
      {for(int i=0; i<stateVector[site].size(); i++)
        {C=(Character)stateVector[site].get(i);
        s+=C.toString()+" ";
        }
      }
    s+="]";
    return s;
  }

     //Implementation of TableReport Interface
     /**
     * @return column names for the table
     */
    public Object[] getTableColumnNames() {
      String[] basicLabels={"Site1","NumberOfStates1","States1","Frequency1",
      "Site2","NumberOfStates2","States2","Frequency2","R^2","DPrime","pDiseq","N"};
      String[] annotatedLabels={"LocusName1","Chromosome1","ChromoPosition1","Site1",
      "NumberOfStates1","States1","Frequency1","LocusName2","Chromosome2","ChromoPosition2",
      "Site2","NumberOfStates2","States2","Frequency2","R^2","DPrime","pDiseq","N"};
      if(annotated)
        {return annotatedLabels;}
       else
        {return basicLabels;}
      }

    /**
     *@return data for the table
     */
    public Object[][] getTableData() {
      Object[][] data;
      java.text.NumberFormat nf=new java.text.DecimalFormat();
      nf.setMaximumFractionDigits(8);
      int i=0, labelOffset;
      int total=theAlignment.getSiteCount()*(theAlignment.getSiteCount()-1)/2;
      if(annotated)
        {data=new String[total][18];}
      else
        {data=new String[total][12];}
      for(int r=0; r<theAlignment.getSiteCount(); r++)
        {String rState=getStatesForPrint(r);
        for(int c=0; c<r; c++)
          {String cState=getStatesForPrint(c);
          labelOffset=0;
          if(annotated)
            {data[i][labelOffset++]=theAnnotationAlignment.getLocusName(r);
            data[i][labelOffset++]=""+theAnnotationAlignment.getChromosome(r);
            data[i][labelOffset++]=theAnnotationAlignment.getLocusName(r);
            data[i][labelOffset++]=""+theAnnotationAlignment.getLocusPosition(r);
            }
           else
            {data[i][labelOffset++]=""+r;}
          data[i][labelOffset++]=""+stateVector[r].size();
          data[i][labelOffset++]=""+rState;
          data[i][labelOffset++]="NotImplemented";
          if(annotated)
            {data[i][labelOffset++]=theAnnotationAlignment.getLocusName(c);
            data[i][labelOffset++]=""+theAnnotationAlignment.getChromosome(c);
            data[i][labelOffset++]=theAnnotationAlignment.getLocusName(c);
            data[i][labelOffset++]=""+theAnnotationAlignment.getLocusPosition(c);
            }
           else
            {data[i][labelOffset++]=""+c;}
          data[i][labelOffset++]=""+stateVector[c].size();
          data[i][labelOffset++]=""+cState;
          data[i][labelOffset++]="NotImplemented";
          data[i][labelOffset++]=nf.format(getRSqr(r,c));
          data[i][labelOffset++]=nf.format(getDPrime(r,c));
          data[i][labelOffset++]=nf.format(getP(r,c));
          data[i][labelOffset++]=""+getN(r,c);
          i++;
          }
        //System.out.println("r="+r);
        }
      return data;
      }

    /**
     * @return the title of the table
     */
    public String getTableTitle() {
      return "Linkage Disequilibrium";
    }
}

