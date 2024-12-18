package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.dao.CrudeRepository;
import ca.jrvs.apps.stockquote.util.QuoteDao;
import ca.jrvs.apps.stockquote.util.QuoteHttpHelper;
import ca.jrvs.apps.stockquote.model.Quote;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class QuoteService {

    private final CrudeRepository<Quote, String> quoteDao;
    private final QuoteHttpHelper quoteHttpHelper;

    public QuoteService(QuoteDao quoteDao, QuoteHttpHelper quoteHttpHelper) {
        this.quoteDao = quoteDao;
        this.quoteHttpHelper = quoteHttpHelper;
    }

    public Quote saveQuote(Quote quote) {
        if (quote == null) {
            throw new IllegalArgumentException("Quote cannot be null");
        }
        return quoteDao.save(quote);
    }

    public Optional<Quote> getQuote(String symbol) {
        return quoteDao.findById(symbol);
    }

    public List<Quote> getAllQuotes() {
        return quoteDao.findAll();
    }

    public void deleteQuote(String symbol) {
        quoteDao.deleteById(symbol);
    }

    public Quote fetchAndSaveQuote(String symbol) throws IOException {
        if (symbol == null || symbol.isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }

        Quote quote = quoteHttpHelper.fetchQuoteInfo(symbol); // Fetch from API
        if (quote == null) {
            throw new IOException("Failed to fetch quote for symbol: " + symbol);
        }

        // Save and return the quote
        return saveQuote(quote);
    }
}
