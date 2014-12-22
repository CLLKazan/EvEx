
/* First created by JCasGen Mon Dec 22 20:52:17 MSK 2014 */
package ru.kfu.itis.issst.evex.util;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Mon Dec 22 20:52:17 MSK 2014
 * @generated */
public class SimpleWord_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SimpleWord_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SimpleWord_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SimpleWord(addr, SimpleWord_Type.this);
  			   SimpleWord_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SimpleWord(addr, SimpleWord_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = SimpleWord.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("ru.kfu.itis.issst.evex.util.SimpleWord");
 
  /** @generated */
  final Feature casFeat_token;
  /** @generated */
  final int     casFeatCode_token;
  /** @generated */ 
  public int getToken(int addr) {
        if (featOkTst && casFeat_token == null)
      jcas.throwFeatMissing("token", "ru.kfu.itis.issst.evex.util.SimpleWord");
    return ll_cas.ll_getRefValue(addr, casFeatCode_token);
  }
  /** @generated */    
  public void setToken(int addr, int v) {
        if (featOkTst && casFeat_token == null)
      jcas.throwFeatMissing("token", "ru.kfu.itis.issst.evex.util.SimpleWord");
    ll_cas.ll_setRefValue(addr, casFeatCode_token, v);}
    
  
 
  /** @generated */
  final Feature casFeat_lemma;
  /** @generated */
  final int     casFeatCode_lemma;
  /** @generated */ 
  public String getLemma(int addr) {
        if (featOkTst && casFeat_lemma == null)
      jcas.throwFeatMissing("lemma", "ru.kfu.itis.issst.evex.util.SimpleWord");
    return ll_cas.ll_getStringValue(addr, casFeatCode_lemma);
  }
  /** @generated */    
  public void setLemma(int addr, String v) {
        if (featOkTst && casFeat_lemma == null)
      jcas.throwFeatMissing("lemma", "ru.kfu.itis.issst.evex.util.SimpleWord");
    ll_cas.ll_setStringValue(addr, casFeatCode_lemma, v);}
    
  
 
  /** @generated */
  final Feature casFeat_grammems;
  /** @generated */
  final int     casFeatCode_grammems;
  /** @generated */ 
  public int getGrammems(int addr) {
        if (featOkTst && casFeat_grammems == null)
      jcas.throwFeatMissing("grammems", "ru.kfu.itis.issst.evex.util.SimpleWord");
    return ll_cas.ll_getRefValue(addr, casFeatCode_grammems);
  }
  /** @generated */    
  public void setGrammems(int addr, int v) {
        if (featOkTst && casFeat_grammems == null)
      jcas.throwFeatMissing("grammems", "ru.kfu.itis.issst.evex.util.SimpleWord");
    ll_cas.ll_setRefValue(addr, casFeatCode_grammems, v);}
    
   /** @generated */
  public String getGrammems(int addr, int i) {
        if (featOkTst && casFeat_grammems == null)
      jcas.throwFeatMissing("grammems", "ru.kfu.itis.issst.evex.util.SimpleWord");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_grammems), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_grammems), i);
	return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_grammems), i);
  }
   
  /** @generated */ 
  public void setGrammems(int addr, int i, String v) {
        if (featOkTst && casFeat_grammems == null)
      jcas.throwFeatMissing("grammems", "ru.kfu.itis.issst.evex.util.SimpleWord");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_grammems), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_grammems), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_grammems), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public SimpleWord_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_token = jcas.getRequiredFeatureDE(casType, "token", "uima.tcas.Annotation", featOkTst);
    casFeatCode_token  = (null == casFeat_token) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_token).getCode();

 
    casFeat_lemma = jcas.getRequiredFeatureDE(casType, "lemma", "uima.cas.String", featOkTst);
    casFeatCode_lemma  = (null == casFeat_lemma) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_lemma).getCode();

 
    casFeat_grammems = jcas.getRequiredFeatureDE(casType, "grammems", "uima.cas.StringArray", featOkTst);
    casFeatCode_grammems  = (null == casFeat_grammems) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_grammems).getCode();

  }
}



    