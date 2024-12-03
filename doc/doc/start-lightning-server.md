<!--
Copyright 2023 ZETARIS Pty Ltd

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies
or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
-->

## 1. prerequisites
Lighting Catalog has been tested with :
* JRE 1.8, JRE 1.8, 11, 17, 18, 19.
* Apache Spark(ver3.4.x, ver3.5.x)

## 2. Lightning Architecture
Lightning Catalog has two main component :
* API Server(port 8080 by default) : RESTful API server to execute all commands and query
* Web Server(port 8081 by default) : provide web ui. It is talking to API Server.
* Lightning Frontend : React application.

## 3. Install Spark
Lightning catalog leverages Apache Spark as a compute engine. Spark package can be downloaded https://spark.apache.org/downloads.html.
Environment variable SPARK_HOME need to be set.

## 4. Install Lightning Catalog
Lightning Catalog package can be downloaded in github release page(https://github.com/zetaris/lightning-catalog/releases) or can be built from source.

Assuming LIGT_HOME variable point the installation directory.

## 5. run lightning server
* copy all 3rd party libs such as JDBC driver into $LIGT_HOME/3rd-party-lib directory
* change LIGHTNING_SERVER_PORT(8080 by default) for API port, LIGHTNING_GUI_PORT(8081 by default) for web server port in ${LIGT_HOME}/bin/start-ligt.sh
* run ${LIGT_HOME}/bin/start-ligt.sh
* connect to http://localhost:LIGHTNING_GUI_PORT from web browser.
