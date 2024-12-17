package ca.jrvs.apps.stockquote.service;

import ca.jrvs.apps.stockquote.dao.CrudeRepository;
import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.model.Quote;

import java.util.List;
import java.util.Optional;

public class QuoteService {

    private final CrudeRepository<Quote, String> quoteDao;

    public QuoteService(QuoteDao quoteDao) {
        this.quoteDao = quoteDao;
    }

    public Quote saveQuote(Quote quote) {
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
}
