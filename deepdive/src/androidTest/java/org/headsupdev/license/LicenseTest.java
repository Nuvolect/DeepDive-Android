package org.headsupdev.license;

import android.content.Context;

import com.nuvolect.deepdive.license.AppConfig;

import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Run license utility through its paces.
 */
public class LicenseTest {

    @Test
    public void testLicense() throws Exception {

        String licenseName = "Matt Kraus";
        String licenseAddress = "10151 University Blvd., Suite 343, Orlando, FL 32817";
        String licensePhone = "4075847854";
        String licenseEmail = "matt.kraus@nuvolect.com";
        String licenseDeviceId = "bfdcdf9d4013bd90";
        String licenseDate = "2017/06/16";
        String licensePeriod = "365";
        String encodedLicense = "qYOp7Kvl0kNo3L40ysyaA0sOHnQX/35LZjv4/zqt8nGgfHvNc5ne2wr61gJqUYGvQo4VAEdIfAyAqKCyucBb6vpiWb7GYBjpHECb8kv7rrPI9LfcvprsKRn6KqsldcjCDSb/2fGi0dNt/fjjmOegQACmDUR+ke4EAhEKUn6BKEHv502pxYkAlSdLwiyvEEGOPgVzJwE5mzuuSNvlftWYbFgJ/JLptjlMlu52juILh5UzOfffr7VsitJOrPMtaGNaN6bZsaZ/VcA12pM+/urkigO12k4vrvsPKxbSQiHOjq3UhUP82pWXB8GL2x1oSdN7uaUrC2tPVTOMnPvYVwuFQyPnmuL/dhGdGfoqqyV1yMJih8FVpkv0CVRwygejgm5J3erTUZkwjMhTVn+tbrRXxjVVO/7hw55QHTSVIq8Q66c6LGGGkTglpFQ/ozFFZMkGr0/nGRwK+2TOON+1KLtujyIARSNGvsgQagxLF1wvohvRwqj6o3VV+JK/CIFQIsbDBje8kAwqjIZL6r+BQzET45prpu0gQSYLFLt4bkt4hCmx8ATso38jBg==";

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

        assertThat( out.getLicenseName(),       is( licenseName));
        assertThat( out.getLicenseAddress(),    is( licenseAddress));
        assertThat( out.getLicensePhone(),      is( licensePhone));
        assertThat( out.getLicenseEmail(),      is( licenseEmail));
        assertThat( out.getLicenseDeviceId(),   is( licenseDeviceId));
        assertThat( out.getLicenseDate(),       is( licenseDate));
        assertThat( out.getLicensePeriodDays(), is( licensePeriod));
    }
}