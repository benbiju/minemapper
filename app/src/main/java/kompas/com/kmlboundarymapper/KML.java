package kompas.com.kmlboundarymapper;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class KML {

    private Document doc;
    private Element root;
    private Element folder;

    /**
     * Create a KML object.
     */
    public KML() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            Element kml = doc.createElementNS("http://www.opengis.net/kml/2.2", "kml");
            doc.appendChild(kml);
            root = doc.createElement("Document");
            folder = doc.createElement("Folder");
            root.appendChild(folder);
           // Element boundStyle = doc.createElement()
            kml.appendChild(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a placemark to this KML object.
     * @param mark
     */
    public void addMark(MapPoint mark) {
        Element placemark = doc.createElement("Placemark");
        folder.appendChild(placemark);

        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(mark.getName()));
        placemark.appendChild(name);

        if(mark.getAccuracy()!=null) {
            Element accuracy = doc.createElement("accuracy");
            accuracy.appendChild(doc.createTextNode(mark.getAccuracy()));
            placemark.appendChild(accuracy);
        }
//issue is here
        Element desc = doc.createElement("description");
        desc.appendChild(doc.createTextNode(mark.getLatitude() + ", " + mark.getLongitude() + ", Acc: "+mark.getAccuracy()+"m\n"));
        placemark.appendChild(desc);

        Element point = doc.createElement("Point");
        placemark.appendChild(point);

        Element coords = doc.createElement("coordinates");
        coords.appendChild(doc.createTextNode(mark.getLongitude() + ", " + mark.getLatitude()));
        point.appendChild(coords);
    }

    public void addMarker(MapPoint mark,String pillarsInfo) {
        Element placemark = doc.createElement("Placemark");
        folder.appendChild(placemark);


        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(mark.getName()));
        placemark.appendChild(name);

        if(mark.getAccuracy()!=null) {
            Element accuracy = doc.createElement("accuracy");
            accuracy.appendChild(doc.createTextNode(mark.getAccuracy()));
            placemark.appendChild(accuracy);
        }



        Element desc = doc.createElement("description");
        desc.appendChild(doc.createTextNode(mark.getLatitude() + ", " + mark.getLongitude() + ", Acc: "+mark.getAccuracy()+"m\n"));
        placemark.appendChild(desc);

        Element point = doc.createElement("Point");
        placemark.appendChild(point);

        Element coords = doc.createElement("coordinates");
        coords.appendChild(doc.createTextNode(mark.getLongitude() + ", " + mark.getLatitude()));
        point.appendChild(coords);
    }

    /**
     * Add a path to this KML object.
     * @param path
     * @param pathName
     */
    public void addPath(List<MapPoint> path, String pathName,String distance,String area,String pillarsInfo) {
        Element placemark = doc.createElement("Placemark");
        folder.appendChild(placemark);

        if(pathName != null) {
            Element name = doc.createElement("name");
            name.appendChild(doc.createTextNode(pathName));
            placemark.appendChild(name);
        }
        if(distance!= null && area!= null) {
            Element description = doc.createElement("description");
            description.appendChild(doc.createTextNode("MineMapper#"+distance+"#"+area+"#"+pillarsInfo));
            placemark.appendChild(description);
        }

        Element style = doc.createElement("Style");
        Element linestyle = doc.createElement("LineStyle");
        Element polystyle = doc.createElement("PolyStyle");
        Element iconStyle = doc.createElement("IconStyle");
        Element icon = doc.createElement("Icon");
        Element href = doc.createElement("href");
        href.appendChild(doc.createTextNode("https://cdn-icons-png.flaticon.com/512/25/25613.png")); // Replace with your image URL
        icon.appendChild(href);
        iconStyle.appendChild(icon);
        Element fill = doc.createElement("fill");
        fill.appendChild(doc.createTextNode("0"));
        polystyle.appendChild(fill);
        Element color = doc.createElement("color");
        color.appendChild(doc.createTextNode("ffff00ff"));
        linestyle.appendChild(color);
        style.appendChild(polystyle);
        style.appendChild(linestyle);
        style.appendChild(iconStyle);
        placemark.appendChild(style);

        Element polygon = doc.createElement("Polygon");
        placemark.appendChild(polygon);
        Element outerBoundary = doc.createElement("outerBoundaryIs");
        polygon.appendChild(outerBoundary);
        Element lineString = doc.createElement("LinearRing");
        outerBoundary.appendChild(lineString);


        Element altitudeMode = doc.createElement("altitudeMode");
        altitudeMode.appendChild(doc.createTextNode("absolute"));
        lineString.appendChild(altitudeMode);

        Element coords = doc.createElement("coordinates");
        String points = "";
        ListIterator<MapPoint> itr = path.listIterator();
        while(itr.hasNext()) {
            MapPoint p = itr.next();
            points += p.getLongitude() + "," + p.getLatitude() + ","+ "\n";
        }
        coords.appendChild(doc.createTextNode(points));
        lineString.appendChild(coords);
    }

    public void addLine(List<MapPoint> path, String pathName,String distance,String area, String pillarsInfo) {
        Element placemark = doc.createElement("Placemark");
        folder.appendChild(placemark);

        if(pathName != null) {
            Element name = doc.createElement("name");
            name.appendChild(doc.createTextNode(pathName));
            placemark.appendChild(name);
        }
        if(distance!= null && area!= null) {
            Element description = doc.createElement("description");
            description.appendChild(doc.createTextNode("MineMapper#"+distance+"#"+area+"#"+pillarsInfo));
            placemark.appendChild(description);
        }

        Element style = doc.createElement("Style");
        Element linestyle = doc.createElement("LineStyle");
        Element color = doc.createElement("color");
        color.appendChild(doc.createTextNode("2986cc"));
        Element iconStyle = doc.createElement("IconStyle");
        Element icon = doc.createElement("Icon");
        Element href = doc.createElement("href");
        href.appendChild(doc.createTextNode("https://cdn-icons-png.flaticon.com/512/25/25613.png")); // Replace with your image URL
        icon.appendChild(href);
        iconStyle.appendChild(icon);
        linestyle.appendChild(color);
        style.appendChild(linestyle);
        style.appendChild(iconStyle);
        placemark.appendChild(style);


        Element lineString = doc.createElement("LineString");
        placemark.appendChild(lineString);

//        Element altitudeMode = doc.createElement("altitudeMode");
//        altitudeMode.appendChild(doc.createTextNode("absolute"));
//        lineString.appendChild(altitudeMode);

        Element coords = doc.createElement("coordinates");
        String points = "";
        ListIterator<MapPoint> itr = path.listIterator();
        while(itr.hasNext()) {
            MapPoint p = itr.next();
            points += p.getLongitude() + "," + p.getLatitude() + ","+ "\n";
        }
        if (!points.isEmpty()) {
            points = points.substring(0, points.length() - 1); // Remove the last newline character
        }

        coords.appendChild(doc.createTextNode(points));
        lineString.appendChild(coords);
    }


    /**
     * Write this KML object to a file.
     * @param file
     * @return
     */
    public boolean writeFile(File file) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource src = new DOMSource(doc);
            StreamResult out = new StreamResult(file);
            transformer.transform(src, out);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Read the KML file into this object.
     * @param file
     */
    public void readFile(File file) {
        // TODO read KML file
    }

}