package com.ale.infra.manager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Pair;

import com.ale.infra.application.RainbowContext;
import com.ale.util.StringsUtil;
import com.ale.util.log.Log;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by wilsius on 20/04/17.
 */



public class LocationMgr implements LocationListener {
    private static String TAG = "LocationMgr";
    private LocationManager mLocationManager;
    private Context m_context;
    private Location location;
    private Location berlin, houston, delhi, sydney;

    public static enum Region {
        EUROPA,
        ASIA,
        AMERICA,
        OCEANIA,
        NONE;

        private Region() {
        }
    }



    public LocationMgr(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        buildLocation();

        try {
            // getting network status
            boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                // First get location from Network Provider
                if (isNetworkEnabled) {
                    Log.getLogger().info(TAG, "Network location enabled");
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1000, this);
                        Log.getLogger().info(TAG, "Network location available");
                        if (mLocationManager != null) {
                            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                    }
                }

        } catch (Exception e) {
            Log.getLogger().info(TAG, "Exception" + e.toString());
        }
    }

    private void buildLocation() {
        berlin = new Location(LocationManager.NETWORK_PROVIDER);
        berlin.setLatitude(52.5243700);
        berlin.setLongitude(13.4105300);

        houston = new Location(LocationManager.NETWORK_PROVIDER);
        houston.setLatitude(29.7630556);
        houston.setLongitude(-95.3630556);

        delhi = new Location(LocationManager.NETWORK_PROVIDER);
        delhi.setLatitude(26.644800);
        delhi.setLongitude(77.216721);

        sydney = new Location(LocationManager.NETWORK_PROVIDER);
        sydney.setLatitude(-33.865143);
        sydney.setLongitude(151.209900);

    }

    public Region calculRegion () {

//        location  = new Location(LocationManager.NETWORK_PROVIDER);
// New York
//        location.setLatitude(40.785091);
//        location.setLongitude(-73.968285);
// Shanghai
//        location.setLatitude(31.2304);
//        location.setLongitude(121.4737);
        if (location == null) return calculateUserCountryRegion();

        float distanceEurope = berlin.distanceTo(location);
        float distanceAmerica = houston.distanceTo(location);
        float distanceAsia = delhi.distanceTo(location);
        float distanceOc = sydney.distanceTo(location);

        float min = Math.min(distanceEurope,distanceAmerica);
        min = Math.min(min,distanceAsia);
        min = Math.min(min,distanceOc);

        if (min == distanceEurope) return Region.EUROPA;
        if (min == distanceAmerica) return Region.AMERICA;
        if (min == distanceAsia) return Region.ASIA;
        if (min == distanceOc) return Region.OCEANIA;


        return Region.NONE;
    }

    private Region calculateUserCountryRegion() {
        if (RainbowContext.getInfrastructure().getContactCacheMgr() == null)
            return Region.NONE;

        String country = RainbowContext.getInfrastructure().getContactCacheMgr().getUser().getCountry();

        Log.getLogger().info(TAG, "Network location enabled" + country);
        String region = null;

        for (int i = 0; i < regionPerCountry.length; i++)
        {
            if (regionPerCountry[i][0].equalsIgnoreCase(country)) {
                region = regionPerCountry[i][1];
                break;
            }
        }

        if (StringsUtil.isNullOrEmpty(region))
            return Region.NONE;

        if (region.contains("AFRICA"))
            return Region.EUROPA;
        if (region.contains("AMERICA"))
            return Region.AMERICA;
        if (region.contains("ASIA"))
            return Region.ASIA;
        if (region.contains("EUROPA"))
            return Region.EUROPA;
        if (region.contains("OCEANIA"))
            return Region.OCEANIA;
        if (region.contains("INDIA"))
            return Region.ASIA;
        if (region.contains("MIDDLE EAST"))
            return Region.EUROPA;


        return Region.NONE;
    }


    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public boolean isPermissionAllowed(String androidPermission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // This Build is < 6 , you can Access to permission
            return true;
        }
        if (m_context.checkSelfPermission(androidPermission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;
    }




static final String[][] regionPerCountry = {
        {"ABW", "SOUTH AMERICA"},
        {"AFG", "CENTRAL ASIA"},
        {"AGO", "AFRICA"},
        {"AIA", "NORTH AMERICA"},
        {"ALA", "NORTH EUROPA"},
        {"ALB", "SOUTH EUROPA"},
        {"AND", "SOUTH EUROPA"},
        {"ARE", "MIDDLE EAST"},
        {"ARG", "SOUTH AMERICA"},
        {"ARM", "MIDDLE EAST"},
        {"ASM", "OCEANIA"},
        {"ATA", "SOUTH AMERICA"},
        {"ATF", "SOUTH AMERICA"},
        {"ATG", "SOUTH AMERICA"},
        {"AUS", "OCEANIA"},
        {"AUT", "NORTH EUROPA"},
        {"AZE", "MIDDLE EAST"},
        {"BDI", "AFRICA"},
        {"BEL", "NORTH EUROPA"},
        {"BEN", "AFRICA"},
        {"BES", "SOUTH AMERICA"},
        {"BFA", "AFRICA"},
        {"BGD", "SOUTH ASIA"},
        {"BGR", "NORTH EUROPA"},
        {"BHR", "MIDDLE EAST"},
        {"BHS", "NORTH AMERICA"},
        {"BIH", "NORTH EUROPA"},
        {"BLM", "SOUTH AMERICA"},
        {"BLR", "NORTH EUROPA"},
        {"BLZ", "NORTH AMERICA"},
        {"BMU", "NORTH AMERICA"},
        {"BOL", "SOUTH AMERICA"},
        {"BRA", "SOUTH AMERICA"},
        {"BRB", "NORTH AMERICA"},
        {"BRN", "MIDDLE EAST"},
        {"BTN", "SOUTH ASIA"},
        {"BVT", "AFRICA"},
        {"BWA", "AFRICA"},
        {"CAF", "AFRICA"},
        {"CAN", "NORTH AMERICA"},
        {"CCK", "INDIA"},
        {"CHE", "NORTH EUROPA"},
        {"CHL", "SOUTH AMERICA"},
        {"CHN", "EAST ASIA"},
        {"CIV", "AFRICA"},
        {"CMR", "INDIA"},
        {"COD", "AFRICA"},
        {"COG", "AFRICA"},
        {"COK", "OCEANIA"},
        {"COL", "SOUTH AMERICA"},
        {"COM", "INDIA"},
        {"CPV", "AFRICA"},
        {"CRI", "SOUTH AMERICA"},
        {"CUB", "NORTH EUROPA"},
        {"CUW", "SOUTH AMERICA"},
        {"CXR", "OCEANIA"},
        {"CYM", "SOUTH AMERICA"},
        {"CYP", "SOUTH EUROPA"},
        {"CZE", "SOUTH EUROPA"},
        {"DEU", "NORTH EUROPA"},
        {"DJI", "AFRICA"},
        {"DMA", "NORTH AMERICA"},
        {"DNK", "NORTH EUROPA"},
        {"DOM", "NORTH EUROPA"},
        {"DZA", "AFRICA"},
        {"ECU", "SOUTH AMERICA"},
        {"EGY", "MIDDLE EAST"},
        {"ERI", "AFRICA"},
        {"ESH", "AFRICA"},
        {"ESP", "SOUTH EUROPA"},
        {"EST", "NORTH EUROPA"},
        {"ETH", "AFRICA"},
        {"FIN", "NORTH EUROPA"},
        {"FJI", "OCEANIA"},
        {"FLK", "SOUTH AFRICA"},
        {"FRA", "SOUTH EUROPA"},
        {"FRO", "NORTH EUROPA"},
        {"FSM", "OCEANIA"},
        {"GAB", "AFRICA"},
        {"GBR", "NORTH EUROPA"},
        {"GEO", "NORTH EUROPA"},
        {"GGY", "NORTH EUROPA"},
        {"GHA", "AFRICA"},
        {"GIB", "SOUTH EUROPA"},
        {"GIN", "AFRICA"},
        {"GLP", "NORTH AMERICA"},
        {"GMB", "AFRICA"},
        {"GNB", "AFRICA"},
        {"GNQ", "AFRICA"},
        {"GRC", "SOUTH EUROPA"},
        {"GRD", "NORTH AMERICA"},
        {"GRL", "NORTH AMERICA"},
        {"GTM", "SOUTH AMERICA"},
        {"GUF", "SOUTH AMERICA"},
        {"GUM", "OCEANIA"},
        {"GUY", "SOUTH AMERICA"},
        {"HKG", "EAST ASIA"},
        {"HMD", "SOUTH EUROPA"},
        {"HND", "SOUTH AMERICA"},
        {"HRV", "SOUTH EUROPA"},
        {"HTI", "NORTH AMERICA"},
        {"HUN", "SOUTH EUROPA"},
        {"IDN", "INDIA"},
        {"IMN", "NORTH EUROPA"},
        {"IND", "INDIA"},
        {"IOT", "INDIA"},
        {"IRL", "NORTH EUROPA"},
        {"IRN", "MIDDLE EAST"},
        {"IRQ", "MIDDLE EAST"},
        {"ISL", "NORTH EUROPA"},
        {"ISR", "MIDDLE EAST"},
        {"ITA", "SOUTH EUROPA"},
        {"JAM", "NORTH AMERICA"},
        {"JEY", "NORTH EUROPA"},
        {"JOR", "MIDLE ASIA"},
        {"JPN", "EAST ASIA"},
        {"KAZ", "NORTH ASIA"},
        {"KEN", "AFRICA"},
        {"KGZ", "NORTH ASIA"},
        {"KHM", "SOUTH INDIA"},
        {"KIR", "OCEANIA"},
        {"KNA", "NORTH AMERICA"},
        {"KOR", "EAST ASIA"},
        {"KWT", "MIDDLE EAST"},
        {"LAO", "SOUTH ASIA"},
        {"LBN", "MIDDLE EAST"},
        {"LBR", "AFRICA"},
        {"LBY", "AFRICA"},
        {"LCA", "NORTH AMERICA"},
        {"LIE", "NORTH EUROPA"},
        {"LKA", "SOUTH ASIA"},
        {"LSO", "AFRICA"},
        {"LTU", "NORTH AMERICA"},
        {"LUX", "NORTH EUROPA"},
        {"LVA", "NORTH EUROPA"},
        {"MAC", "EAST ASIA"},
        {"MAF", "NORTH AMERICA"},
        {"MAR", "AFRICA"},
        {"MCO", "SOUTH EUROPA"},
        {"MDA", "NORTH EUROPA"},
        {"MDG", "AFRICA"},
        {"MDV", "AFRICA"},
        {"MEX", "NORTH AMERICA"},
        {"MHL", "OCEANIE"},
        {"MKD", "SOUTH EUROPA"},
        {"MLI", "AFRICA"},
        {"MLT", "SOUTH EUROPA"},
        {"MMR", "INDIA"},
        {"MNE", "SOUTH EUROPA"},
        {"MNG", "NORTH ASIA"},
        {"MNP", "SOUTH INDIA"},
        {"MOZ", "AFRICA"},
        {"MRT", "AFRICA"},
        {"MSR", "NORTH AMERICA"},
        {"MTQ", "NORTH AMERICA"},
        {"MUS", "INDIA"},
        {"MWI", "AFRICA"},
        {"MYS", "SOUTH ASIA"},
        {"MYT", "INDIA"},
        {"NAM", "AFRICA"},
        {"NCL", "OCEANIA"},
        {"NER", "AFRICA"},
        {"NFK", "NORTH EUROPA"},
        {"NGA", "AFRICA"},
        {"NIC", "SOUTH AMERICA"},
        {"NIU", "OCEANIA"},
        {"NLD", "NORTH EUROPA"},
        {"NOR", "NORTH EUROPA"},
        {"NPL", "INDIA"},
        {"NRU", "OCEANIA"},
        {"NZL", "OCEANIA"},
        {"OMN", "MIDDLE EAST"},
        {"PAK", "INDIA"},
        {"PAN", "NORTH AMERICA"},
        {"PCN", "OCEANIA"},
        {"PER", "NORTH AMERICA"},
        {"PHL", "SOUTH ASIA"},
        {"PLW", "OCEANIA"},
        {"PNG", "OCEANIA"},
        {"POL", "NORTH EUROPA"},
        {"PRI", "SOUTH AMERICA"},
        {"PRK", "EAST ASIA"},
        {"PRT", "SOUTH EUROPA"},
        {"PRY", "SOUTH AMERICA"},
        {"PSE", "MIDDLE EAST"},
        {"PYF", "OCEANIA"},
        {"QAT", "MIDDLE EAST"},
        {"REU", "AFRICA"},
        {"ROU", "NORTH AMERICA"},
        {"RUS", "NORTH EUROPA"},
        {"RWA", "AFRICA"},
        {"SAU", "MIDDLE EAST"},
        {"SDN", "AFRICA"},
        {"SEN", "AFRICA"},
        {"SGP", "SOUTH ASIA"},
        {"SGS", "SOUTH ASIA"},
        {"SHN", "NORTH EUROPA"},
        {"SJM", "NORTH EUROPA"},
        {"SLB", "OCEANIA"},
        {"SLE", "AFRICA"},
        {"SLV", "SOUTH AMERICA"},
        {"SMR", "SOUTH EUROPA"},
        {"SOM", "AFRICA"},
        {"SPM", "AFRICA"},
        {"SRB", "SOUTH EUROPA"},
        {"SSD", "AFRICA"},
        {"STP", "AFRICA"},
        {"SUR", "EAST ASIA"},
        {"SVK", "NORTH EUROPA"},
        {"SVN", "NORTH EUROPA"},
        {"SWE", "NORTH EUROPA"},
        {"SWZ", "AFRICA"},
        {"SXM", "NORTH AMERICA"},
        {"SYC", "INDIA"},
        {"SYR", "AFRICA"},
        {"TCA", "NORTH AMERICA"},
        {"TCD", "AFRICA"},
        {"TGO", "AFRICA"},
        {"THA", "SOUTH EAST"},
        {"TJK", "NORTH ASIA"},
        {"TKL", "OCEANIA"},
        {"TKM", "NORTH ASIA"},
        {"TLS", "OCEANIA"},
        {"TON", "OCEANIA"},
        {"TTO", "NORTH AMERICA"},
        {"TUN", "AFRICA"},
        {"TUR", "SOUTH EUROPA"},
        {"TUV", "OCEANIA"},
        {"TWN", "EAST ASIA"},
        {"TZA", "AFRICA"},
        {"UGA", "AFRICA"},
        {"UKR", "NORTH EUROPA"},
        {"UMI", "NORTH AMERICA"},
        {"URY", "SOUTH AMERICA"},
        {"USA", "NORTH AMERICA"},
        {"UZB", "NORTH ASIA"},
        {"VAT", "SOUTH EUROPA"},
        {"VCT", "NORTH AMERICA"},
        {"VEN", "SOUTH AMERICA"},
        {"VGB", "NORTH AMERICA"},
        {"VIR", "NORTH AMERICA"},
        {"VNM", "SOUTH ASIA"},
        {"VUT", "OCEANIA"},
        {"WLF", "OCEANIA"},
        {"WSM", "OCEANIA"},
        {"YEM", "AFRICA"},
        {"ZAF", "AFRICA"},
        {"ZMB", "AFRICA"},
        {"ZWE", "AFRICA"}
};

}
