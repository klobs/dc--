package org.bouncycastle.asn1.test;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.isismtt.x509.DeclarationOfMajority;

import java.io.IOException;

public class DeclarationOfMajorityUnitTest
    extends ASN1UnitTest
{
    public String getName()
    {
        return "DeclarationOfMajority";
    }

    public void performTest()
        throws Exception
    {
        DERGeneralizedTime dateOfBirth = new DERGeneralizedTime("20070315173729Z");
        DeclarationOfMajority decl = new DeclarationOfMajority(dateOfBirth);

        checkConstruction(decl, DeclarationOfMajority.dateOfBirth, dateOfBirth, -1);

        decl = new DeclarationOfMajority(6);

        checkConstruction(decl, DeclarationOfMajority.notYoungerThan, null, 6);

        decl = DeclarationOfMajority.getInstance(null);

        if (decl != null)
        {
            fail("null getInstance() failed.");
        }

        try
        {
            DeclarationOfMajority.getInstance(new Object());

            fail("getInstance() failed to detect bad object.");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    private void checkConstruction(
        DeclarationOfMajority decl,
        int                   type,
        DERGeneralizedTime    dateOfBirth,
        int                   notYoungerThan)
        throws IOException
    {
        checkValues(decl, type, dateOfBirth, notYoungerThan);

        decl = DeclarationOfMajority.getInstance(decl);

        checkValues(decl, type, dateOfBirth, notYoungerThan);

        ASN1InputStream aIn = new ASN1InputStream(decl.toASN1Object().getEncoded());

        DERTaggedObject info = (DERTaggedObject)aIn.readObject();

        decl = DeclarationOfMajority.getInstance(info);

        checkValues(decl, type, dateOfBirth, notYoungerThan);
    }

    private void checkValues(
        DeclarationOfMajority decl,
        int                   type,
        DERGeneralizedTime    dateOfBirth,
        int                   notYoungerThan)
    {
        checkMandatoryField("type", type, decl.getType());
        checkOptionalField("dateOfBirth", dateOfBirth, decl.getDateOfBirth());
        if (notYoungerThan != -1 && notYoungerThan != decl.notYoungerThan())
        {
            fail("notYoungerThan mismatch");
        }
    }

    public static void main(
        String[]    args)
    {
        runTest(new DeclarationOfMajorityUnitTest());
    }
}
