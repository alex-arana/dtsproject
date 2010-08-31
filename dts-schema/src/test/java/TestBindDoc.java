/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
//import org.dataminx.schemas.dts.x2009.x07.jsdl.CredentialKeyPointerDocument;
//import org.dataminx.schemas.dts.x2009.x07.jsdl.CredentialKeyPointerType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.proposal.dmi.schemas.dts.x2010.dmiCommon.CopyType;
//import org.proposal.dmi.schemas.dts.x2010.dmiCommon.CredentialsType;
import org.proposal.dmi.schemas.dts.x2010.dmiCommon.DataCopyActivityDocument;
import org.proposal.dmi.schemas.dts.x2010.dmiCommon.DataLocationsType;
import org.proposal.dmi.schemas.dts.x2010.dmiCommon.MyProxyTokenType;
import static org.junit.Assert.*;

/**
 * Test binding our example documents.
 *
 * @author David Meredith
 */
public class TestBindDoc {

    public TestBindDoc() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

   
    @Test
    public void testValidateDoc1() throws Exception {
        File f = new File(this.getClass().getResource("test1.xml").toURI());
        assertTrue(f.exists());
        DataCopyActivityDocument doc = DataCopyActivityDocument.Factory.parse(f);
        assertTrue(doc.validate());
        assertTrue(doc.getDataCopyActivity().getCopyArray().length == 2);
    }

    @Test
    public void testValidateDoc2() throws Exception {
        File f = new File(this.getClass().getResource("test2.xml").toURI());
        assertTrue(f.exists());
        DataCopyActivityDocument doc = DataCopyActivityDocument.Factory.parse(f);
        assertTrue(doc.validate());
        System.out.println(doc.toString());
        CopyType[] copies = doc.getDataCopyActivity().getCopyArray();
        assertTrue(copies.length == 1);
        CopyType copy1 = copies[0];
        DataLocationsType source = copy1.getSource();

        /*CredentialsType creds = source.getData().getCredentials();
        assertNotNull(creds);

        XmlCursor c = source.getData().getCredentials().newCursor();
        assertTrue(c.toFirstChild());
        XmlObject obj = c.getObject();
        assertTrue(obj instanceof MyProxyTokenType); 
        MyProxyTokenType mpt = (MyProxyTokenType)obj;

        //assertTrue(source.getData().getCredentials().isSetMyProxyToken());
        assertTrue(mpt.getMyProxyPassword().equals("password"));

        // unset the token in the document and assert !
        c.removeXml();
        System.out.println(doc.toString());
        assertTrue(c.getObject() == null);

        // replace the token with a credential pointer.
        final CredentialKeyPointerDocument credentialKeyPointer = CredentialKeyPointerDocument.Factory.newInstance();
        credentialKeyPointer.setCredentialKeyPointer("credentialUUID");
        creds.set(credentialKeyPointer);
        assertTrue(doc.validate());
        System.out.println(doc.toString());

        c.dispose();
        c = source.getData().getCredentials().newCursor();
     
        assertTrue(c.toFirstChild());
        assertNotNull(c.getObject());
        assertTrue(c.getObject() instanceof CredentialKeyPointerType);
        CredentialKeyPointerType pointer = (CredentialKeyPointerType)c.getObject();
        assertTrue(pointer.getStringValue().equals("credentialUUID")); 
        c.dispose();
  */
    }
}