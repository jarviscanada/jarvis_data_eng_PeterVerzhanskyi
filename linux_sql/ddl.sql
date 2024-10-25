-- Create the "host_info" table if it does not exist
CREATE TABLE IF NOT EXISTS host_info (
    id SERIAL PRIMARY KEY,
    hostname VARCHAR NOT NULL UNIQUE,
    cpu_number INT2 NOT NULL,
    cpu_architecture VARCHAR NOT NULL,
    cpu_model VARCHAR NOT NULL,
    cpu_mhz FLOAT8 NOT NULL,
    l2_cache INT4 NOT NULL,
    total_mem INT4 NOT NULL,
    "timestamp" TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create the "host_usage" table if it does not exist
CREATE TABLE IF NOT EXISTS host_usage(
    "timestamp" TIMESTAMP NOT NULL,
    host_id INT REFERENCES host_info(id),
    memory_free INT4 NOT NULL,
    cpu_idle INT2 NOT NULL,
    cpu_kernel INT2 NOT NULL,
    disk_io INT4 NOT NULL,
    disk_available INT4 NOT NULL,
    PRIMARY KEY ("timestamp", host_id)
);