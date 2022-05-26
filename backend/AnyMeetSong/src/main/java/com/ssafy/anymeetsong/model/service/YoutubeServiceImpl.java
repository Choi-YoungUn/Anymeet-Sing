package com.ssafy.anymeetsong.model.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.ssafy.anymeetsong.model.SongInfoDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource("classpath:youtube.properties")
public class YoutubeServiceImpl implements YoutubeService {

	@Value("${youtube.apiUrl}")
	private String apiUrl;
	@Value("${youtube.apiKey}")
	private String apiKey;
	@Value("${youtube.channelId}")
	private String channelId;
	@Value("${youtube.fields}")
	private String fields;
	@Value("${youtube.numberOfReturn}")
	private int numOfReturns;
	@Value("${youtube.type}")
	private String type;
	@Value("${youtube.part}")
	private String part;

	/** Global instance of the HTTP transport. */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	@Override
	public List<SongInfoDto> searchByTitleFromYoutube(String keyword) throws IOException {
		List<SongInfoDto> list = null;
		try {
			/*
			 * The YouTube object is used to make all API requests. The last argument is
			 * required, but because we don't need anything initialized when the HttpRequest
			 * is initialized, we override the interface and provide a no-op function.
			 */
			YouTube youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
				public void initialize(HttpRequest request) throws IOException {
				}
			}).setApplicationName("youtube-cmdline-search-sample").build();

			YouTube.Search.List search = youtube.search().list("id,snippet");
			
			// Youtube API Key setting
			search.setKey(apiKey);
			
			// Request Param 설정
			search.setQ(keyword);
			search.setType("video");
			search.setType(type);
			search.setPart(part);
			search.setFields(fields);
			search.setChannelId(channelId);
			search.setMaxResults((long) numOfReturns);
			SearchListResponse searchResponse = search.execute();

			List<SearchResult> searchResultList = searchResponse.getItems();

			if (searchResultList != null) {
				list = getListFromResults(searchResultList.iterator(), keyword);
			}
		} catch (GoogleJsonResponseException e) {
			System.err.println(
					"There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
		} catch (IOException e) {
			System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return list;
	}

	private List<SongInfoDto> getListFromResults(Iterator<SearchResult> iteratorSearchResults, String query) {
		List<SongInfoDto> list = new ArrayList<>();

		System.out.println("\n=============================================================");
		System.out.println("   First " + numOfReturns + " videos for search on \"" + query + "\".");
		System.out.println("=============================================================\n");

		if (!iteratorSearchResults.hasNext()) {
			System.out.println(" There aren't any results for your query.");
		}

		while (iteratorSearchResults.hasNext()) {

			SearchResult singleVideo = iteratorSearchResults.next();
			ResourceId rId = singleVideo.getId();

			// Double checks the kind is video.
			if (rId.getKind().equals("youtube#video")) {
				Thumbnail thumbnail = (Thumbnail) singleVideo.getSnippet().getThumbnails().get("default");

				SongInfoDto youtube = new SongInfoDto();
				youtube.setVideoId(rId.getVideoId());
				youtube.setTitle(singleVideo.getSnippet().getTitle());
				youtube.setThumbnail(thumbnail.getUrl());
				list.add(youtube);

				// 받은거 출력
				System.out.println(" Video Id" + rId.getVideoId());
				System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
				System.out.println(" Thumbnail: " + thumbnail.getUrl());
				System.out.println("\n-------------------------------------------------------------\n");
			}
		}
		return list;
	}

}
