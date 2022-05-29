#!/bin/sh
set -e
echo "building functions"
cd lambda && mvn clean package
echo "provisioning with pulumi"
cd ../pulumi && pulumi up