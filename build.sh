#!/bin/bash

# Install Java and Maven
echo "Installing Java and Maven..."

# Download and install Maven
MAVEN_VERSION=3.9.5
wget https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz
tar -xzf apache-maven-$MAVEN_VERSION-bin.tar.gz
export PATH=$PWD/apache-maven-$MAVEN_VERSION/bin:$PATH

# Verify installation
echo "Maven version:"
mvn -version

# Build the project
echo "Building Spring Boot application..."
mvn clean package -DskipTests

echo "Build complete!"
