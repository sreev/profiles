/**
 * 
 */
package com.inpwd.profile;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/*
 * TODO:
 * https://twitter.com/mjimenez
 * http://ezinearticles.com/?expert=Mary_Jimenez
 * https://facebook.com/mary.jimenez
 * 
 */
/**
 * Main class to scrape pipl.com
 * 
 * https://pipl.com/search/?q=Mary+Jimenez
 * https://pipl.com/search/?t=MGYwYWQxNTg4ZDRhZWFlM2VlN2EwOGQwODAxMzZhYWRiYjc0ZDg2OGIzNDNmZjRj&in=8&q=Abdurrahman+Ko%C3%A7ak
 * 
 * @author Sree Vaddi
 *
 */
public class UserProfile {
	private static Logger LOG = LoggerFactory.getLogger("UserProfile");
	private static Type TYPE_MAP = new TypeToken<Map<String, String>>(){}.getType();
	
	private static String PIPL_SEARCH = "https://pipl.com/search/";
	private static String PIPL_URL_QUEST = "?q";
	
	private static String SPACE = " ";
	private static String PLUS = "+";
	private static String EQUALS = "=";
	
	private static String JSON_FULLNAME = "fullName";
//	private static String JSON_SITE = "site";
//	private static String JSON_STORYURL = "storyUrl";
	
	private String fileName;
	
	/**
	 * 
	 */
	public UserProfile(String fileName) {
		LOG.info("UserProfile()");
		this.fileName = fileName;
	}
	
	public void execute() throws Exception {
		LOG.debug("execute()");
		
		List<String> lines = Files.readAllLines(Paths.get(fileName),
				StandardCharsets.UTF_8);
		
		int lineNumber = 0;
		
		for (String line : lines) {
			try {
	//			LOG.info("line  :" + line);
				
				Gson gson = new Gson();
				Map<String, String> json = gson.fromJson(line, TYPE_MAP);
				
	//			LOG.info(json.get("fullName"));
	//			LOG.info(json.get("site"));
	//			LOG.info(json.get("storyUrl"));
				
	//			LOG.info("output:" + gson.toJsonTree(json, TYPE_MAP));
//				String storyURL = json.get(JSON_STORYURL);
//				String site = json.get(JSON_SITE);
				String fullName = json.get(JSON_FULLNAME);
				String[] names = fullName.split(SPACE);
				
				StringJoiner plus = new StringJoiner(PLUS);
				for (String string : names) {
					plus.add(string);
				}
				
				String searchURL = String.format("%s%s%s%s", PIPL_SEARCH, PIPL_URL_QUEST, EQUALS, plus.toString());
	//			LOG.info("searchURL:" + searchURL);
				
				Document doc = Jsoup.connect(searchURL).userAgent("Mozilla").timeout(3000).get();
				Elements results = doc.select("div.profile_result_image > a");
				Element firstResult = results.first();
				String personURL = String.format("%s%s", PIPL_SEARCH, firstResult.attr("href").toString());
	//			LOG.info(personURL);
				
				doc = Jsoup.connect(personURL).userAgent("Mozilla").timeout(3000).get();
				results = doc.select("div.row.group");
				
				for (Element result : results) {
					Elements fields = result.select("div.field_label");
					
					String temp = joinValues(fields, EQUALS);
					String key = temp.substring(0, (temp.length() - 1));
					
					Elements values = result.select("li");
					String value = joinValues(values, PLUS);
					
	//				LOG.info("key:" + key);
	//				LOG.info("value:" + value);
					
					json.put(key, value);
				}
				
				LOG.info(gson.toJson(json, TYPE_MAP));
				
				lineNumber++;
			}
			catch(Exception ex) {
				LOG.error(ex.getMessage(), ex);
			}
		}
		
		LOG.info("total lines:" + lineNumber);
		
		return;
	}
	
	private String joinValues(Elements values, String joinWith) {
		StringJoiner value = new StringJoiner(joinWith);
		for (Element element : values) {
			value.add(element.text());
		}
		
		return value.toString();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		LOG.info("Starting...");
		
		if (args.length != 1) {
			LOG.error("Invalid input arguments: "
					+ (args == null ? args : Arrays.asList(args)));
			
			LOG.info("Usage: java com.inpwd.profile.UserProfile fileName");
			
			System.exit(1);
		}
		
		UserProfile userProfile = new UserProfile(args[0]);
		userProfile.execute();
		
		LOG.info("Done!");
		
		return;
	}
}
