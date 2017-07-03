package com.ale.util;

import com.ale.util.log.Log;
import com.neovisionaries.i18n.CountryCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by georges on 09/12/2016.
 */

public final class LocaleUtil {

    private static final String LOG_TAG = "LocaleUtil";

    private static String[] FAVORITES_COUNTRIES_CODE = {"AU","AT","BE","BR","CA","CN","CZ","DK",
            "FI","FR","DE","HK","IL","MX","NL","NO","PL",
            "RU","ZA","KR","ES","CH","TW", "TR","GB","US"};

    public static List<String> getSortedCountriesFiltered(List<String> filterCountries) {
        Locale[] locale = Locale.getAvailableLocales();
        List<String> allCountries = new ArrayList<>();
        for( Locale loc : locale ){
            String country = loc.getDisplayCountry();
            if( !StringsUtil.isNullOrEmpty(country) && !allCountries.contains(country) ){
                if( !filterCountries.contains(country)) {
                    //Log.getLogger().verbose(LOG_TAG, "LOCALE; "+loc.getCountry()+" / country; "+loc.getDisplayCountry());
                    allCountries.add(country);
                }
            }
        }
        Collections.sort(allCountries, String.CASE_INSENSITIVE_ORDER);
        return allCountries;
    }

    public static List<String> getSortedCountries() {
        Locale[] locale = Locale.getAvailableLocales();
        List<String> allCountries = new ArrayList<>();
        for( Locale loc : locale ){
            String country = loc.getDisplayCountry();
            if( !StringsUtil.isNullOrEmpty(country) && !allCountries.contains(country) ){
                //Log.getLogger().verbose(LOG_TAG, "LOCALE; "+loc.getCountry()+" / country; "+loc.getDisplayCountry());
                allCountries.add( country );
            }
        }
        Collections.sort(allCountries, String.CASE_INSENSITIVE_ORDER);
        return allCountries;
    }

    public static List<String> getSortedMainCountries() {
        List<String> mainCountries = new ArrayList<>();

        for( String countryCode : FAVORITES_COUNTRIES_CODE ){
            Locale loc = findLocalFromCountryCode(countryCode);
            if( loc != null ){
                mainCountries.add( loc.getDisplayCountry() );
            }
        }
        Collections.sort(mainCountries, String.CASE_INSENSITIVE_ORDER);
        return mainCountries;
    }

    public static Locale findLocalFromIsoCountryCode(String codeCountry) {
        if( StringsUtil.isNullOrEmpty(codeCountry) ) {
            Log.getLogger().warn(LOG_TAG, "Country code parameter is EMPTY");
            return null;
        }

        CountryCode cc = CountryCode.getByCodeIgnoreCase(codeCountry);
        if( cc != null) {
            Locale locale = cc.toLocale();
            if (locale != null) {
                for (Locale loc : Locale.getAvailableLocales()) {
                    if (locale.getCountry().equals(loc.getCountry())) {
                        //Log.getLogger().info(LOG_TAG, "Country found ; CountryCode=" + loc.getCountry() + " / displayCountry; " + loc.getDisplayCountry());
                        return (loc);
                    }
                }
            }
        }

        Log.getLogger().warn(LOG_TAG, "Country NO found ; " + codeCountry);
        return null;
    }

    public static Locale findLocalFromCountryCode(String codeCountry) {

        for( Locale loc : Locale.getAvailableLocales() ) {
            String country = loc.getCountry();
            if (!StringsUtil.isNullOrEmpty(country) && country.equalsIgnoreCase(codeCountry)) {
                //Log.getLogger().verbose(LOG_TAG, "Country found ; LOCALE=" + loc.getCountry() + " / displayCountry; " + loc.getDisplayCountry());
                return (loc);
            }
        }

        Log.getLogger().warn(LOG_TAG, "Country NO found ; " + codeCountry);
        return null;
    }

    public static Locale findLocalFromDisplayCountry(String country) {

        for( Locale loc : Locale.getAvailableLocales() ) {
            String displayCountry = loc.getDisplayCountry();
            if (!StringsUtil.isNullOrEmpty(displayCountry) && displayCountry.equalsIgnoreCase(country)) {
                //Log.getLogger().verbose(LOG_TAG, "Country found ; LOCALE=" + loc.getCountry() + " / displayCountry; " + loc.getDisplayCountry());
                return (loc);
            }
        }

        Log.getLogger().warn(LOG_TAG, "Country NO found ; " + country);
        return null;
    }

    public static String getIsoCountryFromCountryCode(String countryCode) {
        Log.getLogger().verbose(LOG_TAG, ">getIsoCountryFromCountryCode: "+countryCode);

        CountryCode cc = CountryCode.getByCodeIgnoreCase(countryCode);
        if( cc != null) {
            return cc.getAlpha3();
        }

        return Locale.getDefault().getISO3Country();
    }

    public static String getIsoCountryFromDisplayCountry(String country) {
        Log.getLogger().verbose(LOG_TAG, ">getIsoCountryFromDisplayCountry: "+country);

        try {
            Locale loc = LocaleUtil.findLocalFromDisplayCountry(country);
            if (loc != null) {
                return loc.getISO3Country();
            }
        }
        catch (Exception e) {
            Log.getLogger().error(LOG_TAG, "Exception: " + e.getMessage());
        }

        return Locale.getDefault().getISO3Country();
    }

}
