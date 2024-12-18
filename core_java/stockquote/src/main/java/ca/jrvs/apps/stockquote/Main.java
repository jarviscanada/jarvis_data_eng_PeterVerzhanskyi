package ca.jrvs.apps.stockquote;

import ca.jrvs.apps.stockquote.util.QuoteDao;
import ca.jrvs.apps.stockquote.util.QuoteHttpHelper;
import ca.jrvs.apps.stockquote.model.Quote;
import ca.jrvs.apps.stockquote.service.QuoteService;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.io.IOException;
import java.util.List;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        // Initialize database connection
        Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/stock_quote", "postgres", ""
        );

        // Initialize services and dependencies
        QuoteDao quoteDao = new QuoteDao(connection);
        QuoteHttpHelper quoteHttpHelper = new QuoteHttpHelper(
                "c905dd1071mshc0457adb8714bfdp1165bfjsn526029a94385",
                new OkHttpClient()
        );
        QuoteService quoteService = new QuoteService(quoteDao, quoteHttpHelper);

        // Fetch and save a quote
        try {
            Quote quote = quoteService.fetchAndSaveQuote("AAPL");
            logger.info("Saved Quote: {}", quote);
        } catch (IOException e) {
            logger.error("Failed to fetch and save quote for AAPL", e);
        }

        // Retrieve and print all quotes
        try {
            List<Quote> quotes = quoteService.getAllQuotes();
            quotes.forEach(quote -> logger.info("Retrieved Quote: {}", quote));
        } catch (Exception e) {
            logger.error("Failed to retrieve quotes", e);
        }

        // Ensure the connection is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                connection.close();
                logger.info("Database connection closed.");
            } catch (Exception e) {
                logger.error("Failed to close database connection", e);
            }
        }));
    }
}
