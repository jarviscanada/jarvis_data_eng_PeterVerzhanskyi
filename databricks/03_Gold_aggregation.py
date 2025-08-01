# Databricks notebook source
# MAGIC %md
# MAGIC # Gold Layer: Monthly Facility Usage & Revenue
# MAGIC
# MAGIC This notebook performs the Gold layer aggregation as part of the Medallion Architecture ETL pipeline.
# MAGIC
# MAGIC We take enriched data from the Silver layer and compute business-critical metrics per facility per month. These outputs power BI dashboards used by stakeholders for revenue tracking, usage trends, and strategic planning.

# COMMAND ----------

# MAGIC %md
# MAGIC ## 1. Load Silver Table
# MAGIC
# MAGIC We begin by loading the cleaned and enriched booking data from the Silver layer (silver_enriched_bookings), which contains member and facility context.

# COMMAND ----------

from pyspark.sql.functions import col, sum, month, year, when, round

#------------
# Load Silver Table
#------------
# Load the enriched booking records from Silver layer
silver_df = spark.table("silver_enriched_bookings")

# COMMAND ----------

# MAGIC %md
# MAGIC ## 2. Add Member Type & Revenue Columns
# MAGIC
# MAGIC We enrich the dataset further by:
# MAGIC
# MAGIC  - Classifying bookings as member or guest  based on recommendedby
# MAGIC  - Calculating estimated revenue:
# MAGIC  - Guests pay guestcost × slots
# MAGIC  - Members pay membercost × slots
# MAGIC  
# MAGIC This logic reflects real-world pricing models in many membership-based businesses.

# COMMAND ----------

#-----------
# Add Member Type & Revenue Columns
#-----------
# Add a column to identify guest vs member

silver_df = silver_df.withColumn(
    "is_guest",
    when(col("memid") == 0, True).otherwise(False)
)

# Calculate revenue per row
silver_df = silver_df.withColumn(
    "revenue",
    when(col("is_guest"), col("guestcost") * col("slots"))
    .otherwise(col("membercost") * col("slots"))
)

# COMMAND ----------

# MAGIC %md
# MAGIC ## 3. Add Year and Month Columns
# MAGIC
# MAGIC To support time-based aggregations and reporting, we extract year and month from the starttime timestamp column.

# COMMAND ----------

#-----------
# Add Year and Month for Aggregations
#-----------
silver_df = silver_df.withColumn("year",year(col("starttime")))
silver_df = silver_df.withColumn("month", month(col("starttime")))

# COMMAND ----------

# MAGIC %md
# MAGIC ## 4. Group and Aggregate Metrics
# MAGIC
# MAGIC We compute the following KPIs per facility and month:
# MAGIC
# MAGIC  - total_slots: total bookings
# MAGIC  - guest_slots: slots booked by guests
# MAGIC  - member_slots: slots booked by members
# MAGIC  - total_revenue: estimated total monthly revenue per facility
# MAGIC  
# MAGIC These metrics provide high-level insights for business leaders.

# COMMAND ----------

# Group by and Aggregate

gold_df = silver_df.groupBy("facility_name", "year", "month").agg(
    sum("slots").alias("total_slots"),
    sum(when(col("is_guest"), col("slots"))).alias("guest_slots"),
    sum(when(~col("is_guest"), col("slots"))).alias("member_slots"),
    round(sum("revenue"), 2).alias("total_revenue")
)


# COMMAND ----------

# MAGIC %md
# MAGIC ## 5. Save as Gold Table
# MAGIC
# MAGIC Finally, we store the results as a managed Delta table:
# MAGIC gold_facility_monthly_metrics
# MAGIC
# MAGIC This table is optimized for reporting tools and will serve as the source for dashboards and business reporting.

# COMMAND ----------

#-------------------
# Save as Gold Table
#-------------------
# Save the final Gold-level business metrics for use in BI dashboards, partitioned by facility

gold_df.write \
    .format("delta") \
    .mode("overwrite") \
    .partitionBy("facility_name") \
    .saveAsTable("gold_facility_monthly_metrics")

print("Gold table 'gold_facility_monthly_metrics' created successfully.")
