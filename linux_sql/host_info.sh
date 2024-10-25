#!/bin/bash

# Script usage
# ./scripts/host_info.sh psql_host psql_port db_name psql_user psql_password

psql_host=$1
psql_port=$2
db_name=$3
psql_user=$4
psql_password=$5

# Check # of args
if [ "$#" -ne 5 ]; then
    echo "Illegal number of parameters"
    exit 1
fi

# Retrieve hardware specifications
hostname=$(hostname -f)
cpu_number=$(lscpu | awk '/^CPU\(s\):/ {print $2}')
cpu_architecture=$(lscpu | awk '/Architecture:/ {print $2}')
cpu_model=$(lscpu | awk -F': ' '/Model name:/ {print $2}' | xargs)  # Trim leading/trailing spaces
cpu_mhz=$(cat /proc/cpuinfo | grep "cpu MHz" | head -n 1 | awk '{print $4}')
l2_cache=$(lscpu | awk '/L2 cache:/ {print $3}' | sed 's/K//')
total_mem=$(free -m | awk '/Mem:/ {print $2}')

# Debug: print variables to see if anything is empty
echo "hostname=$hostname"
echo "cpu_number=$cpu_number"
echo "cpu_architecture=$cpu_architecture"
echo "cpu_model=$cpu_model"
echo "cpu_mhz=$cpu_mhz"
echo "l2_cache=$l2_cache"
echo "total_mem=$total_mem"

# Ensure no empty values
if [ -z "$cpu_model" ]; then
    cpu_model="Unknown CPU Model"
fi

# If cpu_mhz is empty, set it to 0
if [ -z "$cpu_mhz" ]; then
    cpu_mhz=0
fi

# Current time in `2019-11-26 14:40:19` UTC format
timestamp=$(date -u +"%Y-%m-%d %H:%M:%S")

# Construct INSERT statement with ON CONFLICT clause
insert_stmt="INSERT INTO host_info (hostname, cpu_number, cpu_architecture, cpu_model, cpu_mhz, l2_cache, total_mem, timestamp) VALUES ('$hostname', $cpu_number, '$cpu_architecture', '$cpu_model', $cpu_mhz, $l2_cache, $total_mem, '$timestamp') ON CONFLICT (hostname) DO UPDATE SET cpu_number = $cpu_number, cpu_architecture = '$cpu_architecture', cpu_model = '$cpu_model', cpu_mhz = $cpu_mhz, l2_cache = $l2_cache, total_mem = $total_mem, timestamp = '$timestamp';"

# Debug: print the INSERT statement
echo "insert_stmt=$insert_stmt"

# Set up env var for PSQL command
export PGPASSWORD=$psql_password

# Insert data into the database
psql -h $psql_host -p $psql_port -d $db_name -U $psql_user -c "$insert_stmt"
exit $?
