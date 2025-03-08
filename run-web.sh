#!/bin/bash

# Environment variables are only needed when not using mock profile
# The mock profile uses internal mock implementations instead of real AI services

# Run the Spring AI Mobile Automation Framework with the mock profile for development
mvn spring-boot:run -Dspring-boot.run.profiles=mock 