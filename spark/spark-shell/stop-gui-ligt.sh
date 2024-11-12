#!/usr/bin/env bash

#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Define the port numbers to stop
SERVER_PORT=${LIGHTNING_SERVER_PORT:-8080}
GUI_PORT=${LIGHTNING_GUI_PORT:-3001}

# Function to kill process running on a specified port
kill_process_on_port() {
  local PORT=$1
  local PID=$(lsof -ti tcp:$PORT)

  if [ -z "$PID" ]; then
    echo "No process is running on port $PORT."
  else
    echo "Killing process running on port $PORT (PID: $PID)"
    kill -9 $PID
    echo "Process on port $PORT stopped."
  fi
}

# Kill processes on both ports
echo "Stopping server on port $SERVER_PORT and GUI on port $GUI_PORT..."
kill_process_on_port $SERVER_PORT
kill_process_on_port $GUI_PORT

