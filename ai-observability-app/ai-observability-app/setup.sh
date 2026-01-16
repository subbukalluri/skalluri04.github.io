#!/bin/bash

# AI Observability App Setup Script
# This script sets up and starts the entire observability stack

set -e  # Exit on error

echo "🚀 AI Observability App - Setup Script"
echo "========================================"
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Check prerequisites
echo "Checking prerequisites..."

if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi
print_status "Docker is installed"

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi
print_status "Docker Compose is installed"

if ! command -v node &> /dev/null; then
    print_warning "Node.js is not installed. Frontend won't start automatically."
    NODE_INSTALLED=false
else
    print_status "Node.js is installed"
    NODE_INSTALLED=true
fi

# Check for API key
echo ""
echo "Checking for Anthropic API key..."
if [ -f "docker/.env" ]; then
    if grep -q "ANTHROPIC_API_KEY=your-api-key-here" docker/.env || grep -q "ANTHROPIC_API_KEY=$" docker/.env; then
        print_warning "API key not configured in docker/.env"
        echo "Please add your Anthropic API key to docker/.env"
        echo "Example: ANTHROPIC_API_KEY=sk-ant-..."
        read -p "Enter your Anthropic API key (or press Enter to continue): " api_key
        if [ ! -z "$api_key" ]; then
            echo "ANTHROPIC_API_KEY=$api_key" > docker/.env
            print_status "API key saved"
        fi
    else
        print_status "API key found in docker/.env"
    fi
else
    print_warning "docker/.env not found"
    read -p "Enter your Anthropic API key: " api_key
    if [ ! -z "$api_key" ]; then
        echo "ANTHROPIC_API_KEY=$api_key" > docker/.env
        print_status "API key saved"
    else
        echo "ANTHROPIC_API_KEY=your-api-key-here" > docker/.env
        print_warning "Created docker/.env with placeholder - please update with real API key"
    fi
fi

# Start observability stack
echo ""
echo "Starting observability stack..."
cd docker

# Pull images first
echo "Pulling Docker images (this may take a few minutes)..."
docker-compose pull

# Start services
echo "Starting services..."
docker-compose up -d

# Wait for services to be ready
echo ""
echo "Waiting for services to start..."
sleep 10

# Check service health
echo ""
echo "Checking service health..."

# Check Jaeger
if curl -s http://localhost:16686 > /dev/null; then
    print_status "Jaeger is running on http://localhost:16686"
else
    print_error "Jaeger failed to start"
fi

# Check Prometheus
if curl -s http://localhost:9090/-/healthy > /dev/null; then
    print_status "Prometheus is running on http://localhost:9090"
else
    print_error "Prometheus failed to start"
fi

# Check Grafana
if curl -s http://localhost:3001/api/health > /dev/null; then
    print_status "Grafana is running on http://localhost:3001"
else
    print_error "Grafana failed to start"
fi

# Wait for backend to start
echo ""
echo "Waiting for backend to start (this may take 30-60 seconds)..."
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if curl -s http://localhost:8080/actuator/health > /dev/null; then
        print_status "Backend is running on http://localhost:8080"
        break
    fi
    attempt=$((attempt + 1))
    echo -n "."
    sleep 2
done

if [ $attempt -eq $max_attempts ]; then
    print_error "Backend failed to start. Check logs with: docker-compose logs backend"
fi

cd ..

# Start frontend if Node.js is installed
if [ "$NODE_INSTALLED" = true ]; then
    echo ""
    echo "Installing frontend dependencies..."
    cd frontend
    npm install
    
    echo ""
    print_status "Setup complete!"
    echo ""
    echo "Starting frontend server..."
    echo "Frontend will be available at http://localhost:3000"
    echo ""
    npm start
else
    echo ""
    print_status "Backend and observability stack are running!"
    echo ""
    echo "To start the frontend manually:"
    echo "  cd frontend"
    echo "  npm install"
    echo "  npm start"
fi

echo ""
echo "========================================="
echo "Access Points:"
echo "  Application:  http://localhost:3000"
echo "  Jaeger UI:    http://localhost:16686"
echo "  Grafana:      http://localhost:3001 (admin/admin)"
echo "  Prometheus:   http://localhost:9090"
echo "  Backend API:  http://localhost:8080"
echo ""
echo "To stop all services:"
echo "  cd docker"
echo "  docker-compose down"
echo "========================================="
