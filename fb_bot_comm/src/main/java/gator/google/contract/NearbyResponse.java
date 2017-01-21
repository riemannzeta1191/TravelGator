package gator.google.contract;

import java.util.List;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import gator.fb.contract.Attachment;

@Generated("org.jsonschema2pojo")
public class NearbyResponse {

	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	public String getNext_page_token() {
		return next_page_token;
	}

	public void setNext_page_token(String next_page_token) {
		this.next_page_token = next_page_token;
	}

	@SerializedName("results")
	@Expose
	private List<Result> results;
	
	@SerializedName("next_page_token")
	@Expose
	private String next_page_token;
}
