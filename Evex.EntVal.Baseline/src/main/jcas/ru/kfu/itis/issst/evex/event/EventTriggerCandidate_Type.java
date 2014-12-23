
/* First created by JCasGen Tue Dec 23 13:04:06 MSK 2014 */
package ru.kfu.itis.issst.evex.event;

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
 * Updated by JCasGen Tue Dec 23 13:04:06 MSK 2014
 * @generated */
public class EventTriggerCandidate_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (EventTriggerCandidate_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = EventTriggerCandidate_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new EventTriggerCandidate(addr, EventTriggerCandidate_Type.this);
  			   EventTriggerCandidate_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new EventTriggerCandidate(addr, EventTriggerCandidate_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = EventTriggerCandidate.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("ru.kfu.itis.issst.evex.event.EventTriggerCandidate");
 
  /** @generated */
  final Feature casFeat_token;
  /** @generated */
  final int     casFeatCode_token;
  /** @generated */ 
  public int getToken(int addr) {
        if (featOkTst && casFeat_token == null)
      jcas.throwFeatMissing("token", "ru.kfu.itis.issst.evex.event.EventTriggerCandidate");
    return ll_cas.ll_getRefValue(addr, casFeatCode_token);
  }
  /** @generated */    
  public void setToken(int addr, int v) {
        if (featOkTst && casFeat_token == null)
      jcas.throwFeatMissing("token", "ru.kfu.itis.issst.evex.event.EventTriggerCandidate");
    ll_cas.ll_setRefValue(addr, casFeatCode_token, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public EventTriggerCandidate_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_token = jcas.getRequiredFeatureDE(casType, "token", "uima.tcas.Annotation", featOkTst);
    casFeatCode_token  = (null == casFeat_token) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_token).getCode();

  }
}



    