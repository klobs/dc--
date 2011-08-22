package org.bouncycastle.crypto.test;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.GOST3411Digest;

public class GOST3411DigestTest
    extends DigestTest
{
    private static final String[] messages =
    {
        "",
        "This is message, length=32 bytes",
        "Suppose the original message has length = 50 bytes",
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    };
    
//  If S-box = D-A (see: digest/GOST3411Digest.java; function: E(byte[] in, byte[] key); string: CipherParameters  param = new GOST28147Parameters(key,"D-A");)
    private static final String[] digests =
    {
        "981e5f3ca30c841487830f84fb433e13ac1101569b9c13584ac483234cd656c0",
        "2cefc2f7b7bdc514e18ea57fa74ff357e7fa17d652c75f69cb1be7893ede48eb",
        "c3730c5cbccacf915ac292676f21e8bd4ef75331d9405e5f1a61dc3130a65011",
        "73b70a39497de53a6e08c67b6d4db853540f03e9389299d9b0156ef7e85d0f61"
    };

//  If S-box = D-Test (see: digest/GOST3411Digest.java; function:E(byte[] in, byte[] key); string: CipherParameters  param = new GOST28147Parameters(key,"D-Test");)
//    private static final String[] digests =
//    {
//        "ce85b99cc46752fffee35cab9a7b0278abb4c2d2055cff685af4912c49490f8d",
//        "b1c466d37519b82e8319819ff32595e047a28cb6f83eff1c6916a815a637fffa",
//        "471aba57a60a770d3a76130635c1fbea4ef14de51f78b4ae57dd893b62f55208",
//        "95c1af627c356496d80274330b2cff6a10c67b5f597087202f94d06d2338cf8e"
//    };
    
    // 1 million 'a'
    static private String  million_a_digest = "8693287aa62f9478f7cb312ec0866b6c4e4a0f11160441e8f4ffcd2715dd554f";

    GOST3411DigestTest()
    {
        super(new GOST3411Digest(), messages, digests);
    }

    public void performTest()
    {
        super.performTest();
        
        millionATest(million_a_digest);
    }

    protected Digest cloneDigest(Digest digest)
    {
        return new GOST3411Digest((GOST3411Digest)digest);
    }
    
    public static void main(
        String[]    args)
    {
        runTest(new GOST3411DigestTest());
    }
}
