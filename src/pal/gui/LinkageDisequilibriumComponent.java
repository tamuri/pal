// LinkageDisequilibriumComponent.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.gui;

import pal.alignment.*;

import java.awt.*;
import java.util.*;
import java.awt.print.*;
import java.text.*;

import pal.popgen.LinkageDisequilibrium;
/**
 * An AWT Component for displaying information on linkage disequilibrium.
 *
 * Nice schematics are produced if an annotation alignment is used to construct
 * LinkageDisequilibrium.  It can portray things both on the gene and chromosomal
 * scale.
 *
 *
 * @author Ed Buckler
 * @version $Id: LinkageDisequilibriumComponent.java
 */
public class LinkageDisequilibriumComponent extends Component implements Printable {
  public final static int P_VALUE = 0;
  public final static int DPRIME = 1;
  public final static int RSQUARE = 2;

  float minimumChromosomeLength=10;

  LinkageDisequilibrium theLD;
  AnnotationAlignment theAA;
  boolean includeBlockSchematic, chromosomalScale;

  BorderLayout borderLayout1 = new BorderLayout();
  int totalVariableSites, totalLoci, totalChromosomes, totalIntervals, totalBlocks;
  float[] startPos, endPos; //These are the relative positions of the polymorphisms
  float[] blockBeginPos, blockEndPos;
  String[] blockNames;
  int[] xPos, yPos, xEndPos;  //these hold positions of the upper left corners for each site
  int[] blockBeginX, blockEndX;//These are the absolute positions of the genes & chromosomes
  int ih, iw;
  float totalUnits;
  float[] blockStart, blockEnd;
      //this will range from 0 to 1
  String upperLabel, lowerLabel;
  double[][] diseq;
  Color theColor=new Color(0,0,0);
  int distanceBetweenGraphAndGene=40;
  int hoff=70, h2off=70, voff=20;
    //hoff is on the left side for site labels
    //h2off is on the right side for legends
  boolean probability=true, upperProb=false, lowerProb=true;
//  boolean genesOrChromo=true;  //true if display genes , false if display chromosomes


