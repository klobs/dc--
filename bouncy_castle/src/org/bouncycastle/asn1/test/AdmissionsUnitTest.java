package org.bouncycastle.asn1.test;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.isismtt.x509.Admissions;
import org.bouncycastle.asn1.isismtt.x509.NamingAuthority;
import org.bouncycastle.asn1.isismtt.x509.ProfessionInfo;
import org.bouncycastle.asn1.x500.DirectoryString;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509Name;

import java.io.IOException;

public class AdmissionsUnitTest
    extends ASN1UnitTest
{
    public String getName()
    {
        return "Admissions";
    }

    public void performTest()
        throws Exception
    {
        GeneralName name = new GeneralName(new X509Name("CN=hello world"));
        NamingAuthority auth =  new NamingAuthority(new DERObjectIdentifier("1.2.3"), "url", new DirectoryString("fred"));
        Admissions  admissions = new Admissions(name, auth, new ProfessionInfo[0]);

        checkConstruction(admissions, name, auth);

        admissions = Admissions.getInstance(null);

        if (admissions != null)
        {
            fail("null getInstance() failed.");
        }

        try
        {
            Admissions.getInstance(new Object());

            fail("getInstance() failed to detect bad object.");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    private void checkConstruction(
        Admissions      admissions,
        GeneralName     name,
        NamingAuthority auth)
        throws IOException
    {
        checkValues(admissions, name, auth);

        admissions = Admissions.getInstance(admissions);

        checkValues(admissions, name, auth);

        ASN1InputStream aIn = new ASN1InputStream(admissions.toASN1Object().getEncoded());

        ASN1Sequence info = (ASN1Sequence)aIn.readObject();

        admissions = Admissions.getInstance(info);

        checkValues(admissions, name, auth);
    }

    private void checkValues(
        Admissions      admissions,
        GeneralName     name,
        NamingAuthority auth)
    {
        checkMandatoryField("admissionAuthority", name, admissions.getAdmissionAuthority());
        checkMandatoryField("namingAuthority", auth, admissions.getNamingAuthority());
    }

    public static void main(
        String[]    args)
    {
        runTest(new AdmissionsUnitTest());
    }
}
