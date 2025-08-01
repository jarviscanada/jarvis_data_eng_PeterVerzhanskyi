# Databricks notebook source
# MAGIC %md
# MAGIC ## Bronze Layer Ingestion
# MAGIC
# MAGIC This notebook ingests raw data files (CSV) into the Bronze layer of our medallion architecture. 
# MAGIC
# MAGIC Characteristics of Bronze tables:
# MAGIC - Raw and untransformed data
# MAGIC - Schema is enforced but no business logic applied
# MAGIC - Ingestion timestamp added
# MAGIC - Stored in Delta format
# MAGIC
# MAGIC These Bronze tables serve as the reliable source of truth for all downstream Silver and Gold transformations.
# MAGIC

# COMMAND ----------

from pyspark.sql.types import StructType, StructField, IntegerType, StringType, DecimalType, TimestampType
from pyspark.sql.functions import current_timestamp

# COMMAND ----------

# ---------------------
# Bookings Table
# Ingest raw CSV with defined schema and load into the Bronze layer as a managed Delta table
# ---------------------
bookings_schema = StructType([
    StructField("bookid", IntegerType(), True),
    StructField("facid", IntegerType(), True),
    StructField("memid", IntegerType(), True),
    StructField("starttime", TimestampType(), True),
    StructField("slots", IntegerType(), True)
])

bookings_df = (
    spark.read.format("csv")
    .option("header", "true")
    .schema(bookings_schema)
    .load("/Volumes/workspace/default/bookings/bookings.csv")
    .withColumn("_ingest_timestamp", current_timestamp())
)

bookings_df.write.format("delta").mode("overwrite").saveAsTable("bronze_bookings")
print("Bronze: bookings table written.")

# COMMAND ----------

# ---------------------
# Facilities Table
# Ingest raw CSV with defined schema and load into the Bronze layer as a managed Delta table
# ---------------------
facilities_schema = StructType([
    StructField("facid", IntegerType(), True),
    StructField("name", StringType(), True),
    StructField("membercost", DecimalType(10,2), True),
    StructField("guestcost", DecimalType(10,2), True),
    StructField("initialoutlay", DecimalType(10,2), True),
    StructField("monthlymaintenance", DecimalType(10,2), True)
])

facilities_df = (
    spark.read.format("csv")
    .option("header", "true")
    .schema(facilities_schema)
    .load("/Volumes/workspace/default/facilities/facilities.csv")
    .withColumn("_ingest_timestamp", current_timestamp())
)

facilities_df.write.format("delta").mode("overwrite").saveAsTable("bronze_facilities")
print("Bronze: facilities table written.")

# COMMAND ----------

# ---------------------
# Members Table
# Ingest raw CSV with defined schema and load into the Bronze layer as a managed Delta table
# ---------------------
members_schema = StructType([
    StructField("memid", IntegerType(), True),
    StructField("surname", StringType(), True),
    StructField("firstname", StringType(), True),
    StructField("address", StringType(), True),
    StructField("zipcode", StringType(), True),
    StructField("telephone", StringType(), True),
    StructField("recommendedby", IntegerType(), True),
    StructField("joindate", TimestampType(), True)
])

members_df = (
    spark.read.format("csv")
    .option("header", "true")
    .schema(members_schema)
    .load("/Volumes/workspace/default/members/members.csv")
    .withColumn("_ingest_timestamp", current_timestamp())
)

members_df.write.format("delta").mode("overwrite").saveAsTable("bronze_members")
print("Bronze: members table written.")