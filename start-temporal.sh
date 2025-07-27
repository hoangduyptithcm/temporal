#!/bin/bash

# Script để khởi động Temporal Learning Project
# Sử dụng: ./start-temporal.sh

echo "🚀 Starting Temporal Learning Project..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
print_status "Checking Docker status..."
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi
print_success "Docker is running"

# Check if Temporal Server is already running
print_status "Checking if Temporal Server is already running..."
if docker ps | grep -q "temporalio/auto-setup"; then
    print_success "Temporal Server is already running"
else
    print_status "Starting Temporal Server..."
    
    # Create temporal-docker directory if it doesn't exist
    if [ ! -d "temporal-docker" ]; then
        print_status "Cloning Temporal Docker Compose..."
        git clone https://github.com/temporalio/docker-compose.git temporal-docker
    fi
    
    # Start Temporal Server
    cd temporal-docker
    docker-compose up -d
    cd ..
    
    print_status "Waiting for Temporal Server to be ready..."
    sleep 10
    
    # Check if Temporal Server is running
    if docker ps | grep -q "temporalio/auto-setup"; then
        print_success "Temporal Server started successfully"
    else
        print_error "Failed to start Temporal Server"
        exit 1
    fi
fi

# Build the project
print_status "Building the project..."
if mvn clean install -q; then
    print_success "Project built successfully"
else
    print_error "Failed to build project"
    exit 1
fi

# Start the Spring Boot application
print_status "Starting Spring Boot application..."
print_status "This may take a few moments..."

# Run in background and capture PID
mvn spring-boot:run > application.log 2>&1 &
APP_PID=$!

# Wait for application to start
print_status "Waiting for application to start..."
sleep 15

# Check if application is running
if kill -0 $APP_PID 2>/dev/null; then
    # Test health endpoint
    if curl -s http://localhost:8081/api/workflows/health > /dev/null; then
        print_success "Application started successfully!"
        echo ""
        echo "🎉 Temporal Learning Project is now running!"
        echo ""
        echo "📋 Available endpoints:"
        echo "   • Health Check: http://localhost:8081/api/workflows/health"
        echo "   • Temporal Web UI: http://localhost:8080"
        echo ""
        echo "📖 API Examples:"
        echo "   • Async Workflow:"
        echo "     curl -X POST http://localhost:8081/api/workflows/greeting/async \\"
        echo "       -H \"Content-Type: application/json\" \\"
        echo "       -d '{\"name\": \"John Doe\"}'"
        echo ""
        echo "   • Sync Workflow:"
        echo "     curl -X POST http://localhost:8081/api/workflows/greeting/sync \\"
        echo "       -H \"Content-Type: application/json\" \\"
        echo "       -d '{\"name\": \"Jane Smith\"}'"
        echo ""
        echo "📝 Logs: tail -f application.log"
        echo "🛑 Stop: kill $APP_PID"
        echo ""
        echo "Application PID: $APP_PID"
        
        # Save PID to file for easy stopping
        echo $APP_PID > .app.pid
        
    else
        print_error "Application started but health check failed"
        kill $APP_PID 2>/dev/null
        exit 1
    fi
else
    print_error "Failed to start application"
    exit 1
fi