  public LinkageDisequilibriumComponent(LinkageDisequilibrium theLD, boolean includeBlockSchematic, boolean chromosomalScale) {
    this.theLD=theLD;
    theAA=theLD.getAnnotatedAlignment();
    this.includeBlockSchematic=includeBlockSchematic;
    this.chromosomalScale=chromosomalScale;
    this.diseq=new double[theLD.getSiteCount()][theLD.getSiteCount()];
    setUpperCorner(RSQUARE);
    setLowerCorner(P_VALUE);
    totalVariableSites=theLD.getSiteCount();
    if(theAA!=null)
      {countGenesAndChromosomes();
      calculateStartAndEndPositions();
      }
     else
      {includeBlockSchematic=false;}
    xPos=new int[theLD.getSiteCount()+1];
    yPos=new int[theLD.getSiteCount()+1];
    xEndPos=new int[theLD.getSiteCount()+1];
    try  {
      jbInit();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  /**
   * This determines what is displayed in the lower left corner.
   * Options are: P_VALUE, DPRIME, and RSQUARE
   */
  public void setLowerCorner(int ldMeasure) {
    for(int r=0; r<theLD.getSiteCount(); r++)
      {for(int c=r; c<theLD.getSiteCount(); c++)
        {switch (ldMeasure) {
          case P_VALUE :  {diseq[r][c]=theLD.getP(r,c); lowerLabel="P value"; break;}
          case DPRIME : {diseq[r][c]=theLD.getDPrime(r,c); lowerLabel="D'";break;}
          case RSQUARE :  {diseq[r][c]=theLD.getRSqr(r,c); lowerLabel="R^2";break;}
          }
        }
      }
    lowerProb=(ldMeasure==P_VALUE)?true:false;
  }

  /**
   * This determines what is displayed in the upper right corner.
   * Options are: P_VALUE, DPRIME, and RSQUARE
   */
  public void setUpperCorner(int ldMeasure) {
    for(int c=0; c<theLD.getSiteCount(); c++)
      {for(int r=c; r<theLD.getSiteCount(); r++)
        {switch (ldMeasure) {
          case P_VALUE :  {diseq[r][c]=theLD.getP(r,c); upperLabel="P value"; break;}
          case DPRIME : {diseq[r][c]=theLD.getDPrime(r,c); upperLabel="D'"; break;}
          case RSQUARE :  {diseq[r][c]=theLD.getRSqr(r,c); upperLabel="R^2"; break;}
          }
        }
      }
   upperProb=(ldMeasure==P_VALUE)?true:false;
  }

  /**
   * This sets the scale of the LD view, either sites are organized by chromosomes if
   * chromosomalScale is true, otherwise they are organized by genes
   */
  public void setScaleOfView(boolean chromosomalScale) {
    this.chromosomalScale=chromosomalScale;
    countGenesAndChromosomes();
    calculateStartAndEndPositions();
  }

  /**
   * This sets whether a schematic is displayed.  If true a schematic of genes or
   * chromosomes is displayed, otherwise no schematic is displayed
   */
  public void setShowSchematic(boolean includeBlockSchematic) {
    if(theAA==null) return;  //if there is no annotation don't produce the schematic
    this.includeBlockSchematic=includeBlockSchematic;
    countGenesAndChromosomes();
    calculateStartAndEndPositions();
  }

  /**
   * this counts the number of separate blocks
   * if on chromosomal scale then chromosomes are counted otherwise only loci are counted
   * It then deteremines the total span in terms of cM or bases depending on scale
   */
  private void countGenesAndChromosomes () {
    totalLoci=totalChromosomes=0;
    int currc=-1999;
    String currLocus="";
    for(int r=0; r<totalVariableSites; r++)        //sites and chromosomes need to be sorted
      {if(theAA.getChromosome(r)!=currc)
        {totalChromosomes++; currc=theAA.getChromosome(r);}
      if(!currLocus.equals(theAA.getLocusName(r)))
        {totalLoci++; currLocus=theAA.getLocusName(r);}
      }
    //the number of separate totalBlocks
    totalBlocks=(chromosomalScale)?totalChromosomes:totalLoci;
    blockStart=new float[totalBlocks];
    blockEnd=new float[totalBlocks];
    blockNames=new String[totalBlocks];

    for(int i=0; i<totalChromosomes; i++) {blockStart[i]=999999; blockEnd[i]=-999999;}
    int c=-1;
    currLocus="";
    currc=-1999;
    for(int r=0; r<totalVariableSites; r++)
      {if(chromosomalScale)
        {if(theAA.getChromosome(r)!=currc)
            {c++; currc=theAA.getChromosome(r); blockNames[c]="Chr."+currc;}
        if(blockStart[c]>theAA.getChromosomePosition(r)) blockStart[c]=theAA.getChromosomePosition(r);
        if(blockEnd[c]<theAA.getChromosomePosition(r)) blockEnd[c]=theAA.getChromosomePosition(r);
        }
      else
        {if(!currLocus.equals(theAA.getLocusName(r)))
            {c++; currLocus=theAA.getLocusName(r); blockNames[c]=currLocus;}
        if(blockStart[c]>theAA.getLocusPosition(r)) blockStart[c]=theAA.getLocusPosition(r);
        if(blockEnd[c]<theAA.getLocusPosition(r)) blockEnd[c]=theAA.getLocusPosition(r);
        }
      }
    totalUnits=0.5f;
    for(int i=0; i<totalBlocks; i++)
      {if((chromosomalScale)&&((blockEnd[i]-blockStart[i])<minimumChromosomeLength))
        {blockEnd[i]=blockStart[i]+minimumChromosomeLength;}
      else if((blockEnd[i]-blockStart[i])<1)
        {blockEnd[i]=blockStart[i]+1;}
      totalUnits+=blockEnd[i]-blockStart[i];}//+1;}
    System.out.println("Got here");
  }

   /**
   * this determines to relative positions of the sites and cartoons (everything ranges from 0..1)
   *
   */
  void calculateStartAndEndPositions() {
    //This will determine were all the relative positions of the sites go
     float proportionPerPolymorphism,proportionPerUnit=0.0f;
    if(includeBlockSchematic)
      {totalIntervals=totalVariableSites+totalBlocks-1;
      proportionPerPolymorphism=1/(float)totalIntervals;
      proportionPerUnit=(proportionPerPolymorphism*totalVariableSites)/totalUnits;
      blockBeginPos=new float[totalBlocks];    //These hold the start and end points of the genes
      blockEndPos=new float[totalBlocks];
      }
    else
      {totalIntervals=totalVariableSites;
      proportionPerPolymorphism=1/(float)totalIntervals;
      }
    startPos=new float[totalVariableSites];
    endPos=new float[totalVariableSites];

//    int r,b=0,currg=-1999,currc=-1999;
    startPos[0]=0;
    endPos[0]=0;
    float currStartBase=0, currEndBase=0, geneToChromosomeSpace=0;
    for(int r=0; r<totalVariableSites; r++)
      {if((chromosomalScale)&&(r>0)&&(includeBlockSchematic)&&(theAA.getChromosome(r)!=theAA.getChromosome(r-1))) //transition between chromosomes if on chromosomal scale
        {currStartBase+=proportionPerPolymorphism;}
      if((!chromosomalScale)&&(r>0)&&(includeBlockSchematic)&&(!theAA.getLocusName(r).equals(theAA.getLocusName(r-1))))   //transition between loci if not at chromosomal scale
        {currStartBase+=proportionPerPolymorphism;}
      startPos[r]=currStartBase;
      currStartBase+=proportionPerPolymorphism;
      }  //end of going through sites
    if(includeBlockSchematic)
      {currStartBase=0;
      for(int b=0; b<totalBlocks; b++)
        {blockBeginPos[b]=currStartBase;
        blockEndPos[b]=blockBeginPos[b]+((blockEnd[b]-blockStart[b])*proportionPerUnit);
        currStartBase=blockEndPos[b]+proportionPerPolymorphism;
        }
      int currB=0;
      if(chromosomalScale)
        {endPos[0]=blockBeginPos[0]+((theAA.getChromosomePosition(0)-blockStart[0])*proportionPerUnit);}
        else
        {endPos[0]=blockBeginPos[0]+((theAA.getLocusPosition(0)-blockStart[0])*proportionPerUnit);}
      for(int r=1; r<totalVariableSites; r++)
        {if(chromosomalScale)
          {if(theAA.getChromosome(r)!=theAA.getChromosome(r-1)) currB++;
          endPos[r]=blockBeginPos[currB]+((theAA.getChromosomePosition(r)-blockStart[currB])*proportionPerUnit);}
          else
          {if(!theAA.getLocusName(r).equals(theAA.getLocusName(r-1))) currB++;
          endPos[r]=blockBeginPos[currB]+((theAA.getLocusPosition(r)-blockStart[currB])*proportionPerUnit);}
        }
      }
//    blockBeginPos[b]=currEndBase;
 //   blockEndPos[b]=endPos[r-1];
  }

  private void jbInit() throws Exception {
    this.setBackground(Color.red);
    this.setSize(400,400);
//    this.setPreferredSize(new Dimension(400, 400));
//    this.setLayout(borderLayout1);
  }



  private Color getMagnitudeColor(int r, int c) {
    if(r==c) {return theColor.getHSBColor(0.999f,(float)diseq[r][c],1f);}
    if(diseq[r][c]>0.999) {return theColor.getHSBColor(1f,1f,1f);}
    if(diseq[r][c]<-998.0) {return theColor.lightGray;}
    return theColor.getHSBColor((float)diseq[r][c],(float)diseq[r][c],1f);
  }

  private Color getProbabilityColor(int r, int c) {
    double p1=0.01, p2=0.001, p3=0.0001;
    if(diseq[r][c]<-998.0) {return theColor.lightGray;}
    if(diseq[r][c]>p1) {return theColor.white;}
    if(diseq[r][c]>p2) {return theColor.blue;}
    if(diseq[r][c]>p3) {return theColor.green;}
    return theColor.red;
  }

  private void addPolymorphismLabels(Graphics g, int ih) {
    int gr=0;
    String s;
    g.setFont(new java.awt.Font("Dialog", 0, 9));
    g.setColor(theColor.black);
    for(int r=0; r<totalVariableSites; r++)
      {if(chromosomalScale)
        {s=theAA.getChromosome(r)+"c"+Math.round(theAA.getChromosomePosition(r));}
       else
        {s=theAA.getLocusName(r)+"s"+theAA.getLocusPosition(r);}
      g.drawString(s,4,yPos[r]+ih-1);
      }
  }

  /**
   * This converts all those relative positions to real coordinates based on the size of the component
   */
  private void calculateCoordinates(Graphics gr) {
    Dimension d=this.getSize();
    float iwf, ihf, xSize, ySize;
    ySize=d.height-voff-distanceBetweenGraphAndGene;
    ihf=ySize/(float)totalIntervals;
    xSize=d.width-hoff-h2off;
    iwf=xSize/(float)totalIntervals;
    ih=Math.round(ihf);
    iw=Math.round(iwf);
    for(int r=0; r<totalVariableSites; r++)
      {xPos[r]=(int)((startPos[r]*xSize)+(float)hoff);
      yPos[r]=(int)((startPos[r]*ySize)+(float)voff);
      //xEndPos[r]=Math.round((endPos[r]*xSize)+hoff);
      }  //end of going through sites
    xPos[totalVariableSites]=(int)d.width-h2off;
    yPos[totalVariableSites]=(int)ySize+voff;
    if(includeBlockSchematic)
      {for(int r=0; r<totalVariableSites; r++)
        {xEndPos[r]=Math.round((endPos[r]*xSize)+hoff);}  //end of going through sites
      blockBeginX=new int[totalBlocks];
      blockEndX=new int[totalBlocks];
      for(int b=0; b<totalBlocks; b++)
        {blockBeginX[b]=Math.round((blockBeginPos[b]*xSize)+hoff);
        blockEndX[b]=Math.round((blockEndPos[b]*xSize)+hoff);
        }
      }
  }

  protected void paintComponent(Graphics g) {
    if(diseq==null) return;
//    super.paintComponent(g);
    int hue;
    Dimension d=this.getSize();
    calculateCoordinates(g);
    g.setColor(theColor.white);
    g.fillRect(0,0, d.width, d.height);
  System.out.println("UpperProb="+upperProb+"  LowerProb="+lowerProb);
    g.setColor(theColor.darkGray);
    g.fillRect(xPos[0], yPos[0], xPos[totalVariableSites]-xPos[0], yPos[totalVariableSites]-yPos[0]+2);
    for(int r=0; r<totalVariableSites; r++)
      {for(int c=0; c<totalVariableSites; c++)
        {if(((c<r)&&(upperProb==true))||((c>r)&&(lowerProb==true))) {g.setColor(getProbabilityColor(r,c));}
          else if(r==c) {g.setColor(theColor.black);}
          else {g.setColor(getMagnitudeColor(r,c));}
        g.fillRect(xPos[r], yPos[c], iw+1, ih+1);
        }
      }
    g.setColor(theColor.darkGray);
    for(int r=0; r<totalVariableSites; r++)
      {g.drawLine(xPos[r], yPos[0], xPos[r], yPos[totalVariableSites]);
      g.drawLine(xPos[0], yPos[r], xPos[totalVariableSites], yPos[r]);
      }
    addPolymorphismLabels(g,ih);
    if(includeBlockSchematic) addGenePicture(g,ih, iw);
    addLegend(g);
  }

  public void paint(Graphics g) {
    paintComponent(g);
  }

  private void addLegend(Graphics g) {
    Dimension d=this.getSize();
    int localX=d.width-h2off+10;
    int mid=d.height/2;
    g.setColor(Color.black);
    g.drawString("Upper "+upperLabel,localX,10);
    addLegendGraph(g, upperProb, localX, 20, mid-10);
    g.setColor(Color.black);
    g.drawString("Lower "+lowerLabel,localX,mid+10);
    addLegendGraph(g, lowerProb, localX, mid+20, d.height-10);
  }

  private void addLegendGraph(Graphics g, boolean prob, int xStart, int yStart, int yEnd) {
    DecimalFormat dF; //=new DecimalFormat("0.0000");
    int yInc, currY=yStart;
    int barWidth=10;
    if(prob) {
      yInc=(yEnd-yStart)/4;
      g.setColor(theColor.white);
      g.fillRect(xStart, currY, barWidth, yInc);
      g.setColor(Color.black);
      g.drawRect(xStart, currY, barWidth, yInc);
      g.drawString(">0.01",xStart+barWidth+5,currY+10);
      currY+=yInc;
      g.setColor(theColor.blue);
      g.fillRect(xStart, currY, barWidth, yInc);
      g.setColor(Color.black);
      g.drawRect(xStart, currY, barWidth, yInc);
      g.drawString("<0.01",xStart+barWidth+5,currY+10);
      currY+=yInc;
      g.setColor(theColor.green);
      g.fillRect(xStart, currY, barWidth, yInc);
      g.setColor(Color.black);
      g.drawRect(xStart, currY, barWidth, yInc);
      g.drawString("<0.001",xStart+barWidth+5,currY+10);
      currY+=yInc;
      g.setColor(theColor.red);
      g.fillRect(xStart, currY, barWidth, yInc);
      g.setColor(Color.black);
      g.drawRect(xStart, currY, barWidth, yInc);
      g.drawString("<0.0001",xStart+barWidth+5,currY+10);
      }
    else
      {yInc=(yEnd-yStart)/11;
      dF=new DecimalFormat("0.00");
      for(float d=1.0001f; d>=0; d-=0.1)
        {g.setColor(theColor.getHSBColor(d,d,1f));
        g.fillRect(xStart, currY, barWidth, yInc);
        g.setColor(Color.black);
        g.drawRect(xStart, currY, barWidth, yInc);
        g.drawString(dF.format(d),xStart+barWidth+5,currY+10);
        currY+=yInc;
        }
      }
  }

  private void addGenePicture(Graphics g, int ih, int iw) {
      //This will add the gene picture to the left of the polymorphisms
    int yOfLinkBlock, yOfGene, yOfGeneLabel;//,totalBases,spacer, cpos;
    int halfIW=iw/2;
//    MultiAlleleSiteCharacteristic theMSC, lastMSC;
    Dimension d=this.getSize();
    yOfLinkBlock=yPos[totalVariableSites];
    yOfGene=yOfLinkBlock+(distanceBetweenGraphAndGene/2);
    yOfGeneLabel=yOfLinkBlock+(int)(0.8f*(float)distanceBetweenGraphAndGene);

    for(int r=0; r<totalVariableSites; r++)
      {g.drawLine(xPos[r]+halfIW,yOfLinkBlock,xEndPos[r],yOfGene);
      }  //end of going through sites
    g.setColor(theColor.blue);
    for(int b=0; b<totalBlocks; b++)
      {g.drawLine(blockBeginX[b],yOfGene,blockEndX[b],yOfGene);
       g.drawLine(blockBeginX[b],yOfGene+1,blockEndX[b],yOfGene+1);
       g.drawString(blockNames[b],blockBeginX[b],yOfGeneLabel);
      }
  }

  /*
  public void changeMatrix(double[][] diseq, boolean upperProb, boolean lowerProb, String upperLabel, String lowerLabel) {
    this.diseq=diseq;
    this.upperProb=upperProb;
    this.lowerProb=lowerProb;
    this.upperLabel=upperLabel;
    this.lowerLabel=lowerLabel;
    this.repaint();
  }
*/
  public int print(Graphics g, PageFormat pf, int pageIndex) {
    if (pageIndex != 0) return NO_SUCH_PAGE;
    Graphics2D g2 = (Graphics2D)g;
    g2.translate(pf.getImageableX(), pf.getImageableY());
    double pageHeight = pf.getImageableHeight();
    double pageWidth = pf.getImageableWidth();
    double tableWidth = (double) this.getWidth();
    double tableHeight = (double) this.getHeight();
    double scaleW =  pageWidth / tableWidth;
    double scaleH =  pageHeight/tableHeight;
    double maxScale=(scaleW>scaleH)?scaleH:scaleW;
//    System.out.println(scaleW+"=W  H="+scaleH+"   maxScale="+maxScale);
    g2.scale(maxScale,maxScale);
//    g2.scale(scaleW,scaleH);
    this.paint(g2);
    return PAGE_EXISTS;
  }
/*
  public void sendToPrinter() {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(this);
        if (printJob.printDialog()) {
            try {
                printJob.print();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
  }
*/
}