/*
 * Copyright 2010-2011 Heads Up Development Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.headsupdev.license;

import java.util.Properties;

/**
 * @author Andrew Williams
 * @version $Id: License.java 76 2012-03-17 23:17:03Z handyande $
 * @since 1.0
 */
public class License {
    private static final String LICENSE_NAME        = "license.name";
    private static final String LICENSE_ADDRESS     = "license.address";
    private static final String LICENSE_PHONE       = "license.phone";
    private static final String LICENSE_EMAIL       = "license.email";
    private static final String LICENSE_DEVICE_ID   = "license.device.id";
    private static final String LICENSE_DATE        = "license.date";
    private static final String LICENSE_PERIOD_DAYS = "license.period.days";

    private Properties properties = new Properties();

    public License() {
    }

    public String getLicenseTitle()      { return "Basic License File"; }
    public String getLicenseName()       { return getProperty(LICENSE_NAME); }
    public String getLicenseAddress()    { return getProperty(LICENSE_ADDRESS); }
    public String getLicensePhone()      { return getProperty(LICENSE_PHONE); }
    public String getLicenseEmail()      { return getProperty(LICENSE_EMAIL); }
    public String getLicenseDeviceId()   { return getProperty(LICENSE_DEVICE_ID); }
    public String getLicenseDate()       { return getProperty(LICENSE_DATE); }
    public String getLicensePeriodDays() { return getProperty(LICENSE_PERIOD_DAYS); }

    public void setLicenseName(String licensedName)            { setProperty(LICENSE_NAME, licensedName); }
    public void setLicenseAddress(String licenseAddress)       { setProperty(LICENSE_ADDRESS, licenseAddress); }
    public void setLicensePhone(String licensePhone)           { setProperty(LICENSE_PHONE, licensePhone); }
    public void setLicenseEmail(String licenseEmail)           { setProperty(LICENSE_EMAIL, licenseEmail); }
    public void setLicenseDeviceId(String licenseDeviceId)     { setProperty(LICENSE_DEVICE_ID, licenseDeviceId); }
    public void setLicenseDate(String licenseDate)             { setProperty(LICENSE_DATE, licenseDate); }
    public void setLicensePeriodDays(String licensePeriodDays) { setProperty(LICENSE_PERIOD_DAYS, licensePeriodDays); }

    public String getSummary(){

        String newLine = System.lineSeparator();

        return "Licensed to: "+ getProperty(LICENSE_NAME) + newLine
                +"User address: "  + getProperty(LICENSE_ADDRESS) + newLine
                +"User email: "    + getProperty(LICENSE_EMAIL) + newLine
                +"User phone: "    + getProperty(LICENSE_PHONE) + newLine
                +"Device ID: "     + getProperty(LICENSE_DEVICE_ID) + newLine
                +"License date: "  + getProperty(LICENSE_DATE) + newLine
                +"License period:" + getProperty(LICENSE_PERIOD_DAYS);
    }

    protected String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    protected String getProperty(String key, String deflt) {
        return getProperties().getProperty(key, deflt);
    }

    protected void setProperty(String key, String value) {
        getProperties().setProperty(key, value);
    }

    protected Properties getProperties() {
        return properties;
    }

}
