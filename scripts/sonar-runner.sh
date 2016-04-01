#!/usr/bin/env bash
cd /tmp/
wget http://repo1.maven.org/maven2/org/codehaus/sonar/runner/sonar-runner-dist/2.4/sonar-runner-dist-2.4.zip
mkdir sonar-runner
unzip sonar-runner-dist-2.4.zip -d sonar-runner
rm -f /tmp/sonar-runner-dist-2.4.zip