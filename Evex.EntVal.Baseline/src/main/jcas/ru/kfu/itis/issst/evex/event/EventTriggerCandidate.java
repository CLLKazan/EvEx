

/* First created by JCasGen Tue Dec 23 13:04:06 MSK 2014 */
package ru.kfu.itis.issst.evex.event;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Tue Dec 23 13:04:06 MSK 2014
 * XML source: src/main/resources/ru/kfu/itis/issst/evex/util/evex-util-ts.xml
 * @generated */
public class EventTriggerCandidate extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(EventTriggerCandidate.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected EventTriggerCandidate() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public EventTriggerCandidate(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public EventTriggerCandidate(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public EventTriggerCandidate(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: token

  /** getter for token - gets 
   * @generated */
  public Annotation getToken() {
    if (EventTriggerCandidate_Type.featOkTst && ((EventTriggerCandidate_Type)jcasType).casFeat_token == null)
      jcasType.jcas.throwFeatMissing("token", "ru.kfu.itis.issst.evex.event.EventTriggerCandidate");
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((EventTriggerCandidate_Type)jcasType).casFeatCode_token)));}
    
  /** setter for token - sets  
   * @generated */
  public void setToken(Annotation v) {
    if (EventTriggerCandidate_Type.featOkTst && ((EventTriggerCandidate_Type)jcasType).casFeat_token == null)
      jcasType.jcas.throwFeatMissing("token", "ru.kfu.itis.issst.evex.event.EventTriggerCandidate");
    jcasType.ll_cas.ll_setRefValue(addr, ((EventTriggerCandidate_Type)jcasType).casFeatCode_token, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    