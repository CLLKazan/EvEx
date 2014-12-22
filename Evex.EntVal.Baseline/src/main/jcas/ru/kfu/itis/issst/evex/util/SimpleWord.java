

/* First created by JCasGen Mon Dec 22 20:52:17 MSK 2014 */
package ru.kfu.itis.issst.evex.util;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.cas.StringArray;


/** 
 * Updated by JCasGen Mon Dec 22 20:52:17 MSK 2014
 * XML source: src/main/resources/ru/kfu/itis/issst/evex/util/evex-util-ts.xml
 * @generated */
public class SimpleWord extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(SimpleWord.class);
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
  protected SimpleWord() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SimpleWord(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SimpleWord(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SimpleWord(JCas jcas, int begin, int end) {
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
    if (SimpleWord_Type.featOkTst && ((SimpleWord_Type)jcasType).casFeat_token == null)
      jcasType.jcas.throwFeatMissing("token", "ru.kfu.itis.issst.evex.util.SimpleWord");
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SimpleWord_Type)jcasType).casFeatCode_token)));}
    
  /** setter for token - sets  
   * @generated */
  public void setToken(Annotation v) {
    if (SimpleWord_Type.featOkTst && ((SimpleWord_Type)jcasType).casFeat_token == null)
      jcasType.jcas.throwFeatMissing("token", "ru.kfu.itis.issst.evex.util.SimpleWord");
    jcasType.ll_cas.ll_setRefValue(addr, ((SimpleWord_Type)jcasType).casFeatCode_token, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: lemma

  /** getter for lemma - gets 
   * @generated */
  public String getLemma() {
    if (SimpleWord_Type.featOkTst && ((SimpleWord_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "ru.kfu.itis.issst.evex.util.SimpleWord");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SimpleWord_Type)jcasType).casFeatCode_lemma);}
    
  /** setter for lemma - sets  
   * @generated */
  public void setLemma(String v) {
    if (SimpleWord_Type.featOkTst && ((SimpleWord_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "ru.kfu.itis.issst.evex.util.SimpleWord");
    jcasType.ll_cas.ll_setStringValue(addr, ((SimpleWord_Type)jcasType).casFeatCode_lemma, v);}    
   
    
  //*--------------*
  //* Feature: grammems

  /** getter for grammems - gets 
   * @generated */
  public StringArray getGrammems() {
    if (SimpleWord_Type.featOkTst && ((SimpleWord_Type)jcasType).casFeat_grammems == null)
      jcasType.jcas.throwFeatMissing("grammems", "ru.kfu.itis.issst.evex.util.SimpleWord");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SimpleWord_Type)jcasType).casFeatCode_grammems)));}
    
  /** setter for grammems - sets  
   * @generated */
  public void setGrammems(StringArray v) {
    if (SimpleWord_Type.featOkTst && ((SimpleWord_Type)jcasType).casFeat_grammems == null)
      jcasType.jcas.throwFeatMissing("grammems", "ru.kfu.itis.issst.evex.util.SimpleWord");
    jcasType.ll_cas.ll_setRefValue(addr, ((SimpleWord_Type)jcasType).casFeatCode_grammems, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for grammems - gets an indexed value - 
   * @generated */
  public String getGrammems(int i) {
    if (SimpleWord_Type.featOkTst && ((SimpleWord_Type)jcasType).casFeat_grammems == null)
      jcasType.jcas.throwFeatMissing("grammems", "ru.kfu.itis.issst.evex.util.SimpleWord");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((SimpleWord_Type)jcasType).casFeatCode_grammems), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((SimpleWord_Type)jcasType).casFeatCode_grammems), i);}

  /** indexed setter for grammems - sets an indexed value - 
   * @generated */
  public void setGrammems(int i, String v) { 
    if (SimpleWord_Type.featOkTst && ((SimpleWord_Type)jcasType).casFeat_grammems == null)
      jcasType.jcas.throwFeatMissing("grammems", "ru.kfu.itis.issst.evex.util.SimpleWord");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((SimpleWord_Type)jcasType).casFeatCode_grammems), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((SimpleWord_Type)jcasType).casFeatCode_grammems), i, v);}
  }

    