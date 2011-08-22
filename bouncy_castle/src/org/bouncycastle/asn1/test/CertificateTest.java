package org.bouncycastle.asn1.test;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.util.ASN1Dump;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AttCertIssuer;
import org.bouncycastle.asn1.x509.AttCertValidityPeriod;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.AttributeCertificate;
import org.bouncycastle.asn1.x509.AttributeCertificateInfo;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.Holder;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.TBSCertificateStructure;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.test.SimpleTest;

import java.io.ByteArrayInputStream;
import java.util.Enumeration;

public class CertificateTest
    extends SimpleTest
{
    //
    // server.crt
    //
    byte[]  cert1 = Base64.decode(
           "MIIDXjCCAsegAwIBAgIBBzANBgkqhkiG9w0BAQQFADCBtzELMAkGA1UEBhMCQVUx"
         + "ETAPBgNVBAgTCFZpY3RvcmlhMRgwFgYDVQQHEw9Tb3V0aCBNZWxib3VybmUxGjAY"
         + "BgNVBAoTEUNvbm5lY3QgNCBQdHkgTHRkMR4wHAYDVQQLExVDZXJ0aWZpY2F0ZSBB"
         + "dXRob3JpdHkxFTATBgNVBAMTDENvbm5lY3QgNCBDQTEoMCYGCSqGSIb3DQEJARYZ"
         + "d2VibWFzdGVyQGNvbm5lY3Q0LmNvbS5hdTAeFw0wMDA2MDIwNzU2MjFaFw0wMTA2"
         + "MDIwNzU2MjFaMIG4MQswCQYDVQQGEwJBVTERMA8GA1UECBMIVmljdG9yaWExGDAW"
         + "BgNVBAcTD1NvdXRoIE1lbGJvdXJuZTEaMBgGA1UEChMRQ29ubmVjdCA0IFB0eSBM"
         + "dGQxFzAVBgNVBAsTDldlYnNlcnZlciBUZWFtMR0wGwYDVQQDExR3d3cyLmNvbm5l"
         + "Y3Q0LmNvbS5hdTEoMCYGCSqGSIb3DQEJARYZd2VibWFzdGVyQGNvbm5lY3Q0LmNv"
         + "bS5hdTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEArvDxclKAhyv7Q/Wmr2re"
         + "Gw4XL9Cnh9e+6VgWy2AWNy/MVeXdlxzd7QAuc1eOWQkGQEiLPy5XQtTY+sBUJ3AO"
         + "Rvd2fEVJIcjf29ey7bYua9J/vz5MG2KYo9/WCHIwqD9mmG9g0xLcfwq/s8ZJBswE"
         + "7sb85VU+h94PTvsWOsWuKaECAwEAAaN3MHUwJAYDVR0RBB0wG4EZd2VibWFzdGVy"
         + "QGNvbm5lY3Q0LmNvbS5hdTA6BglghkgBhvhCAQ0ELRYrbW9kX3NzbCBnZW5lcmF0"
         + "ZWQgY3VzdG9tIHNlcnZlciBjZXJ0aWZpY2F0ZTARBglghkgBhvhCAQEEBAMCBkAw"
         + "DQYJKoZIhvcNAQEEBQADgYEAotccfKpwSsIxM1Hae8DR7M/Rw8dg/RqOWx45HNVL"
         + "iBS4/3N/TO195yeQKbfmzbAA2jbPVvIvGgTxPgO1MP4ZgvgRhasaa0qCJCkWvpM4"
         + "yQf33vOiYQbpv4rTwzU8AmRlBG45WdjyNIigGV+oRc61aKCTnLq7zB8N3z1TF/bF"
         + "5/8=");

    //
    // ca.crt
    //
    byte[]  cert2 = Base64.decode(
           "MIIDbDCCAtWgAwIBAgIBADANBgkqhkiG9w0BAQQFADCBtzELMAkGA1UEBhMCQVUx"
         + "ETAPBgNVBAgTCFZpY3RvcmlhMRgwFgYDVQQHEw9Tb3V0aCBNZWxib3VybmUxGjAY"
         + "BgNVBAoTEUNvbm5lY3QgNCBQdHkgTHRkMR4wHAYDVQQLExVDZXJ0aWZpY2F0ZSBB"
         + "dXRob3JpdHkxFTATBgNVBAMTDENvbm5lY3QgNCBDQTEoMCYGCSqGSIb3DQEJARYZ"
         + "d2VibWFzdGVyQGNvbm5lY3Q0LmNvbS5hdTAeFw0wMDA2MDIwNzU1MzNaFw0wMTA2"
         + "MDIwNzU1MzNaMIG3MQswCQYDVQQGEwJBVTERMA8GA1UECBMIVmljdG9yaWExGDAW"
         + "BgNVBAcTD1NvdXRoIE1lbGJvdXJuZTEaMBgGA1UEChMRQ29ubmVjdCA0IFB0eSBM"
         + "dGQxHjAcBgNVBAsTFUNlcnRpZmljYXRlIEF1dGhvcml0eTEVMBMGA1UEAxMMQ29u"
         + "bmVjdCA0IENBMSgwJgYJKoZIhvcNAQkBFhl3ZWJtYXN0ZXJAY29ubmVjdDQuY29t"
         + "LmF1MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDgs5ptNG6Qv1ZpCDuUNGmv"
         + "rhjqMDPd3ri8JzZNRiiFlBA4e6/ReaO1U8ASewDeQMH6i9R6degFdQRLngbuJP0s"
         + "xcEE+SksEWNvygfzLwV9J/q+TQDyJYK52utb++lS0b48A1KPLwEsyL6kOAgelbur"
         + "ukwxowprKUIV7Knf1ajetQIDAQABo4GFMIGCMCQGA1UdEQQdMBuBGXdlYm1hc3Rl"
         + "ckBjb25uZWN0NC5jb20uYXUwDwYDVR0TBAgwBgEB/wIBADA2BglghkgBhvhCAQ0E"
         + "KRYnbW9kX3NzbCBnZW5lcmF0ZWQgY3VzdG9tIENBIGNlcnRpZmljYXRlMBEGCWCG"
         + "SAGG+EIBAQQEAwICBDANBgkqhkiG9w0BAQQFAAOBgQCsGvfdghH8pPhlwm1r3pQk"
         + "msnLAVIBb01EhbXm2861iXZfWqGQjrGAaA0ZpXNk9oo110yxoqEoSJSzniZa7Xtz"
         + "soTwNUpE0SLHvWf/SlKdFWlzXA+vOZbzEv4UmjeelekTm7lc01EEa5QRVzOxHFtQ"
         + "DhkaJ8VqOMajkQFma2r9iA==");

    //
    // testx509.pem
    //
    byte[]  cert3 = Base64.decode(
           "MIIBWzCCAQYCARgwDQYJKoZIhvcNAQEEBQAwODELMAkGA1UEBhMCQVUxDDAKBgNV"
         + "BAgTA1FMRDEbMBkGA1UEAxMSU1NMZWF5L3JzYSB0ZXN0IENBMB4XDTk1MDYxOTIz"
         + "MzMxMloXDTk1MDcxNzIzMzMxMlowOjELMAkGA1UEBhMCQVUxDDAKBgNVBAgTA1FM"
         + "RDEdMBsGA1UEAxMUU1NMZWF5L3JzYSB0ZXN0IGNlcnQwXDANBgkqhkiG9w0BAQEF"
         + "AANLADBIAkEAqtt6qS5GTxVxGZYWa0/4u+IwHf7p2LNZbcPBp9/OfIcYAXBQn8hO"
         + "/Re1uwLKXdCjIoaGs4DLdG88rkzfyK5dPQIDAQABMAwGCCqGSIb3DQIFBQADQQAE"
         + "Wc7EcF8po2/ZO6kNCwK/ICH6DobgLekA5lSLr5EvuioZniZp5lFzAw4+YzPQ7XKJ"
         + "zl9HYIMxATFyqSiD9jsx");

    //
    // v3-cert1.pem
    //
    byte[]  cert4 = Base64.decode(
           "MIICjTCCAfigAwIBAgIEMaYgRzALBgkqhkiG9w0BAQQwRTELMAkGA1UEBhMCVVMx"
         + "NjA0BgNVBAoTLU5hdGlvbmFsIEFlcm9uYXV0aWNzIGFuZCBTcGFjZSBBZG1pbmlz"
         + "dHJhdGlvbjAmFxE5NjA1MjgxMzQ5MDUrMDgwMBcROTgwNTI4MTM0OTA1KzA4MDAw"
         + "ZzELMAkGA1UEBhMCVVMxNjA0BgNVBAoTLU5hdGlvbmFsIEFlcm9uYXV0aWNzIGFu"
         + "ZCBTcGFjZSBBZG1pbmlzdHJhdGlvbjEgMAkGA1UEBRMCMTYwEwYDVQQDEwxTdGV2"
         + "ZSBTY2hvY2gwWDALBgkqhkiG9w0BAQEDSQAwRgJBALrAwyYdgxmzNP/ts0Uyf6Bp"
         + "miJYktU/w4NG67ULaN4B5CnEz7k57s9o3YY3LecETgQ5iQHmkwlYDTL2fTgVfw0C"
         + "AQOjgaswgagwZAYDVR0ZAQH/BFowWDBWMFQxCzAJBgNVBAYTAlVTMTYwNAYDVQQK"
         + "Ey1OYXRpb25hbCBBZXJvbmF1dGljcyBhbmQgU3BhY2UgQWRtaW5pc3RyYXRpb24x"
         + "DTALBgNVBAMTBENSTDEwFwYDVR0BAQH/BA0wC4AJODMyOTcwODEwMBgGA1UdAgQR"
         + "MA8ECTgzMjk3MDgyM4ACBSAwDQYDVR0KBAYwBAMCBkAwCwYJKoZIhvcNAQEEA4GB"
         + "AH2y1VCEw/A4zaXzSYZJTTUi3uawbbFiS2yxHvgf28+8Js0OHXk1H1w2d6qOHH21"
         + "X82tZXd/0JtG0g1T9usFFBDvYK8O0ebgz/P5ELJnBL2+atObEuJy1ZZ0pBDWINR3"
         + "WkDNLCGiTkCKp0F5EWIrVDwh54NNevkCQRZita+z4IBO");

    //
    // v3-cert2.pem
    //
    byte[]  cert5 = Base64.decode(
           "MIICiTCCAfKgAwIBAgIEMeZfHzANBgkqhkiG9w0BAQQFADB9MQswCQYDVQQGEwJD"
         + "YTEPMA0GA1UEBxMGTmVwZWFuMR4wHAYDVQQLExVObyBMaWFiaWxpdHkgQWNjZXB0"
         + "ZWQxHzAdBgNVBAoTFkZvciBEZW1vIFB1cnBvc2VzIE9ubHkxHDAaBgNVBAMTE0Vu"
         + "dHJ1c3QgRGVtbyBXZWIgQ0EwHhcNOTYwNzEyMTQyMDE1WhcNOTYxMDEyMTQyMDE1"
         + "WjB0MSQwIgYJKoZIhvcNAQkBExVjb29rZUBpc3NsLmF0bC5ocC5jb20xCzAJBgNV"
         + "BAYTAlVTMScwJQYDVQQLEx5IZXdsZXR0IFBhY2thcmQgQ29tcGFueSAoSVNTTCkx"
         + "FjAUBgNVBAMTDVBhdWwgQS4gQ29va2UwXDANBgkqhkiG9w0BAQEFAANLADBIAkEA"
         + "6ceSq9a9AU6g+zBwaL/yVmW1/9EE8s5you1mgjHnj0wAILuoB3L6rm6jmFRy7QZT"
         + "G43IhVZdDua4e+5/n1ZslwIDAQABo2MwYTARBglghkgBhvhCAQEEBAMCB4AwTAYJ"
         + "YIZIAYb4QgENBD8WPVRoaXMgY2VydGlmaWNhdGUgaXMgb25seSBpbnRlbmRlZCBm"
         + "b3IgZGVtb25zdHJhdGlvbiBwdXJwb3Nlcy4wDQYJKoZIhvcNAQEEBQADgYEAi8qc"
         + "F3zfFqy1sV8NhjwLVwOKuSfhR/Z8mbIEUeSTlnH3QbYt3HWZQ+vXI8mvtZoBc2Fz"
         + "lexKeIkAZXCesqGbs6z6nCt16P6tmdfbZF3I3AWzLquPcOXjPf4HgstkyvVBn0Ap"
         + "jAFN418KF/Cx4qyHB4cjdvLrRjjQLnb2+ibo7QU=");

   byte[] cert6 = Base64.decode(
       "MIIEDjCCAvagAwIBAgIEFAAq2jANBgkqhkiG9w0BAQUFADBLMSowKAYDVQQDEyFT"
    + "dW4gTWljcm9zeXN0ZW1zIEluYyBDQSAoQ2xhc3MgQikxHTAbBgNVBAoTFFN1biBN"
    + "aWNyb3N5c3RlbXMgSW5jMB4XDTA0MDIyOTAwNDMzNFoXDTA5MDMwMTAwNDMzNFow"
    + "NzEdMBsGA1UEChMUU3VuIE1pY3Jvc3lzdGVtcyBJbmMxFjAUBgNVBAMTDXN0b3Jl"
    + "LnN1bi5jb20wgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAP9ErzFT7MPg2bVV"
    + "LNmHTgN4kmiRNlPpuLGWS7EDIXYBbLeSSOCp/e1ANcOGnsuf0WIq9ejd/CPyEfh4"
    + "sWoVvQzpOfHZ/Jyei29PEuxzWT+4kQmCx3+sLK25lAnDFsz1KiFmB6Y3GJ/JSjpp"
    + "L0Yy1R9YlIc82I8gSw44y5JDABW5AgMBAAGjggGQMIIBjDAOBgNVHQ8BAf8EBAMC"
    + "BaAwHQYDVR0OBBYEFG1WB3PApZM7OPPVWJ31UrERaoKWMEcGA1UdIARAMD4wPAYL"
    + "YIZIAYb3AIN9k18wLTArBggrBgEFBQcCARYfaHR0cDovL3d3dy5zdW4uY29tL3Br"
    + "aS9jcHMuaHRtbDCBhQYDVR0fBH4wfDB6oCegJYYjaHR0cDovL3d3dy5zdW4uY29t"
    + "L3BraS9wa2lzbWljYS5jcmyiT6RNMEsxKjAoBgNVBAMTIVN1biBNaWNyb3N5c3Rl"
    + "bXMgSW5jIENBIChDbGFzcyBCKTEdMBsGA1UEChMUU3VuIE1pY3Jvc3lzdGVtcyBJ"
    + "bmMwHwYDVR0jBBgwFoAUT7ZnqR/EEBSgG6h1wdYMI5RiiWswVAYIKwYBBQUHAQEE"
    + "SDBGMB0GCCsGAQUFBzABhhFodHRwOi8vdmEuc3VuLmNvbTAlBggrBgEFBQcwAYYZ"
    + "aHR0cDovL3ZhLmNlbnRyYWwuc3VuLmNvbTATBgNVHSUEDDAKBggrBgEFBQcDATAN"
    + "BgkqhkiG9w0BAQUFAAOCAQEAq3byQgyU24tBpR07iQK7agm1zQyzDQ6itdbji0ln"
    + "T7fOd5Pnp99iig8ovwWliNtXKAmgtJY60jWz7nEuk38AioZJhS+RPWIWX/+2PRV7"
    + "s2aWTzM3n43BypD+jU2qF9c9kDWP/NW9K9IcrS7SfU/2MZVmiCMD/9FEL+CWndwE"
    + "JJQ/oenXm44BFISI/NjV7fMckN8EayPvgtzQkD5KnEiggOD6HOrwTDFR+tmAEJ0K"
    + "ZttQNwOzCOcEdxXTg6qBHUbONdL7bjTT5NzV+JR/bnfiCqHzdnGwfbHzhmrnXw8j"
    + "QCVXcfBfL9++nmpNNRlnJMRdYGeCY6OAfh/PRo8/fXak1Q==");
   
   byte[] cert7 = Base64.decode(
     "MIIFJDCCBAygAwIBAgIKEcJZuwAAAAAABzANBgkqhkiG9w0BAQUFADAPMQ0wCwYD"
    + "VQQDEwRNU0NBMB4XDTA0MDUyMjE2MTM1OFoXDTA1MDUyMjE2MjM1OFowaTEbMBkG"
    + "CSqGSIb3DQEJCBMMMTkyLjE2OC4xLjMzMScwJQYJKoZIhvcNAQkCExhwaXhmaXJl"
    + "d2FsbC5jaXNjb3BpeC5jb20xITAfBgNVBAMTGHBpeGZpcmV3YWxsLmNpc2NvcGl4"
    + "LmNvbTB8MA0GCSqGSIb3DQEBAQUAA2sAMGgCYQCbcsY7vrjweXZiFQdhUafEjJV+"
    + "HRy5UKmuCy0237ffmYrN+XNLw0h90cdCSK6KPZebd2E2Bc2UmTikc/FY8meBT3/E"
    + "O/Osmywzi++Ur8/IrDvtuR1zd0c/xEPnV1ZRezkCAwEAAaOCAs4wggLKMAsGA1Ud"
    + "DwQEAwIFoDAdBgNVHQ4EFgQUzJBSxkQiN9TKvhTMQ1/Aq4gZnHswHwYDVR0jBBgw"
    + "FoAUMsxzXVh+5UKMNpwNHmqSfcRYfJ4wgfcGA1UdHwSB7zCB7DCB6aCB5qCB44aB"
    + "r2xkYXA6Ly8vQ049TVNDQSxDTj1NQVVELENOPUNEUCxDTj1QdWJsaWMlMjBLZXkl"
    + "MjBTZXJ2aWNlcyxDTj1TZXJ2aWNlcyxDTj1Db25maWd1cmF0aW9uLERDPWludCxE"
    + "Qz1wcmltZWtleSxEQz1zZT9jZXJ0aWZpY2F0ZVJldm9jYXRpb25MaXN0P2Jhc2U/"
    + "b2JqZWN0Q2xhc3M9Y1JMRGlzdHJpYnV0aW9uUG9pbnSGL2h0dHA6Ly9tYXVkLmlu"
    + "dC5wcmltZWtleS5zZS9DZXJ0RW5yb2xsL01TQ0EuY3JsMIIBEAYIKwYBBQUHAQEE"
    + "ggECMIH/MIGqBggrBgEFBQcwAoaBnWxkYXA6Ly8vQ049TVNDQSxDTj1BSUEsQ049"
    + "UHVibGljJTIwS2V5JTIwU2VydmljZXMsQ049U2VydmljZXMsQ049Q29uZmlndXJh"
    + "dGlvbixEQz1pbnQsREM9cHJpbWVrZXksREM9c2U/Y0FDZXJ0aWZpY2F0ZT9iYXNl"
    + "P29iamVjdENsYXNzPWNlcnRpZmljYXRpb25BdXRob3JpdHkwUAYIKwYBBQUHMAKG"
    + "RGh0dHA6Ly9tYXVkLmludC5wcmltZWtleS5zZS9DZXJ0RW5yb2xsL01BVUQuaW50"
    + "LnByaW1la2V5LnNlX01TQ0EuY3J0MCwGA1UdEQEB/wQiMCCCGHBpeGZpcmV3YWxs"
    + "LmNpc2NvcGl4LmNvbYcEwKgBITA/BgkrBgEEAYI3FAIEMh4wAEkAUABTAEUAQwBJ"
    + "AG4AdABlAHIAbQBlAGQAaQBhAHQAZQBPAGYAZgBsAGkAbgBlMA0GCSqGSIb3DQEB"
    + "BQUAA4IBAQCa0asiPbObLJjpSz6ndJ7y4KOWMiuuBc/VQBnLr7RBCF3ZlZ6z1+e6"
    + "dmv8se/z11NgateKfxw69IhLCriA960HEgX9Z61MiVG+DrCFpbQyp8+hPFHoqCZN"
    + "b7upc8k2OtJW6KPaP9k0DW52YQDIky4Vb2rZeC4AMCorWN+KlndHhr1HFA14HxwA"
    + "4Mka0FM6HNWnBV2UmTjBZMDr/OrGH1jLYIceAaZK0X2R+/DWXeeqIga8jwP5empq"
    + "JetYnkXdtTbEh3xL0BX+mZl8vDI+/PGcwox/7YjFmyFWphRMxk9CZ3rF2/FQWMJP"
    + "YqQpKiQOmQg5NAhcwffLAuVjVVibPYqi");

   byte[] cert8 = Base64.decode(
     "MIIB0zCCATwCAQEwbqBsMGekZTBjMQswCQYDVQQGEwJERTELMAkGA1UECBMCQlkx"
    + "EzARBgNVBAcTClJlZ2Vuc2J1cmcxEDAOBgNVBAoTB0FDIFRlc3QxCzAJBgNVBAsT" 
    + "AkNBMRMwEQYDVQQDEwpBQyBUZXN0IENBAgEBoHYwdKRyMHAxCzAJBgNVBAYTAkRF"
    + "MQswCQYDVQQIEwJCWTETMBEGA1UEBxMKUmVnZW5zYnVyZzESMBAGA1UEChMJQUMg"
    + "SXNzdWVyMRowGAYDVQQLExFBQyBJc3N1ZXIgc2VjdGlvbjEPMA0GA1UEAxMGQUMg"
    + "TWFuMA0GCSqGSIb3DQEBBQUAAgEBMCIYDzIwMDQxMTI2MTI1MjUxWhgPMjAwNDEy"
    + "MzEyMzAwMDBaMBkwFwYDVRhIMRAwDoEMREFVMTIzNDU2Nzg5MA0GCSqGSIb3DQEB"
    + "BQUAA4GBABd4Odx3yEMGL/BvItuT1RafNR2uuWuZbajg0pD6bshUsl+WCIfRiEkq"
    + "lHMkpI7WqAZikdnAEQ5jQsVWEuVejWxR6gjejKxc0fb9qpIui7/GoI5Eh6dmG20e"
    + "xbwJL3+6YYFrZwxR8cC5rPvWrblUR5XKJy+Zp/H5+t9iANnL1L8J");
                
   String[] subjects = 
   {
       "C=AU,ST=Victoria,L=South Melbourne,O=Connect 4 Pty Ltd,OU=Webserver Team,CN=www2.connect4.com.au,E=webmaster@connect4.com.au",
       "C=AU,ST=Victoria,L=South Melbourne,O=Connect 4 Pty Ltd,OU=Certificate Authority,CN=Connect 4 CA,E=webmaster@connect4.com.au",
       "C=AU,ST=QLD,CN=SSLeay/rsa test cert",
       "C=US,O=National Aeronautics and Space Administration,SERIALNUMBER=16+CN=Steve Schoch",
       "E=cooke@issl.atl.hp.com,C=US,OU=Hewlett Packard Company (ISSL),CN=Paul A. Cooke",
       "O=Sun Microsystems Inc,CN=store.sun.com",
       "unstructuredAddress=192.168.1.33,unstructuredName=pixfirewall.ciscopix.com,CN=pixfirewall.ciscopix.com"
    };

    public String getName()
    {
        return "Certificate";
    }

    public void checkCertificate(
        int     id,
        byte[]  cert)
        throws Exception
    {
        ByteArrayInputStream bIn = new ByteArrayInputStream(cert);
        ASN1InputStream aIn = new ASN1InputStream(bIn);

        ASN1Sequence      seq = (ASN1Sequence)aIn.readObject();
//        String dump = ASN1Dump.dumpAsString(seq);

        X509CertificateStructure    obj = new X509CertificateStructure(seq);
        TBSCertificateStructure     tbsCert = obj.getTBSCertificate();
        
        if (!tbsCert.getSubject().toString().equals(subjects[id - 1]))
        {
            fail("failed subject test for certificate id " + id + " got " + tbsCert.getSubject().toString());
        }
        
        if (tbsCert.getVersion() == 3)
        {
            X509Extensions                ext = tbsCert.getExtensions();
            if (ext != null)
            {
                Enumeration    en = ext.oids();
                while (en.hasMoreElements())
                {
                    DERObjectIdentifier    oid = (DERObjectIdentifier)en.nextElement();
                    X509Extension            extVal = ext.getExtension(oid);
                    
                    ASN1OctetString        oct = extVal.getValue();
                    ASN1InputStream        extIn = new ASN1InputStream(new ByteArrayInputStream(oct.getOctets()));
                    
                    if (oid.equals(X509Extensions.SubjectKeyIdentifier))
                    {
                        SubjectKeyIdentifier si = SubjectKeyIdentifier.getInstance(extIn.readObject());
                    }
                    else if (oid.equals(X509Extensions.KeyUsage))
                    {
                        DERBitString ku = KeyUsage.getInstance(extIn.readObject());
                    }
                    else if (oid.equals(X509Extensions.ExtendedKeyUsage))
                    {
                        ExtendedKeyUsage ku = ExtendedKeyUsage.getInstance(extIn.readObject());
                        
                        ASN1Sequence    sq = (ASN1Sequence)ku.getDERObject();
                        for (int i = 0; i != sq.size(); i++)
                        {
                            DERObjectIdentifier    p = KeyPurposeId.getInstance(sq.getObjectAt(i));
                        }
                    }
                    else if (oid.equals(X509Extensions.SubjectAlternativeName))
                    {
                        GeneralNames    gn = GeneralNames.getInstance(extIn.readObject());
                        
                        ASN1Sequence    sq = (ASN1Sequence)gn.getDERObject();
                        for (int i = 0; i != sq.size(); i++)
                        {
                            GeneralName    n = GeneralName.getInstance(sq.getObjectAt(i));
                        }
                    }
                    else if (oid.equals(X509Extensions.IssuerAlternativeName))
                    {
                        GeneralNames    gn = GeneralNames.getInstance(extIn.readObject());
                        
                        ASN1Sequence    sq = (ASN1Sequence)gn.getDERObject();
                        for (int i = 0; i != sq.size(); i++)
                        {
                            GeneralName    n = GeneralName.getInstance(sq.getObjectAt(i));
                        }
                    }
                    else if (oid.equals(X509Extensions.CRLDistributionPoints))
                    {
                        CRLDistPoint    p = CRLDistPoint.getInstance(extIn.readObject());
                        
                        DistributionPoint[] points = p.getDistributionPoints();
                        for (int i = 0; i != points.length; i++)
                        {
                            // do nothing
                        }
                    }
                    else if (oid.equals(X509Extensions.CertificatePolicies))
                    {
                        ASN1Sequence    cp = (ASN1Sequence)extIn.readObject();
                        
                        for (int i = 0; i != cp.size(); i++)
                        {
                            PolicyInformation.getInstance(cp.getObjectAt(i));
                        }
                    }
                    else if (oid.equals(X509Extensions.AuthorityKeyIdentifier))
                    {
                        AuthorityKeyIdentifier    auth = AuthorityKeyIdentifier.getInstance(extIn.readObject());
                    }
                    else if (oid.equals(X509Extensions.BasicConstraints))
                    {
                        BasicConstraints    bc = BasicConstraints.getInstance(extIn.readObject());
                    }
                    else
                    {
                        //System.out.println(oid.getId());
                    }
                }
            }
        }
    }


    public void checkAttributeCertificate(
        int     id,
        byte[]  cert)
        throws Exception
    {
        ByteArrayInputStream bIn;
        ASN1InputStream aIn;
        String dump = "";

        bIn = new ByteArrayInputStream(cert);
        aIn = new ASN1InputStream(bIn);

        ASN1Sequence seq = (ASN1Sequence) aIn.readObject();
        dump = ASN1Dump.dumpAsString(seq);

        AttributeCertificate obj = new AttributeCertificate(seq);
        AttributeCertificateInfo acInfo = obj.getAcinfo();

        // Version
        if (!(acInfo.getVersion().equals(new DERInteger(1)))
                && (!(acInfo.getVersion().equals(new DERInteger(2)))))
        {
            fail(
                    "failed AC Version test for id " + id);
        }

        // Holder
        Holder h = acInfo.getHolder();
        if (h == null)
        {
            fail(
                    "failed AC Holder test, it's null, for id " + id);
        }

        // Issuer
        AttCertIssuer aci = acInfo.getIssuer();
        if (aci == null)
        {
            fail(
                    "failed AC Issuer test, it's null, for id " + id);
        }

        // Signature
        AlgorithmIdentifier sig = acInfo.getSignature();
        if (sig == null)
        {
            fail(
                    "failed AC Signature test for id " + id);
        }

        // Serial
        DERInteger serial = acInfo.getSerialNumber();

        // Validity
        AttCertValidityPeriod validity = acInfo.getAttrCertValidityPeriod();
        if (validity == null)
        {
            fail("failed AC AttCertValidityPeriod test for id " + id);
        }

        // Attributes
        ASN1Sequence attribSeq = acInfo.getAttributes();
        Attribute att[] = new Attribute[attribSeq.size()];
        for (int i = 0; i < attribSeq.size(); i++)
        {
            att[i] = Attribute.getInstance(attribSeq.getObjectAt(i));
        }

        // IssuerUniqueId
        // TODO, how to best test?

        // X509 Extensions
        X509Extensions ext = acInfo.getExtensions();
        if (ext != null)
        {
            Enumeration en = ext.oids();
            while (en.hasMoreElements())
            {
                DERObjectIdentifier oid = (DERObjectIdentifier) en
                        .nextElement();
                X509Extension extVal = ext.getExtension(oid);
            }
        }
    }

    public void performTest()
        throws Exception
    {
        checkCertificate(1, cert1);
        checkCertificate(2, cert2);
        checkCertificate(3, cert3);
        checkCertificate(4, cert4);
        checkCertificate(5, cert5);
        checkCertificate(6, cert6);
        checkCertificate(7, cert7);
        checkAttributeCertificate(8,cert8);
    }

    public static void main(
        String[]    args)
    {
        runTest(new CertificateTest());
    }
}
