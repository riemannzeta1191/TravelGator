package gator.google.places;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class Test {

	static String big_url = "https://maps.googleapis.com/maps/api/place/photo?key=AIzaSyDDahyjeXnoCo7LSw3gsToRui7-9UXCbjI&photoreference=CoQBdwAAAKroQP7O3oXwjt2EdHnDDsT2BiBLLs-uf2qJdPfjl8yIbw-KEJgq9954y1X4t-kgSKVTbrl4UcD7IuT4sm57IiLv0IFHqquiQr_9PeMb_1FmZdZtz5DmmbqAyE94TUjqx7KVOmoOsc4KkeCJME8WloNp6klcFRpHes38ULH79UcMEhBKtay9SQ36zHtl2m3vdvffGhSsXZwkeqQ7kOHFkysqsu3oJ3Taxw&maxwidth=400";
	static String small_url = "https://lh6.googleusercontent.com/-SI4Cnjr8WEc/V-P3dbON7sI/AAAAAAAAtDQ/0Wph7k0tdNwlPEkFinEu_FRMfhn8shlggCJkC/s1600-w400/";

	public static void main(String[] args) throws Exception {
		
		URLConnection conn  = (new URL(big_url)).openConnection();
		conn.setAllowUserInteraction(true);
		conn.connect();
		System.out.println(conn.getURL());
		conn.getInputStream();
		conn.getURL();
		System.out.println(conn.getURL());
	}
}
