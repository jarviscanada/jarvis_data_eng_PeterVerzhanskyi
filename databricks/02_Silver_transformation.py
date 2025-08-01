# Databricks notebook source
# MAGIC %md
# MAGIC # Silver Layer : Enriched Bookings Table
# MAGIC This notebook performs the Silver layer transformation as part of the Medallion Architecture ETL pipeline.
# MAGIC
# MAGIC We take raw Bronze tables (bookings, members, facilities) and:
# MAGIC
# MAGIC Join them together to create a single enriched dataset
# MAGIC Apply business rules and clean the data
# MAGIC Select only relevant fields for analytics
# MAGIC Save the result as a managed Delta table called silver_enriched_bookings

# COMMAND ----------

# MAGIC %md
# MAGIC ## 1. Load Bronze Tables
# MAGIC
# MAGIC We begin by loading the raw data that was ingested in the Bronze layer. These are managed Delta tables created from the original CSV files.

# COMMAND ----------

from pyspark.sql.functions import col, concat_ws, when

#----------
# Load Bronze Tables
#----------
# Load raw ingested tables from Bronze layer as managed Delta tables

bookings_df = spark.table("bronze_bookings")
facilities_df = spark.table("bronze_facilities")
members_df = spark.table("bronze_members")

# COMMAND ----------

# MAGIC %md
# MAGIC ## 2. Join & Enrich
# MAGIC
# MAGIC We perform a series of joins to combine the tables into one:
# MAGIC
# MAGIC Join bookings with members to get member details
# MAGIC Join with facilities to attach facility names and costs
# MAGIC This gives us a denormalized dataset that simplifies downstream analysis.

# COMMAND ----------

#---------
# Join & Enrich
#---------
# Join bookings with members and facilities to enrich each booking record

# Rename _ingest_timestamp columns before the join to avoid ambiguity
bookings_df = bookings_df.withColumnRenamed("_ingest_timestamp", "booking_ingest_ts")
facilities_df = facilities_df.withColumnRenamed("_ingest_timestamp", "facility_ingest_ts")
members_df = members_df.withColumnRenamed("_ingest_timestamp", "member_ingest_ts")

enriched_df = bookings_df \
  .join(members_df, on='memid', how='left') \
  .join(facilities_df, on='facid', how='left')

# COMMAND ----------

# MAGIC %md
# MAGIC ## 3. Apply Business Logic
# MAGIC
# MAGIC We apply the following transformations:
# MAGIC
# MAGIC Create a new column member_name
# MAGIC Filter out invalid bookings (slots <= 0)
# MAGIC Replace missing recommendedby with -1
# MAGIC Rename columns for clarity (e.g. name → facility_name)

# COMMAND ----------

#---------
# Busines Logic & Cleanup
#---------

# Create member full name for easier reporting
enriched_df = enriched_df.withColumn(
    "member_name",
    concat_ws(" ", col("firstname"), col("surname"))
)

# Renabe ambiguous column 'name' to 'facility_name'
enriched_df = enriched_df.withColumnRenamed("name", "facility_name")

# Filter out invalid bookings where slots <= 0
enriched_df = enriched_df.filter(col("slots") > 0)

# Replace nulls in 'recommendedby' with -1 for downstream clarity
enriched_df = enriched_df.withColumn(
    "recommendedby",
    when(col("recommendedby").isNull(), -1).otherwise(col("recommendedby"))
) 

# COMMAND ----------

# MAGIC %md
# MAGIC ## 4. Select Relevant Columns
# MAGIC
# MAGIC To optimize performance and clarity, we keep only the columns required for business reporting and KPIs.

# COMMAND ----------

#-----------
# Column Pruning
#-----------
# Select only the relevant columsn needed for analytics

enriched_df = enriched_df.select(
    "memid",
    "bookid","starttime","slots",
    "member_name","joindate", "recommendedby",
    "facility_name", "membercost", "guestcost", "booking_ingest_ts"
)

# ---------------------
# Rename timestamp for consistency across layers
# ---------------------
enriched_df = enriched_df.withColumnRenamed("booking_ingest_ts", "_ingest_timestamp")


# COMMAND ----------

# MAGIC %md
# MAGIC ## 5. Save as Silver Table
# MAGIC
# MAGIC Finally, we save the result as a managed Delta table called silver_enriched_bookings. This table will serve as the input for our Gold layer aggregations and dashboards.

# COMMAND ----------

#---------
# Save as Silver Table
#---------
# Write the enriched, cleaned, joined data as a managed Delta table for the Silver layer

enriched_df.write \
    .option("overwriteSchema", "true") \
    .format("delta") \
    .mode("overwrite") \
    .saveAsTable("silver_enriched_bookings")