# Databricks notebook source
# MAGIC %md
# MAGIC #Optimizate the ETL pipeline for performance and efficiency

# COMMAND ----------

# Load Silver Table and Cache
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, sum, when, round
import time

spark = SparkSession.builder.getOrCreate()

silver_df = spark.table("silver_enriched_bookings").cache()
silver_df.count() 

print("Silver table cached succesfully")

# COMMAND ----------



# COMMAND ----------

