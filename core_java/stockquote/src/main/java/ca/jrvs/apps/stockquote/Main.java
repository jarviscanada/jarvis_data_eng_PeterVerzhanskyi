package ca.jrvs.apps.stockquote;

import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.model.Quote;
import ca.jrvs.apps.stockquote.service.QuoteService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/stock_quote", "postgres", ""
        );
        QuoteDao quoteDao = new QuoteDao(connection);
        QuoteService quoteService = new QuoteService(quoteDao);

        // Sample Quote
        Quote quote = new Quote("AAPL", new BigDecimal("150.00"), new BigDecimal("155.00"),
                new BigDecimal("148.00"), new BigDecimal("152.00"), 500000,
                java.time.LocalDate.now(), new BigDecimal("149.00"), new BigDecimal("3.00"), "2%");

        // Save Quote
        quoteService.saveQuote(quote);

        // Retrieve and print all quotes
        List<Quote> quotes = quoteService.getAllQuotes();
        quotes.forEach(System.out::println);


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }
}
