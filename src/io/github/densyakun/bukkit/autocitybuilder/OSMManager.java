package io.github.densyakun.bukkit.autocitybuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OSMManager {

	public static final double EARTH_MAJOR_AXIS = 6378137;
	public static final double GRS80_OBLATENESS = 1 / 298.257222101;
	public static final double EARTH_MINOR_AXIS = EARTH_MAJOR_AXIS - EARTH_MAJOR_AXIS * GRS80_OBLATENESS;

	public static float MAX_BBOX_SIZE = 1.0f;
	public static int MIN_Y = 64;

	private static double center_x = 0;
	private static double center_z = 0;
	private static float center_lat = -180;
	private static float center_lon = 0;

	private static List<OSMNode> nodes;

	public static void setCenterPoint(double x, double z, float lat, float lon) {
		center_x = x;
		center_z = z;
		center_lat = lat;
		center_lon = lon;
	}

	public static void clearCenterPoint() {
		center_x = 0;
		center_z = 0;
		center_lat = -180;
		center_lon = 0;
	}

	public static double getCenterPointX() {
		return center_x;
	}

	public static double getCenterPointZ() {
		return center_z;
	}

	public static float getCenterPointLat() {
		return center_lat;
	}

	public static float getCenterPointLon() {
		return center_lon;
	}

	public static boolean CenterPointIsEmpty() {
		return center_lat == -180;
	}

	public static void query(String ql, DefaultHandler dh)
			throws IOException, SAXException, ParserConfigurationException {
		HttpURLConnection http = null;
		try {
			http = (HttpURLConnection) new URL("http://overpass-api.de/api/interpreter?data=" + ql)
					.openConnection();
			http.setRequestMethod("GET");
			http.connect();

			InputStream in = http.getInputStream();
			SAXParserFactory.newInstance().newSAXParser().parse(in, dh);
			in.close();
		} catch (IOException | SAXException | ParserConfigurationException e) {
			throw e;
		} finally {
			if (http != null)
				http.disconnect();
		}
	}

	public static List<OSMNode> query_nodes_out(float s, float w, float n, float e)
			throws IOException, SAXException, ParserConfigurationException {
		query("node(" + s + "," + w + "," + n + "," + e + ");out;", new DefaultHandler() {
			List<OSMNode> nodes = new ArrayList<OSMNode>();
			long id;
			float lat;
			float lon;
			Map<String, String> tags = new HashMap<String, String>();

			public void startElement(String uri, String localName, String qName, Attributes attributes) {
				if (qName.equalsIgnoreCase("node")) {
					id = 0;
					lat = -180;
					lon = 0;
					tags.clear();
					for (int a = 0; a < attributes.getLength(); a++) {
						String b = attributes.getQName(a);
						if (b.equalsIgnoreCase("id"))
							id = Long.valueOf(attributes.getValue(a));
						else if (b.equalsIgnoreCase("lat"))
							lat = Float.valueOf(attributes.getValue(a));
						else if (b.equalsIgnoreCase("lon"))
							lon = Float.valueOf(attributes.getValue(a));
						else if (!(b.equalsIgnoreCase("version") || b.equalsIgnoreCase("changeset")
								|| b.equalsIgnoreCase("user") || b.equalsIgnoreCase("uid")
								|| b.equalsIgnoreCase("visible") || b.equalsIgnoreCase("timestamp")))
							System.out.println("node " + attributes.getQName(a) + ": " + attributes.getValue(a));
					}
				} else if (qName.equalsIgnoreCase("tag")) {
					String k = null;
					String v = null;
					for (int a = 0; a < attributes.getLength(); a++) {
						String b = attributes.getQName(a);
						if (b.equalsIgnoreCase("k"))
							k = attributes.getValue(a);
						else if (b.equalsIgnoreCase("v"))
							v = attributes.getValue(a);
						if (k != null && v != null)
							break;
					}
					tags.put(k, v);
				} else if (!(qName.equalsIgnoreCase("osm") || qName.equalsIgnoreCase("note")
						|| qName.equalsIgnoreCase("meta"))) {
					System.out.println("qName: " + qName);
					for (int a = 0; a < attributes.getLength(); a++)
						System.out.println(attributes.getQName(a) + ": " + attributes.getValue(a));
				}
			}

			/*public void characters(char[] ch, int offset, int length) {
				String a = new String(ch, offset, length).trim();
				if (!a.isEmpty())
					System.out.println(a);
			}*/

			public void endElement(String uri, String localName, String qName) {
				if (qName.equalsIgnoreCase("node"))
					nodes.add(new OSMNode(id, lat, lon, tags));
				else if (qName.equalsIgnoreCase("osm"))
					OSMManager.nodes = nodes;
			}
		});

		return nodes;
	}

	public static void query_ways_out(float s, float w, float n, float e)
			throws IOException, SAXException, ParserConfigurationException {
		query("way(" + s + "," + w + "," + n + "," + e + ");out;", new DefaultHandler() {
			List<Long> nds = new ArrayList<Long>();
			Map<String, String> tags = new HashMap<String, String>();

			public void startElement(String uri, String localName, String qName, Attributes attributes) {
				if (qName.equalsIgnoreCase("way")) {
					nds.clear();
					tags.clear();
					for (int a = 0; a < attributes.getLength(); a++) {
						String b = attributes.getQName(a);
						if (!(b.equalsIgnoreCase("id") || b.equalsIgnoreCase("visible")
								|| b.equalsIgnoreCase("timestamp") || b.equalsIgnoreCase("version")
								|| b.equalsIgnoreCase("changeset") || b.equalsIgnoreCase("user")
								|| b.equalsIgnoreCase("uid")))
							System.out.println("way " + attributes.getQName(a) + ": " + attributes.getValue(a));
					}
				} else if (qName.equalsIgnoreCase("nd")) {
					long ref = 0;
					for (int a = 0; a < attributes.getLength(); a++) {
						String b = attributes.getQName(a);
						if (b.equalsIgnoreCase("ref"))
							ref = Long.valueOf(attributes.getValue(a));
						else
							System.out.println("nd " + attributes.getQName(a) + ": " + attributes.getValue(a));
					}
					nds.add(ref);
				} else if (qName.equalsIgnoreCase("tag")) {
					String k = null;
					String v = null;
					for (int a = 0; a < attributes.getLength(); a++) {
						String b = attributes.getQName(a);
						if (b.equalsIgnoreCase("k"))
							k = attributes.getValue(a);
						else if (b.equalsIgnoreCase("v"))
							v = attributes.getValue(a);
						if (k != null && v != null)
							break;
					}
					tags.put(k, v);
				} else if (!(qName.equalsIgnoreCase("osm") || qName.equalsIgnoreCase("note")
						|| qName.equalsIgnoreCase("meta"))) {
					System.out.println("qName: " + qName);
					for (int a = 0; a < attributes.getLength(); a++)
						System.out.println(attributes.getQName(a) + ": " + attributes.getValue(a));
				}
			}

			/*public void characters(char[] ch, int offset, int length) {
				String a = new String(ch, offset, length).trim();
				if (!a.isEmpty())
					System.out.println(a);
			}*/

			public void endElement(String uri, String localName, String qName) {
				if (qName.equalsIgnoreCase("way"))
					System.out.println("nds: " + nds + " tags: " + tags);
			}
		});
	}

	/*public static double getEllipseCircumference(double major, double minor) {
		double k = 0;
		double m = 0.001;
		double o = minor / major;
		for (int n = 1; n <= 1 / m; n++)
			k += Math.sqrt((m * m) + (o * o * ((Math.sqrt(1 - m * (n - 1) * m * (n - 1)) - Math.sqrt(1 - m * n * m * n))
					* (Math.sqrt(1 - m * (n - 1) * m * (n - 1)) - Math.sqrt(1 - m * n * m * n)))));
		return k * major * 4;
	}*/

	public static double getZ(float lat) {
		return center_z + (lat - center_lat) * 2 * Math.PI * EARTH_MAJOR_AXIS; //TODO
	}

	public static double getX(float lat, float lon) {
		return center_x + (lon - center_lon) * 2 * Math.PI * EARTH_MAJOR_AXIS; //TODO
	}

	public static void buildNode(World world, OSMNode node) {
		int x = (int) Math.round(getX(node.getLat(), node.getLon()));
		int z = (int) Math.round(getZ(node.getLat()));
		for (int y = MIN_Y; y <= 253; y++) {
			Block b = world.getBlockAt(x, y, z);
			Block bs = world.getBlockAt(x, y + 1, z);
			if (b.isEmpty() && bs.isEmpty()) {
				b.setType(Material.WOOL);
				bs.setType(Material.SIGN);
				if (bs instanceof Sign) {
					((Sign) bs).setLine(0, "[OSM] node");
					((Sign) bs).setLine(1, node.toString());
				}
				break;
			}
		}
	}

	public static void main(String[] args) {
		/*try {
			List<OSMNode> nodes = query_nodes_out(50.745f, 7.17f, 50.75f, 7.18f);
			for (int a = 0; a < nodes.size(); a++)
				System.out.println(nodes.get(a));
		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}*/
		//System.out.println(getEllipseCircumference(0.5, 0.5));
	}
}
