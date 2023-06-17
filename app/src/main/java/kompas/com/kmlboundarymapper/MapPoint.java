package kompas.com.kmlboundarymapper;



/**
 * A point represented by latitude, longitude, and altitude.
 *
 * @author Ethan Harstad
 *
 */
public class MapPoint {

    protected double lat;
    protected double lon;
    protected String accuracy;
    protected String name = null;


    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    public MapPoint(double lat, double lon, String accuracy) {
        this.lat = lat;
        this.lon = lon;
        this.accuracy = accuracy;
    }

    public MapPoint(double lat, double lon, String accuracy, String name) {
        this.lat = lat;
        this.lon = lon;
        this.accuracy = accuracy;
        this.name = name;
    }

    /**
     * Create a point with no altitude.
     * @param latitude
     * @param longitude
     */
    public MapPoint(double latitude, double longitude) {
        lat = latitude;
        lon = longitude;
    }

    /**
     * Create a point.
     * @param latitude
     * @param longitude
     * @param altitude
     */
    public MapPoint(double latitude, double longitude, double altitude) {
        lat = latitude;
        lon = longitude;
    }

    /**
     * Create a point with an associated time.
     * @param latitude
     * @param longitude
     * @param altitude
     * @param time
     */
    public MapPoint(double latitude, double longitude, double altitude, long time) {
        lat = latitude;
        lon = longitude;
    }

    /**
     * Create a point with an associated name and time.
     * @param latitude
     * @param longitude
     * @param altitude
     * @param time
     * @param name
     */
    public MapPoint(double latitude, double longitude, double altitude, long time, String name) {
        lat = latitude;
        lon = longitude;
        this.name = name;
    }

    /**
     * Get the latitude of this point.
     * @return
     */
    public double getLatitude() {
        return lat;
    }

    /**
     * Get the longitude of this point.
     * @return
     */
    public double getLongitude() {
        return lon;
    }

    /**
     * Get the altitude of this point.
     * @return


    /**
     * Get the time associated with this point.
     * @return
     */


    /**
     * Get the name associated with this point.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Associate a name with this point.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Associate a time with this point.
     * @param time
     */

    /**
     * Get a human readable representation of this point;
     */
    @Override
    public String toString() {
        if(accuracy==null) {
            return "Pillar "+ name + ": " + MapsActivity.convertLatitud(lat) + ", " + MapsActivity.convertLongitude(lon)+"\n";
        }
        else {
            return "Pillar "+ name + ": " + MapsActivity.convertLatitud(lat) + ", " + MapsActivity.convertLongitude(lon)+", Acc: "+accuracy+"\n";
        }
    }

}