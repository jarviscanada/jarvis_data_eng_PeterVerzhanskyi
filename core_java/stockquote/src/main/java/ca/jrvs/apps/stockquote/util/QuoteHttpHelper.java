package ca.jrvs.apps.stockquote.util;

import ca.jrvs.apps.stockquote.model.Quote;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import java.io.IOException;

public class QuoteHttpHelper {

    private final String apiKey;
    private final OkHttpClient httpClient;
    private static final Logger logger = LoggerFactory.getLogger(QuoteHttpHelper.class);

    private static final String API_HOST = "alpha-vantage.p.rapidapi.com";
    private static final String BASE_URL = "https://" + API_HOST + "/query";

    public QuoteHttpHelper(String apiKey, OkHttpClient httpClient) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
    }

    public Quote fetchQuoteInfo(String symbol) throws IOException {
        logger.info("Fetching quote for symbol: {}", symbol);

        // Construct the request URL
        String url = String.format("%s?function=GLOBAL_QUOTE&symbol=%s&datatype=json", BASE_URL, symbol);

        // Create the HTTP request
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-RapidAPI-Key", apiKey)
                .addHeader("X-RapidAPI-Host", API_HOST)
                .build();

        // Execute the request and handle the response
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Failed request: HTTP code {}", response.code());
                throw new IOException("HTTP request failed with code " + response.code());
            }

            String responseBody = response.body().string();
            logger.debug("Received response: {}", responseBody);

            // Deserialize and return the Quote
            return parseResponse(responseBody);
        }
    }

    private Quote parseResponse(String jsonData) throws IOException {
        // Create and configure ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Enable support for LocalDate
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JsonNode rootNode = mapper.readTree(jsonData);

        // Extract the "Global Quote" node
        JsonNode globalQuoteNode = rootNode.path("Global Quote");
        if (globalQuoteNode.isMissingNode()) {
            logger.error("Missing 'Global Quote' node in API response");
            throw new IOException("Invalid API response: 'Global Quote' data is missing");
        }

        // Map the node to a Quote object
        return mapper.treeToValue(globalQuoteNode, Quote.class);
    }
}
