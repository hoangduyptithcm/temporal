#!/bin/bash

# Script để dừng Temporal Learning Project
# Sử dụng: ./stop-temporal.sh

echo "🛑 Stopping Temporal Learning Project..."

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

# Stop Spring Boot application
if [ -f ".app.pid" ]; then
    APP_PID=$(cat .app.pid)
    print_status "Stopping Spring Boot application (PID: $APP_PID)..."
    
    if kill -0 $APP_PID 2>/dev/null; then
        kill $APP_PID
        sleep 3
        
        # Force kill if still running
        if kill -0 $APP_PID 2>/dev/null; then
            print_warning "Force killing application..."
            kill -9 $APP_PID 2>/dev/null
        fi
        
        print_success "Spring Boot application stopped"
    else
        print_warning "Application was not running"
    fi
    
    rm -f .app.pid
else
    print_warning "No PID file found. Trying to find and stop Java processes..."
    
    # Find and kill Java processes related to our application
    JAVA_PIDS=$(ps aux | grep "temporal-learning" | grep -v grep | awk '{print $2}')
    
    if [ ! -z "$JAVA_PIDS" ]; then
        for pid in $JAVA_PIDS; do
            print_status "Stopping Java process: $pid"
            kill $pid 2>/dev/null
        done
        sleep 3
        print_success "Java processes stopped"
    else
        print_warning "No running Java processes found"
    fi
fi

# Ask user if they want to stop Temporal Server
echo ""
read -p "Do you want to stop Temporal Server as well? (y/N): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_status "Stopping Temporal Server..."
    
    if [ -d "temporal-docker" ]; then
        cd temporal-docker
        docker-compose down
        cd ..
        print_success "Temporal Server stopped"
    else
        print_warning "temporal-docker directory not found"
        print_status "Stopping Temporal containers manually..."
        
        # Stop Temporal containers
        TEMPORAL_CONTAINERS=$(docker ps | grep "temporalio" | awk '{print $1}')
        
        if [ ! -z "$TEMPORAL_CONTAINERS" ]; then
            for container in $TEMPORAL_CONTAINERS; do
                print_status "Stopping container: $container"
                docker stop $container
            done
            print_success "Temporal containers stopped"
        else
            print_warning "No Temporal containers found running"
        fi
    fi
else
    print_status "Keeping Temporal Server running"
fi

# Clean up log files
if [ -f "application.log" ]; then
    read -p "Do you want to delete application logs? (y/N): " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        rm -f application.log
        print_success "Application logs deleted"
    fi
fi

echo ""
print_success "Temporal Learning Project stopped successfully!"
echo ""
echo "📋 To start again:"
echo "   ./start-temporal.sh"
echo ""
echo "📖 To view documentation:"
echo "   cat README.md"
