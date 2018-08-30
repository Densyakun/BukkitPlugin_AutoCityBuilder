package io.github.densyakun.bukkit.autocitybuilder;

import java.util.Map;

public class OSMNode {

	private long id;
	private float lat;
	private float lon;
	private Map<String, String> tags;

	public OSMNode(long id, float lat, float lon, Map<String, String> tags) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.tags = tags;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setLat(float lat) {
		this.lat = lat;
	}

	public float getLat() {
		return lat;
	}

	public void setLon(float lon) {
		this.lon = lon;
	}

	public float getLon() {
		return lon;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	@Override
	public String toString() {
		return "id: " + id + " lat: " + lat + " lon: " + lon + " tags: " + tags;
	}
}
