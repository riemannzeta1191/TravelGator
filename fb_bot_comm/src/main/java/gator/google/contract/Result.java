package gator.google.contract;

import java.util.List;

import javax.annotation.Generated;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Result {

	@SerializedName("name")
	@Expose
	private String name;

	@SerializedName("opening_hours")
	@Expose
	private OpeningHours opening_hours;

	@SerializedName("place_id")
	@Expose
	private String place_id;

	@SerializedName("vicinity")
	@Expose
	private String vicinity;

	@SerializedName("icon")
	@Expose
	private String icon;

	@SerializedName("types")
	@Expose
	private String[] types;

	@SerializedName("url")
	@Expose
	private String url;

	@SerializedName("photos")
	@Expose
	private List<Photo> photos;

	@SerializedName("geometry")
	@Expose
	private Geometry geometry;

	@SerializedName("rating")
	@Expose
	private double rating;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public OpeningHours getOpening_hours() {
		return opening_hours;
	}

	public void setOpening_hours(OpeningHours opening_hours) {
		this.opening_hours = opening_hours;
	}

	public String getPlace_id() {
		return place_id;
	}

	public void setPlace_id(String place_id) {
		this.place_id = place_id;
	}

	public String getVicinity() {
		return vicinity;
	}

	public void setVicinity(String vicinity) {
		this.vicinity = vicinity;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String[] getTypes() {
		return types;
	}

	public void setTypes(String[] types) {
		this.types = types;
	}

	public String getIcon() {
		return icon;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public List<Photo> getPhotos() {
		return photos;
	}

	public void setPhotos(List<Photo> photos) {
		this.photos = photos;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	private String timeSpent = "1";

	public String getTimeSpent() {
		return timeSpent;
	}

	public void setTimeSpent(String timeSpent) {
		this.timeSpent = timeSpent;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
