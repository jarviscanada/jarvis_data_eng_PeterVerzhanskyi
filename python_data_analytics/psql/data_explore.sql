-- Show table schema 
\d+ retail;

-- Show first 10 rows
SELECT * FROM retail limit 10;

-- Check # of records
SELECT COUNT(*) AS NumberOfRecords FROM retail;

-- number of clients (e.g. unique client ID)
SELECT COUNT(DISTINCT customer_id) FROM retail;

--invoice date range
SELECT MIN(invoice_date) AS min_date, MAX(invoice_date) AS max_date FROM retail;

--number of SKU/merchants (e.g. unique stock code)
SELECT COUNT(DISTINCT stock_code) FROM retail;

--Calculate average invoice amount excluding invoices with a negative amount(e.g. 
-- canceled orders have negative amount)
SELECT AVG(total_amount)
FROM (
    SELECT invoice_no, SUM(unit_price * quantity) AS total_amount
    FROM retail
    GROUP BY invoice_no
    HAVING SUM(unit_price * quantity) > 0
) subquery;

-- Calculate total revenue (e.g. sum of unit_price * quantity)
SELECT SUM (unit_price * quantity) AS total_revenue FROM retail;

--Calculate total revenue by YYYYMM
SELECT EXTRACT(YEAR FROM invoice_date) * 100 + EXTRACT (MONTH FROM invoice_date) AS YYYYMM,
    SUM(unit_price * quantity) AS total_revenue
FROM retail
GROUP BY YYYYMM
ORDER BY YYYYMM;