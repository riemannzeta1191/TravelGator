package gator.fb.contract;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")

public class DefaultAction {

	@SerializedName("type")
	@Expose
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMessenger_extensions() {
		return messenger_extensions;
	}

	public void setMessenger_extensions(String messenger_extensions) {
		this.messenger_extensions = messenger_extensions;
	}

	public String getWebview_height_ratio() {
		return webview_height_ratio;
	}

	public void setWebview_height_ratio(String webview_height_ratio) {
		this.webview_height_ratio = webview_height_ratio;
	}

	@SerializedName("url")
	@Expose
	private String url;

	@SerializedName("messenger_extensions")
	@Expose
	private String messenger_extensions;

	@SerializedName("webview_height_ratio")
	@Expose
	private String webview_height_ratio;
}
