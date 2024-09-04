#!/bin/bash
#
# Copyright 2015-2024 Open Text.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

dropwizardConfig="/maven/worker.yaml"
defaultCrashDumpFilePath="/tmp/"

####################################################
# Sets the dropwizard config file to a path passed in by environment variable if a variable was passed in and the file exists there.
####################################################
function set_dropwizard_config_file_location_if_mounted(){
  if [ "$DROPWIZARD_CONFIG_PATH" ] && [ -e "$DROPWIZARD_CONFIG_PATH" ];
  then
    echo "Using dropwizard config file at $DROPWIZARD_CONFIG_PATH"
    dropwizardConfig="$DROPWIZARD_CONFIG_PATH"
  fi
}

####################################################
# Sets the CRASH_DUMP_FILE_PATH to a default value if it is not set
####################################################
function set_default_crash_dump_file_path_if_not_set() {
  if [ -z "$CRASH_DUMP_FILE_PATH" ];
    then
      echo "CRASH_DUMP_FILE_PATH was not set - using default: $defaultCrashDumpFilePath"
      export CRASH_DUMP_FILE_PATH=$defaultCrashDumpFilePath
      echo "Updated CRASH_DUMP_FILE_PATH: $CRASH_DUMP_FILE_PATH"
  fi
}

set_dropwizard_config_file_location_if_mounted

# If the CAF_APPNAME and CAF_CONFIG_PATH environment variables are not set, then use the
# JavaScript-encoded config files that are built into the container
if [ -z "$CAF_APPNAME" ] && [ -z "$CAF_CONFIG_PATH" ];
then
  export CAF_APPNAME=caf/worker
  export CAF_CONFIG_PATH=/maven/config
  export CAF_CONFIG_DECODER=JavascriptDecoder
  export CAF_CONFIG_ENABLE_SUBSTITUTOR=false
fi

# If CRASH_DUMP_ON_OUT_OF_MEMORY_ERROR is true, then add JVM argument and append CAF_WORKER_JAVA_OPTS
if [ "$CRASH_DUMP_ON_OUT_OF_MEMORY_ERROR" == "true" ]
then
  set_default_crash_dump_file_path_if_not_set

  CAF_WORKER_JAVA_OPTS="${CAF_WORKER_JAVA_OPTS} -XX:+CrashOnOutOfMemoryError -XX:ErrorFile=${CRASH_DUMP_FILE_PATH}${HOSTNAME}_crash.log"
  echo "CRASH_DUMP_ON_OUT_OF_MEMORY_ERROR set: Updated CAF_WORKER_JAVA_OPTS: $CAF_WORKER_JAVA_OPTS"
fi

# If HEAP_DUMP_ON_OUT_OF_MEMORY_ERROR is true, then add JVM argument and append CAF_WORKER_JAVA_OPTS
if [ "$HEAP_DUMP_ON_OUT_OF_MEMORY_ERROR" == "true" ];
then
  set_default_crash_dump_file_path_if_not_set

  CAF_WORKER_JAVA_OPTS="${CAF_WORKER_JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${CRASH_DUMP_FILE_PATH}${HOSTNAME}_heap_dump.hprof"
  echo "HEAP_DUMP_ON_OUT_OF_MEMORY_ERROR set: Updated CAF_WORKER_JAVA_OPTS: $CAF_WORKER_JAVA_OPTS"
fi

cd /maven
exec java $CAF_WORKER_JAVA_OPTS \
    -Dcld2.location=/maven/cld2native \
    -Dpolyglot.engine.WarnInterpreterOnly=false \
    -cp "*" \
    com.hpe.caf.worker.core.WorkerApplication \
    server \
    ${dropwizardConfig}
