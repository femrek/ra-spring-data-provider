#!/bin/bash

# Exit on error
set -e

echo "🚀 Starting Integration Tests..."

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to cleanup on exit
cleanup() {
    echo -e "\n${YELLOW}Cleaning up...${NC}"
    if [ ! -z "$SPRING_PID" ]; then
        echo "Stopping Spring Boot application (PID: $SPRING_PID)"
        kill $SPRING_PID 2>/dev/null || true
    fi
    if [ ! -z "$REACT_PID" ]; then
        echo "Stopping React Admin application (PID: $REACT_PID)"
        kill $REACT_PID 2>/dev/null || true
    fi
}

trap cleanup EXIT

# Step 1: Install npm dependencies if needed
echo -e "${YELLOW}📦 Installing npm dependencies...${NC}"
cd ra-spring-data-provider
if [ ! -d "node_modules" ]; then
    npm install
else
    echo "Dependencies already installed, skipping..."
fi

# Step 2: Install Playwright browsers if needed
echo -e "${YELLOW}🌐 Installing Playwright browsers...${NC}"
npx playwright install chromium

# Step 3: Start Spring Boot application
echo -e "${YELLOW}🍃 Starting Spring Boot application on port 8081...${NC}"
cd ../ra-spring-json-server
mvn clean test-compile
mvn exec:java -Ptest-run > spring-boot.log 2>&1 &
SPRING_PID=$!

# Wait for Spring Boot to start
echo "Waiting for Spring Boot to start..."
for i in {1..30}; do
    if curl -s http://localhost:8081/api/users > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Spring Boot is ready!${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}✗ Spring Boot failed to start within 30 seconds${NC}"
        cat spring-boot.log
        exit 1
    fi
    echo -n "."
    sleep 1
done

# Step 4: Run Playwright tests
echo -e "${YELLOW}🎭 Running Playwright tests...${NC}"
cd ../ra-spring-data-provider
npm run test

# Step 5: Check test results
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
else
    echo -e "${RED}✗ Some tests failed${NC}"
    exit 1
fi
