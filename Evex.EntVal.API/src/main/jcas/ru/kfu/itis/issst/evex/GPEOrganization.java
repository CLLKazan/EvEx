

/* First created by JCasGen Thu Jul 03 20:13:04 MSD 2014 */
package ru.kfu.itis.issst.evex;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Thu Jul 03 20:13:04 MSD 2014
 * XML source: src/main/resources/ru/kfu/itis/issst/evex/entval/entval-ts.xml
 * @generated */
public class GPEOrganization extends GPE {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(GPEOrganization.class);
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
  protected GPEOrganization() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public GPEOrganization(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public GPEOrganization(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public GPEOrganization(JCas jcas, int begin, int end) {
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
     
}

    