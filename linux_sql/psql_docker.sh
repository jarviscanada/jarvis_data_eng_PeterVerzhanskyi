#!/bin/sh

# Capture CLI arguments
cmd=$1
db_username=$2
db_password=$3
db_name=$4  # Added for the database name

# Function to check if Docker is running
start_docker() {
  if ! systemctl is-active --quiet docker; then
    sudo systemctl start docker
  fi
}

# Start Docker if not running
start_docker

# Check if container already exists
docker container inspect jrvs-psql > /dev/null 2>&1
container_status=$?

# Use switch case to handle create|stop|start options
case $cmd in
  create)
    # Check if container is already created
    if [ $container_status -eq 0 ]; then
      echo 'Error: Container already exists'
      exit 1
    fi

    # Check if username, password, and db_name are provided
    if [ $# -ne 4 ]; then
      echo 'Error: Create requires username, password, and database name'
      exit 1
    fi

    # Create Docker volume and PostgreSQL container
    docker volume create jrvs-psql-vol
    docker run --name jrvs-psql -e POSTGRES_USER="$db_username" -e POSTGRES_PASSWORD="$db_password" -d -v jrvs-psql-vol:/var/lib/postgresql/data -p 5432:5432 postgres
    # Wait a moment for the container to be fully initialized
    sleep 5
    # Create the specified database inside the container
    docker exec -it jrvs-psql psql -U "$db_username" -c "CREATE DATABASE $db_name;"
    exit $?
    ;;

  start)
    # Check if the container is created; exit if not
    if [ $container_status -ne 0 ]; then
      echo 'Error: Container does not exist'
      exit 1
    fi

    # Start the container
    docker container start jrvs-psql
    exit $?
    ;;

  stop)
    # Check if the container is created, exit if not
    if [ $container_status -ne 0 ]; then
      echo 'Error: Container does not exist'
      exit 1
    fi

    # Stop the container
    docker container stop jrvs-psql
    exit $?
    ;;

  *)
    echo 'Error: Invalid command. Use create|start|stop'
    exit 1
    ;;
esac