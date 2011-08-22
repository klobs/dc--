package org.bouncycastle.crypto.test;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.RIPEMD128Digest;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.encodings.ISO9796d1Encoding;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.ParametersWithSalt;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.signers.ISO9796d2PSSSigner;
import org.bouncycastle.crypto.signers.ISO9796d2Signer;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.test.SimpleTest;

/**
 * test vectors from ISO 9796-1 and ISO 9796-2 edition 1.
 */
public class ISO9796Test
    extends SimpleTest
{
    static BigInteger mod1 = new BigInteger("0100000000000000000000000000000000bba2d15dbb303c8a21c5ebbcbae52b7125087920dd7cdf358ea119fd66fb064012ec8ce692f0a0b8e8321b041acd40b7", 16);

    static BigInteger pub1 = new BigInteger("03", 16);

    static BigInteger pri1 = new BigInteger("2aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaac9f0783a49dd5f6c5af651f4c9d0dc9281c96a3f16a85f9572d7cc3f2d0f25a9dbf1149e4cdc32273faadd3fda5dcda7", 16);

    static BigInteger mod2 = new BigInteger("ffffff7fa27087c35ebead78412d2bdffe0301edd494df13458974ea89b364708f7d0f5a00a50779ddf9f7d4cb80b8891324da251a860c4ec9ef288104b3858d", 16);

    static BigInteger pub2 = new BigInteger("03", 16);

    static BigInteger pri2 = new BigInteger("2aaaaa9545bd6bf5e51fc7940adcdca5550080524e18cfd88b96e8d1c19de6121b13fac0eb0495d47928e047724d91d1740f6968457ce53ec8e24c9362ce84b5", 16);

    static byte msg1[] = Hex.decode("0cbbaa99887766554433221100");

    //
    // you'll need to see the ISO 9796 to make sense of this
    //
    static byte sig1[] = mod1.subtract(new BigInteger("309f873d8ded8379490f6097eaafdabc137d3ebfd8f25ab5f138d56a719cdc526bdd022ea65dabab920a81013a85d092e04d3e421caab717c90d89ea45a8d23a", 16)).toByteArray();

    static byte msg2[] = Hex.decode("fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210");

    static byte sig2[] = new BigInteger("319bb9becb49f3ed1bca26d0fcf09b0b0a508e4d0bd43b350f959b72cd25b3af47d608fdcd248eada74fbe19990dbeb9bf0da4b4e1200243a14e5cab3f7e610c", 16).toByteArray();

    static byte msg3[] = Hex.decode("0112233445566778899aabbccd");

    static byte sig3[] = mod2.subtract(new BigInteger("58e59ffb4b1fb1bcdbf8d1fe9afa3730c78a318a1134f5791b7313d480ff07ac319b068edf8f212945cb09cf33df30ace54f4a063fcca0b732f4b662dc4e2454", 16)).toByteArray();

    //
    // ISO 9796-2
    //
    static BigInteger mod3 = new BigInteger("ffffffff78f6c55506c59785e871211ee120b0b5dd644aa796d82413a47b24573f1be5745b5cd9950f6b389b52350d4e01e90009669a8720bf265a2865994190a661dea3c7828e2e7ca1b19651adc2d5", 16);

    static BigInteger pub3 = new BigInteger("03", 16);

    static BigInteger pri3 = new BigInteger("2aaaaaaa942920e38120ee965168302fd0301d73a4e60c7143ceb0adf0bf30b9352f50e8b9e4ceedd65343b2179005b2f099915e4b0c37e41314bb0821ad8330d23cba7f589e0f129b04c46b67dfce9d", 16);

    static BigInteger mod4 = new BigInteger("FFFFFFFF45f1903ebb83d4d363f70dc647b839f2a84e119b8830b2dec424a1ce0c9fd667966b81407e89278283f27ca8857d40979407fc6da4cc8a20ecb4b8913b5813332409bc1f391a94c9c328dfe46695daf922259174544e2bfbe45cc5cd", 16);
    static BigInteger pub4 = new BigInteger("02", 16);
    static BigInteger pri4 = new BigInteger("1fffffffe8be3207d7707a9a6c7ee1b8c8f7073e5509c2337106165bd8849439c193faccf2cd70280fd124f0507e4f94cb66447680c6b87b6599d1b61c8f3600854a618262e9c1cb1438e485e47437be036d94b906087a61ee74ab0d9a1accd8", 16);

    static byte msg4[] = Hex.decode("6162636462636465636465666465666765666768666768696768696a68696a6b696a6b6c6a6b6c6d6b6c6d6e6c6d6e6f6d6e6f706e6f7071");
    static byte sig4[] = Hex.decode("374695b7ee8b273925b4656cc2e008d41463996534aa5aa5afe72a52ffd84e118085f8558f36631471d043ad342de268b94b080bee18a068c10965f581e7f32899ad378835477064abed8ef3bd530fce");

    static byte msg5[] = Hex.decode("fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210");
    static byte sig5[] = Hex.decode("5cf9a01854dbacaec83aae8efc563d74538192e95466babacd361d7c86000fe42dcb4581e48e4feb862d04698da9203b1803b262105104d510b365ee9c660857ba1c001aa57abfd1c8de92e47c275cae");

    //
    // scheme 2 data
    //
    static BigInteger mod6 = new BigInteger("b259d2d6e627a768c94be36164c2d9fc79d97aab9253140e5bf17751197731d6f7540d2509e7b9ffee0a70a6e26d56e92d2edd7f85aba85600b69089f35f6bdbf3c298e05842535d9f064e6b0391cb7d306e0a2d20c4dfb4e7b49a9640bdea26c10ad69c3f05007ce2513cee44cfe01998e62b6c3637d3fc0391079b26ee36d5", 16);
    static BigInteger pub6 = new BigInteger("11", 16);
    static BigInteger pri6 = new BigInteger("92e08f83cc9920746989ca5034dcb384a094fb9c5a6288fcc4304424ab8f56388f72652d8fafc65a4b9020896f2cde297080f2a540e7b7ce5af0b3446e1258d1dd7f245cf54124b4c6e17da21b90a0ebd22605e6f45c9f136d7a13eaac1c0f7487de8bd6d924972408ebb58af71e76fd7b012a8d0e165f3ae2e5077a8648e619", 16);

    static byte sig6[] = new BigInteger("0073FEAF13EB12914A43FE635022BB4AB8188A8F3ABD8D8A9E4AD6C355EE920359C7F237AE36B1212FE947F676C68FE362247D27D1F298CA9302EB21F4A64C26CE44471EF8C0DFE1A54606F0BA8E63E87CDACA993BFA62973B567473B4D38FAE73AB228600934A9CC1D3263E632E21FD52D2B95C5F7023DA63DE9509C01F6C7BBC", 16).modPow(pri6, mod6).toByteArray();

    static byte msg7[] = Hex.decode("6162636462636465636465666465666765666768666768696768696A68696A6B696A6B6C6A6B6C6D6B6C6D6E6C6D6E6F6D6E6F706E6F70716F70717270717273");
    static byte sig7[] = new BigInteger("296B06224010E1EC230D4560A5F88F03550AAFCE31C805CE81E811E5E53E5F71AE64FC2A2A486B193E87972D90C54B807A862F21A21919A43ECF067240A8C8C641DE8DCDF1942CF790D136728FFC0D98FB906E7939C1EC0E64C0E067F0A7443D6170E411DF91F797D1FFD74009C4638462E69D5923E7433AEC028B9A90E633CC", 16).modPow(pri6, mod6).toByteArray();

    static byte msg8[] = Hex.decode("FEDCBA9876543210FEDCBA9876543210FEDCBA9876543210FEDCBA9876543210FEDCBA9876543210FEDCBA9876543210FEDCBA9876543210FEDCBA9876543210FEDCBA9876543210FEDCBA9876543210FEDCBA9876543210FEDCBA9876543210FEDCBA9876543210FEDCBA9876543210FEDCBA9876543210FEDCBA9876543210FEDCBA98");
    static byte sig8[] = new BigInteger("01402B29ABA104079677CE7FC3D5A84DB24494D6F9508B4596484F5B3CC7E8AFCC4DDE7081F21CAE9D4F94D6D2CCCB43FCEDA0988FFD4EF2EAE72CFDEB4A2638F0A34A0C49664CD9DB723315759D758836C8BA26AC4348B66958AC94AE0B5A75195B57ABFB9971E21337A4B517F2E820B81F26BCE7C66F48A2DB12A8F3D731CC", 16).modPow(pri6, mod6).toByteArray();

    static byte msg9[] = Hex.decode("6162636462636465636465666465666765666768666768696768696A68696A6B696A6B6C6A6B6C6D6B6C6D6E6C6D6E6F6D6E6F706E6F70716F707172707172737172737472737475737475767475767775767778767778797778797A78797A61797A61627A6162636162636462636465");
    static byte sig9[] = new BigInteger("6F2BB97571FE2EF205B66000E9DD06656655C1977F374E8666D636556A5FEEEEAF645555B25F45567C4EE5341F96FED86508C90A9E3F11B26E8D496139ED3E55ECE42860A6FB3A0817DAFBF13019D93E1D382DA07264FE99D9797D2F0B7779357CA7E74EE440D8855B7DDF15F000AC58EE3FFF144845E771907C0C83324A6FBC", 16).modPow(pri6, mod6).toByteArray();

    public String getName()
    {
        return "ISO9796";
    }

    private boolean isSameAs(
        byte[] a,
        int off,
        byte[] b)
    {
        if ((a.length - off) != b.length)
        {
            return false;
        }

        for (int i = 0; i != b.length; i++)
        {
            if (a[i + off] != b[i])
            {
                return false;
            }
        }

        return true;
    }

    private boolean startsWith(
        byte[] a,
        byte[] b)
    {
        if (a.length < b.length)
        {
            return false;
        }

        for (int i = 0; i != b.length; i++)
        {
            if (a[i] != b[i])
            {
                return false;
            }
        }

        return true;
    }

    private void doTest1()
        throws Exception
    {
        RSAKeyParameters pubParameters = new RSAKeyParameters(false, mod1, pub1);
        RSAKeyParameters privParameters = new RSAKeyParameters(true, mod1, pri1);
        RSAEngine rsa = new RSAEngine();
        byte[] data;

        //
        // ISO 9796-1 - public encrypt, private decrypt
        //
        ISO9796d1Encoding eng = new ISO9796d1Encoding(rsa);

        eng.init(true, privParameters);

        eng.setPadBits(4);

        data = eng.processBlock(msg1, 0, msg1.length);

        eng.init(false, pubParameters);

        if (!areEqual(sig1, data))
        {
            fail("failed ISO9796-1 generation Test 1");
        }

        data = eng.processBlock(data, 0, data.length);

        if (!areEqual(msg1, data))
        {
            fail("failed ISO9796-1 retrieve Test 1");
        }
    }

    private void doTest2()
        throws Exception
    {
        RSAKeyParameters pubParameters = new RSAKeyParameters(false, mod1, pub1);
        RSAKeyParameters privParameters = new RSAKeyParameters(true, mod1, pri1);
        RSAEngine rsa = new RSAEngine();
        byte[] data;

        //
        // ISO 9796-1 - public encrypt, private decrypt
        //
        ISO9796d1Encoding eng = new ISO9796d1Encoding(rsa);

        eng.init(true, privParameters);

        data = eng.processBlock(msg2, 0, msg2.length);

        eng.init(false, pubParameters);

        if (!isSameAs(data, 1, sig2))
        {
            fail("failed ISO9796-1 generation Test 2");
        }

        data = eng.processBlock(data, 0, data.length);


        if (!areEqual(msg2, data))
        {
            fail("failed ISO9796-1 retrieve Test 2");
        }
    }

    public void doTest3()
        throws Exception
    {
        RSAKeyParameters pubParameters = new RSAKeyParameters(false, mod2, pub2);
        RSAKeyParameters privParameters = new RSAKeyParameters(true, mod2, pri2);
        RSAEngine rsa = new RSAEngine();
        byte[] data;

        //
        // ISO 9796-1 - public encrypt, private decrypt
        //
        ISO9796d1Encoding eng = new ISO9796d1Encoding(rsa);

        eng.init(true, privParameters);

        eng.setPadBits(4);

        data = eng.processBlock(msg3, 0, msg3.length);

        eng.init(false, pubParameters);

        if (!isSameAs(sig3, 1, data))
        {
            fail("failed ISO9796-1 generation Test 3");
        }

        data = eng.processBlock(data, 0, data.length);

        if (!isSameAs(msg3, 0, data))
        {
            fail("failed ISO9796-1 retrieve Test 3");
        }
    }

    public void doTest4()
        throws Exception
    {
        RSAKeyParameters pubParameters = new RSAKeyParameters(false, mod3, pub3);
        RSAKeyParameters privParameters = new RSAKeyParameters(true, mod3, pri3);
        RSAEngine rsa = new RSAEngine();
        byte[] data;

        //
        // ISO 9796-2 - Signing
        //
        ISO9796d2Signer eng = new ISO9796d2Signer(rsa, new RIPEMD128Digest());

        eng.init(true, privParameters);

        eng.update(msg4[0]);
        eng.update(msg4, 1, msg4.length - 1);

        data = eng.generateSignature();

        eng.init(false, pubParameters);

        if (!isSameAs(sig4, 0, data))
        {
            fail("failed ISO9796-2 generation Test 4");
        }

        eng.update(msg4[0]);
        eng.update(msg4, 1, msg4.length - 1);

        if (!eng.verifySignature(sig4))
        {
            fail("failed ISO9796-2 verify Test 4");
        }

        if (eng.hasFullMessage())
        {
            eng = new ISO9796d2Signer(rsa, new RIPEMD128Digest());

            eng.init(false, pubParameters);

            if (!eng.verifySignature(sig4))
            {
                fail("failed ISO9796-2 verify and recover Test 4");
            }

            if (!isSameAs(eng.getRecoveredMessage(), 0, msg4))
            {
                fail("failed ISO9796-2 recovered message Test 4");
            }

            // try update with recovered
            eng.updateWithRecoveredMessage(sig4);

            if (!isSameAs(eng.getRecoveredMessage(), 0, msg4))
            {
                fail("failed ISO9796-2 updateWithRecovered recovered message Test 4");
            }

            if (!eng.verifySignature(sig4))
            {
                fail("failed ISO9796-2 updateWithRecovered verify and recover Test 4");
            }

            if (!isSameAs(eng.getRecoveredMessage(), 0, msg4))
            {
                fail("failed ISO9796-2 updateWithRecovered recovered verify message Test 4");
            }

            // should fail
            eng.updateWithRecoveredMessage(sig4);

            eng.update(msg4, 0, msg4.length);

            if (eng.verifySignature(sig4))
            {
                fail("failed ISO9796-2 updateWithRecovered verify and recover Test 4");
            }
        }
        else
        {
            fail("full message flag false - Test 4");
        }
    }

    public void doTest5()
        throws Exception
    {
        RSAKeyParameters pubParameters = new RSAKeyParameters(false, mod3, pub3);
        RSAKeyParameters privParameters = new RSAKeyParameters(true, mod3, pri3);
        RSAEngine rsa = new RSAEngine();
        byte[] data;

        //
        // ISO 9796-2 - Signing
        //
        ISO9796d2Signer eng = new ISO9796d2Signer(rsa, new RIPEMD160Digest(), true);

        eng.init(true, privParameters);

        eng.update(msg5[0]);
        eng.update(msg5, 1, msg5.length - 1);

        data = eng.generateSignature();

        eng.init(false, pubParameters);

        if (!isSameAs(sig5, 0, data))
        {
            fail("failed ISO9796-2 generation Test 5");
        }

        eng.update(msg5[0]);
        eng.update(msg5, 1, msg5.length - 1);

        if (!eng.verifySignature(sig5))
        {
            fail("failed ISO9796-2 verify Test 5");
        }

        if (eng.hasFullMessage())
        {
            fail("fullMessage true - Test 5");
        }

        if (!startsWith(msg5, eng.getRecoveredMessage()))
        {
            fail("failed ISO9796-2 partial recovered message Test 5");
        }

        int length = eng.getRecoveredMessage().length;

        if (length >= msg5.length)
        {
            fail("Test 5 recovered message too long");
        }

        eng = new ISO9796d2Signer(rsa, new RIPEMD160Digest(), true);

        eng.init(false, pubParameters);

        eng.updateWithRecoveredMessage(sig5);

        if (!startsWith(msg5, eng.getRecoveredMessage()))
        {
            fail("failed ISO9796-2 updateWithRecovered partial recovered message Test 5");
        }

        if (eng.hasFullMessage())
        {
            fail("fullMessage updateWithRecovered true - Test 5");
        }

        for (int i = length; i != msg5.length; i++)
        {
            eng.update(msg5[i]);
        }

        if (!eng.verifySignature(sig5))
        {
            fail("failed ISO9796-2 verify Test 5");
        }

        if (eng.hasFullMessage())
        {
            fail("fullMessage updateWithRecovered true - Test 5");
        }

        // should fail
        eng.updateWithRecoveredMessage(sig5);

        eng.update(msg5, 0, msg5.length);

        if (eng.verifySignature(sig5))
        {
            fail("failed ISO9796-2 updateWithRecovered verify fail Test 5");
        }
    }

    //
    // against a zero length string
    //

    public void doTest6()
        throws Exception
    {
        byte[] salt = Hex.decode("61DF870C4890FE85D6E3DD87C3DCE3723F91DB49");
        RSAKeyParameters pubParameters = new RSAKeyParameters(false, mod6, pub6);
        RSAKeyParameters privParameters = new RSAKeyParameters(true, mod6, pri6);
        ParametersWithSalt sigParameters = new ParametersWithSalt(privParameters, salt);
        RSAEngine rsa = new RSAEngine();
        byte[] data;

        //
        // ISO 9796-2 - PSS Signing
        //
        ISO9796d2PSSSigner eng = new ISO9796d2PSSSigner(rsa, new RIPEMD160Digest(), 20, true);

        eng.init(true, sigParameters);

        data = eng.generateSignature();

        eng.init(false, pubParameters);

        if (!isSameAs(sig6, 1, data))
        {
            fail("failed ISO9796-2 generation Test 6");
        }

        if (!eng.verifySignature(data))
        {
            fail("failed ISO9796-2 verify Test 6");
        }
    }

    public void doTest7()
        throws Exception
    {
        byte[] salt = new byte[0];
        RSAKeyParameters pubParameters = new RSAKeyParameters(false, mod6, pub6);
        RSAKeyParameters privParameters = new RSAKeyParameters(true, mod6, pri6);
        ParametersWithSalt sigParameters = new ParametersWithSalt(privParameters, salt);
        RSAEngine rsa = new RSAEngine();
        byte[] data;

        //
        // ISO 9796-2 - PSS Signing
        //
        ISO9796d2PSSSigner eng = new ISO9796d2PSSSigner(rsa, new SHA1Digest(), 0, false);

        eng.init(true, sigParameters);

        eng.update(msg7[0]);
        eng.update(msg7, 1, msg7.length - 1);

        data = eng.generateSignature();

        eng.init(false, pubParameters);

        if (!isSameAs(sig7, 0, data))
        {
            fail("failed ISO9796-2 generation Test 7");
        }

        eng.update(msg7[0]);
        eng.update(msg7, 1, msg7.length - 1);

        if (!eng.verifySignature(sig7))
        {
            fail("failed ISO9796-2 verify Test 7");
        }

        if (!isSameAs(msg7, 0, eng.getRecoveredMessage()))
        {
            fail("failed ISO9796-2 recovery Test 7");
        }
    }

    public void doTest8()
        throws Exception
    {
        byte[] salt = Hex.decode("78E293203CBA1B7F92F05F4D171FF8CA3E738FF8");
        RSAKeyParameters pubParameters = new RSAKeyParameters(false, mod6, pub6);
        RSAKeyParameters privParameters = new RSAKeyParameters(true, mod6, pri6);
        ParametersWithSalt sigParameters = new ParametersWithSalt(privParameters, salt);
        RSAEngine rsa = new RSAEngine();
        byte[] data;

        //
        // ISO 9796-2 - PSS Signing
        //
        ISO9796d2PSSSigner eng = new ISO9796d2PSSSigner(rsa, new RIPEMD160Digest(), 20, false);

        eng.init(true, sigParameters);

        eng.update(msg8[0]);
        eng.update(msg8, 1, msg8.length - 1);

        data = eng.generateSignature();

        eng.init(false, pubParameters);

        if (!isSameAs(sig8, 0, data))
        {
            fail("failed ISO9796-2 generation Test 8");
        }

        eng.update(msg8[0]);
        eng.update(msg8, 1, msg8.length - 1);

        if (!eng.verifySignature(sig8))
        {
            fail("failed ISO9796-2 verify Test 8");
        }
    }

    public void doTest9()
        throws Exception
    {
        RSAKeyParameters pubParameters = new RSAKeyParameters(false, mod6, pub6);
        RSAKeyParameters privParameters = new RSAKeyParameters(true, mod6, pri6);
        RSAEngine rsa = new RSAEngine();
        byte[] data;

        //
        // ISO 9796-2 - PSS Signing
        //
        ISO9796d2PSSSigner eng = new ISO9796d2PSSSigner(rsa, new RIPEMD160Digest(), 0, true);

        eng.init(true, privParameters);

        eng.update(msg9[0]);
        eng.update(msg9, 1, msg9.length - 1);

        data = eng.generateSignature();

        eng.init(false, pubParameters);

        if (!isSameAs(sig9, 0, data))
        {
            fail("failed ISO9796-2 generation Test 9");
        }

        eng.update(msg9[0]);
        eng.update(msg9, 1, msg9.length - 1);

        if (!eng.verifySignature(sig9))
        {
            fail("failed ISO9796-2 verify Test 9");
        }
    }

    public void doTest10()
        throws Exception
    {
        BigInteger mod = new BigInteger("B3ABE6D91A4020920F8B3847764ECB34C4EB64151A96FDE7B614DC986C810FF2FD73575BDF8532C06004C8B4C8B64F700A50AEC68C0701ED10E8D211A4EA554D", 16);
        BigInteger pubExp = new BigInteger("65537", 10);
        BigInteger priExp = new BigInteger("AEE76AE4716F77C5782838F328327012C097BD67E5E892E75C1356E372CCF8EE1AA2D2CBDFB4DA19F703743F7C0BA42B2D69202BA7338C294D1F8B6A5771FF41", 16);
        RSAKeyParameters pubParameters = new RSAKeyParameters(false, mod, pubExp);
        RSAKeyParameters privParameters = new RSAKeyParameters(true, mod, priExp);
        RSAEngine rsa = new RSAEngine();
        byte[] data;

        //
        // ISO 9796-2 - PSS Signing
        //
        Digest dig = new SHA1Digest();
        ISO9796d2PSSSigner eng = new ISO9796d2PSSSigner(rsa, dig, dig.getDigestSize());

        //
        // as the padding is random this test needs to repeat a few times to
        // make sure
        //
        for (int i = 0; i != 500; i++)
        {
            eng.init(true, privParameters);

            eng.update(msg9[0]);
            eng.update(msg9, 1, msg9.length - 1);

            data = eng.generateSignature();

            eng.init(false, pubParameters);

            eng.update(msg9[0]);
            eng.update(msg9, 1, msg9.length - 1);

            if (!eng.verifySignature(data))
            {
                fail("failed ISO9796-2 verify Test 10");
            }
        }
    }

    public void doTest11()
        throws Exception
    {
        BigInteger mod = new BigInteger("B3ABE6D91A4020920F8B3847764ECB34C4EB64151A96FDE7B614DC986C810FF2FD73575BDF8532C06004C8B4C8B64F700A50AEC68C0701ED10E8D211A4EA554D", 16);
        BigInteger pubExp = new BigInteger("65537", 10);
        BigInteger priExp = new BigInteger("AEE76AE4716F77C5782838F328327012C097BD67E5E892E75C1356E372CCF8EE1AA2D2CBDFB4DA19F703743F7C0BA42B2D69202BA7338C294D1F8B6A5771FF41", 16);
        RSAKeyParameters pubParameters = new RSAKeyParameters(false, mod, pubExp);
        RSAKeyParameters privParameters = new RSAKeyParameters(true, mod, priExp);
        RSAEngine rsa = new RSAEngine();
        byte[] data;
        byte[] m1 = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        byte[] m2 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        byte[] m3 = {1, 2, 3, 4, 5, 6, 7, 8};

        //
        // ISO 9796-2 - PSS Signing
        //
        Digest dig = new SHA1Digest();
        ISO9796d2PSSSigner eng = new ISO9796d2PSSSigner(rsa, dig, dig.getDigestSize());

        //
        // check message bounds
        //
        eng.init(true, privParameters);

        eng.update(m1, 0, m1.length);

        data = eng.generateSignature();

        eng.init(false, pubParameters);

        eng.update(m2, 0, m2.length);

        if (eng.verifySignature(data))
        {
            fail("failed ISO9796-2 m2 verify Test 11");
        }

        eng.init(false, pubParameters);

        eng.update(m3, 0, m3.length);

        if (eng.verifySignature(data))
        {
            fail("failed ISO9796-2 m3 verify Test 11");
        }

        eng.init(false, pubParameters);

        eng.update(m1, 0, m1.length);

        if (!eng.verifySignature(data))
        {
            fail("failed ISO9796-2 verify Test 11");
        }
    }

    public void doTest12()
        throws Exception
    {
        BigInteger mod = new BigInteger("B3ABE6D91A4020920F8B3847764ECB34C4EB64151A96FDE7B614DC986C810FF2FD73575BDF8532C06004C8B4C8B64F700A50AEC68C0701ED10E8D211A4EA554D", 16);
        BigInteger pubExp = new BigInteger("65537", 10);
        BigInteger priExp = new BigInteger("AEE76AE4716F77C5782838F328327012C097BD67E5E892E75C1356E372CCF8EE1AA2D2CBDFB4DA19F703743F7C0BA42B2D69202BA7338C294D1F8B6A5771FF41", 16);
        RSAKeyParameters pubParameters = new RSAKeyParameters(false, mod, pubExp);
        RSAKeyParameters privParameters = new RSAKeyParameters(true, mod, priExp);
        RSAEngine rsa = new RSAEngine();
        byte[] data;
        byte[] m1 = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        byte[] m2 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        byte[] m3 = {1, 2, 3, 4, 5, 6, 7, 8};

        //
        // ISO 9796-2 - PSS Signing
        //
        Digest dig = new SHA1Digest();
        ISO9796d2Signer eng = new ISO9796d2Signer(rsa, dig);

        //
        // check message bounds
        //
        eng.init(true, privParameters);

        eng.update(m1, 0, m1.length);

        data = eng.generateSignature();

        eng.init(false, pubParameters);

        eng.update(m2, 0, m2.length);

        if (eng.verifySignature(data))
        {
            fail("failed ISO9796-2 m2 verify Test 12");
        }

        eng.init(false, pubParameters);

        eng.update(m3, 0, m3.length);

        if (eng.verifySignature(data))
        {
            fail("failed ISO9796-2 m3 verify Test 12");
        }

        eng.init(false, pubParameters);

        eng.update(m1, 0, m1.length);

        if (!eng.verifySignature(data))
        {
            fail("failed ISO9796-2 verify Test 12");
        }
    }


    private void doTest13()
        throws Exception
    {
        BigInteger modulus = new BigInteger(1, Hex.decode("CDCBDABBF93BE8E8294E32B055256BBD0397735189BF75816341BB0D488D05D627991221DF7D59835C76A4BB4808ADEEB779E7794504E956ADC2A661B46904CDC71337DD29DDDD454124EF79CFDD7BC2C21952573CEFBA485CC38C6BD2428809B5A31A898A6B5648CAA4ED678D9743B589134B7187478996300EDBA16271A861"));
        BigInteger pubExp = new BigInteger(1, Hex.decode("010001"));
        BigInteger privExp = new BigInteger(1, Hex.decode("4BA6432AD42C74AA5AFCB6DF60FD57846CBC909489994ABD9C59FE439CC6D23D6DE2F3EA65B8335E796FD7904CA37C248367997257AFBD82B26F1A30525C447A236C65E6ADE43ECAAF7283584B2570FA07B340D9C9380D88EAACFFAEEFE7F472DBC9735C3FF3A3211E8A6BBFD94456B6A33C17A2C4EC18CE6335150548ED126D"));

        RSAKeyParameters pubParams = new RSAKeyParameters(false, modulus, pubExp);
        RSAKeyParameters privParams = new RSAKeyParameters(true, modulus, privExp);

        AsymmetricBlockCipher rsaEngine = new RSABlindedEngine();
        Digest digest = new SHA256Digest();

        // set challenge to all zero's for verification
        byte[] challenge = new byte[8];

        // DOES NOT USE FINAL BOOLEAN TO INDICATE RECOVERY
        ISO9796d2Signer signer = new ISO9796d2Signer(rsaEngine, digest, false);

        // sign
        signer.init(true, privParams);
        signer.update(challenge, 0, challenge.length);

        byte[]  sig = signer.generateSignature();

        // verify
        signer.init(false, pubParams);
        signer.update(challenge, 0, challenge.length);

        if (!signer.verifySignature(sig))
        {
            fail("basic verification failed");
        }

        // === LETS ACTUALLY DO SOME RECOVERY, USING INPUT FROM INTERNAL AUTHENTICATE ===

        signer.reset();

        final String args0 = "482E20D1EDDED34359C38F5E7C01203F9D6B2641CDCA5C404D49ADAEDE034C7481D781D043722587761C90468DE69C6585A1E8B9C322F90E1B580EEDAB3F6007D0C366CF92B4DB8B41C8314929DCE2BE889C0129123484D2FD3D12763D2EBFD12AC8E51D7061AFCA1A53DEDEC7B9A617472A78C952CCC72467AE008E5F132994";

        digest = new SHA1Digest();

        // NOTE setting implit to false does not actually do anything for verification !!!
        signer = new ISO9796d2Signer(rsaEngine, digest, false);


        signer.init(false, pubParams);
        final byte[] signature = Hex.decode(args0);
        signer.updateWithRecoveredMessage(signature);
        signer.update(challenge, 0, challenge.length);

        if (!signer.verifySignature(signature))
        {
            fail("recovered + challenge signature failed");
        }

        // === FINALLY, USING SHA-256 ===

        signer.reset();

        digest = new SHA256Digest();

        // NOTE setting implit to false does not actually do anything for verification !!!
        signer = new ISO9796d2Signer(rsaEngine, digest, false);


        signer.init(true, privParams);
        // generate NONCE of correct length using some inner knowledge
        int nonceLength = modulus.bitLength() / 8 - 1 - digest.getDigestSize() - 2;
        final byte[] nonce = new byte[nonceLength];
        SecureRandom rnd = new SecureRandom();

        rnd.nextBytes(nonce);

        signer.update(nonce, 0, nonce.length);
        signer.update(challenge, 0, challenge.length);
        byte[] sig3 = signer.generateSignature();

        signer.init(false, pubParams);
        signer.updateWithRecoveredMessage(sig3);
        signer.update(challenge, 0, challenge.length);
        if (signer.verifySignature(sig3))
        {
            if (signer.hasFullMessage())
            {
                fail("signer indicates full message");
            }
            byte[] recoverableMessage = signer.getRecoveredMessage();

            // sanity check, normally the nonce is ignored in eMRTD specs (PKI Technical Report)
            if (!Arrays.areEqual(nonce, recoverableMessage))
            {
                fail("Nonce compare with recoverable part of message failed");
            }
        }
        else
        {
            fail("recoverable + nonce failed.");
        }
    }

    public void performTest()
        throws Exception
    {
        doTest1();
        doTest2();
        doTest3();
        doTest4();
        doTest5();
        doTest6();
        doTest7();
        doTest8();
        doTest9();
        doTest10();
        doTest11();
        doTest12();
        doTest13();
    }

    public static void main(
        String[] args)
    {
        runTest(new ISO9796Test());
    }
}
