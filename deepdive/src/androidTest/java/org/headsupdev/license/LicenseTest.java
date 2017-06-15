package org.headsupdev.license;

import android.content.Context;

import com.nuvolect.deepdive.license.AppConfig;

import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Run license utility through its paces.
 */
public class LicenseTest {

    @Test
    public void testLicense() throws Exception {

        String encodedLicense = "qYOp7Kvl0kNo3L40ysyaA0sOHnQX/35LoRZJGHD3Rw4Jvaj9uQPQlQr61gJqUYGvQo4VAEdIfAzY76tgkFD/9eY+idFGAbzwwwkxbmvmry5cycxRjftGnOIZ4urAcPpl8wJbtVe5bB6BtdW+6QGYZWmp1v3vX9K8QPL9iRglaVoRiq4QF7p++6I3DftzI/GBRwlR9H/9oF5t/fjjmOegQACmDUR+ke4EndiCQC4MYZyFccuJI25l/kyQ7VK9OJadiaSLBWAd9/DXCrikFgJdwgiU451JredMk7L9fGvTgyUZuQ7nYBqX1uYcbldGLvhoUCoS7Hko+yZIYiZWmcHh3zBnL2vREdArpt7swcGKLMj7rl6YPUiNaPmUGiambQZ+kmaIT2laqaBilGTM27vGJA==";
        String installId = "bfdcdf9d4013bd90";
        String licenseDate = "2017/06/12";
        String licensePeriod = "365";
        String licenseTo = "matt.kraus@nuvolect.com";

        Context ctx = getTargetContext();

        AppConfig config = new AppConfig();
        License out = new License();
        boolean decoderSetupSuccess = true;

        LicenseDecoder decoder = new LicenseDecoder();
        try {
            decoder.setPublicKey( LicenseUtils.deserialiseKey( config.getPublicKeyFile(ctx) ) );
            decoder.setSharedKey( LicenseUtils.deserialiseKey( config.getSharedKeyFile(ctx) ) );
        } catch (Exception e) {
            decoderSetupSuccess = false;
        }

        assertTrue(decoderSetupSuccess);

        boolean decodeLicense = true;
        try {
            decoder.decodeLicense( encodedLicense, out );
        } catch ( Exception e ) {
            decodeLicense = false;
        }
        assertTrue(decodeLicense);

        String resultLicenseTo = out.getLicensedTo();

        assertThat( out.getLicensedTo(), is( licenseTo));
        assertThat( out.getLicenseInstallId(), is( installId));
        assertThat( out.getLicenseDate(), is(licenseDate));
        assertThat( out.getLicensePeriodDays(), is( licensePeriod));
    }
}