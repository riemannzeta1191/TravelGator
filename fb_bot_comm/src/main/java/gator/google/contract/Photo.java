package gator.google.contract;

import javax.annotation.Generated;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")

public class Photo {


	@SerializedName("photo_reference")
	@Expose
	public String photoreference;

	public String getPhotoreference() {
		return photoreference;
	}

	public void setPhotoreference(String photoreference) {
		this.photoreference = photoreference;
	}
	

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
