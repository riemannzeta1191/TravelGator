package gator.google.contract;

import java.util.List;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Result {

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

}
