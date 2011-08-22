package org.bouncycastle.asn1.test;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.x509.qualified.SemanticsInformation;
import org.bouncycastle.util.test.SimpleTest;

public class SemanticsInformationUnitTest 
    extends SimpleTest
{
    public String getName()
    {
        return "SemanticsInformation";
    }

    public void performTest() 
        throws Exception
    {
        DERObjectIdentifier  statementId = new DERObjectIdentifier("1.1");
        SemanticsInformation mv = new SemanticsInformation(statementId);

        checkConstruction(mv, statementId, null);
        
        GeneralName[] names = new GeneralName[2];
        
        names[0] = new GeneralName(GeneralName.rfc822Name, "test@test.org");
        names[1] = new GeneralName(new X509Name("cn=test"));
        
        mv = new SemanticsInformation(statementId, names);

        checkConstruction(mv, statementId, names);
        
        mv = new SemanticsInformation(names);

        checkConstruction(mv, null, names);
        
        mv = SemanticsInformation.getInstance(null);
        
        if (mv != null)
        {
            fail("null getInstance() failed.");
        }
        
        try
        {
            SemanticsInformation.getInstance(new Object());
            
            fail("getInstance() failed to detect bad object.");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
        
        try
        {
            ASN1EncodableVector v = new ASN1EncodableVector();
            
            new SemanticsInformation(new DERSequence(v));
            
            fail("constructor failed to detect empty sequence.");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    private void checkConstruction(
        SemanticsInformation mv,
        DERObjectIdentifier  semanticsIdentifier,
        GeneralName[]        names)
        throws Exception
    {
        checkStatement(mv, semanticsIdentifier, names);
        
        mv = SemanticsInformation.getInstance(mv);
        
        checkStatement(mv, semanticsIdentifier, names);
        
        ASN1InputStream aIn = new ASN1InputStream(mv.toASN1Object().getEncoded());
        
        ASN1Sequence seq = (ASN1Sequence)aIn.readObject();

        mv = SemanticsInformation.getInstance(seq);
        
        checkStatement(mv, semanticsIdentifier, names);
    }

    private void checkStatement(
        SemanticsInformation si,
        DERObjectIdentifier  id,
        GeneralName[]        names)
    {
        if (id != null)
        {
            if (!si.getSemanticsIdentifier().equals(id))
            {
                fail("ids don't match.");
            }
        }
        else if (si.getSemanticsIdentifier() != null)
        {
            fail("statementId found when none expected.");
        }
        
        if (names != null)
        {
            GeneralName[] siNames = si.getNameRegistrationAuthorities();
            
            for (int i = 0; i != siNames.length; i++)
            {
                if (!names[i].equals(siNames[i]))
                {
                    fail("name registration authorities don't match.");
                }
            }
        }
        else if (si.getNameRegistrationAuthorities() != null)
        {
            fail("name registration authorities found when none expected.");
        }
    }
    
    public static void main(
        String[]    args)
    {
        runTest(new SemanticsInformationUnitTest());
    }
}
