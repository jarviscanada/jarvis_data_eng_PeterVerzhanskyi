package stockquote.dao;

import ca.jrvs.apps.stockquote.model.Quote;
import org.postgresql.util.PSQLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuoteDao implements CrudeRepository<Quote, String> {

    private final Connection connection;

    public QuoteDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Quote save(Quote quote) {
        String upsertQuery = "INSERT INTO quote (symbol, open, high, low, price, volume, latest_trading_day, previous_close, change, change_percent)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                " ON CONFLICT (symbol) DO UPDATE SET" +
                " open = EXCLUDED.open, high = EXCLUDED.high, low = EXCLUDED.low, price = EXCLUDED.price," +
                " volume = EXCLUDED.volume, latest_trading_day = EXCLUDED.latest_trading_day," +
                " previous_close = EXCLUDED.previous_close, change = EXCLUDED.change," +
                " change_percent = EXCLUDED.change_percent";

        try (PreparedStatement statement = connection.prepareStatement(upsertQuery)) {
            statement.setString(1, quote.getSymbol());
            statement.setBigDecimal(2, quote.getOpen());
            statement.setBigDecimal(3, quote.getHigh());
            statement.setBigDecimal(4, quote.getLow());
            statement.setBigDecimal(5, quote.getPrice());
            statement.setInt(6, quote.getVolume());
            statement.setDate(7, Date.valueOf(quote.getLatestTradingDay()));
            statement.setBigDecimal(8, quote.getPreviousClose());
            statement.setBigDecimal(9, quote.getChange());
            statement.setString(10, quote.getChangePercent());

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving quote", e);
        }

        return quote;
    }

    @Override
    public Optional<Quote> findById(String symbol) {
        String selectQuery = "SELECT * FROM quote WHERE symbol = ?";
        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            statement.setString(1, symbol);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapToQuote(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding quote by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Quote> findAll() {
        String selectQuery = "SELECT * FROM quote";
        List<Quote> quotes = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(selectQuery);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                quotes.add(mapToQuote(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all quotes", e);
        }
        return quotes;
    }

    @Override
    public void deleteById(String symbol) {
        String deleteQuery = "DELETE FROM quote WHERE symbol = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.setString(1, symbol);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting quote by ID", e);
        }
    }

    private Quote mapToQuote(ResultSet resultSet) throws SQLException {
        return new Quote(
                resultSet.getString("symbol"),
                resultSet.getBigDecimal("open"),
                resultSet.getBigDecimal("high"),
                resultSet.getBigDecimal("low"),
                resultSet.getBigDecimal("price"),
                resultSet.getInt("volume"),
                resultSet.getDate("latest_trading_day").toLocalDate(),
                resultSet.getBigDecimal("previous_close"),
                resultSet.getBigDecimal("change"),
                resultSet.getString("change_percent")
        );
    }
}
