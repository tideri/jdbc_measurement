#!/bin/bash
# You need to apply setPoolable() patch beforehand. 

set -eu

LOG_DIR=$(dirname ${0})/log
EXE_FILE=TestMeasure

for num in `seq 1 1000`;do

###################################################################################
LOG_FILE=${LOG_DIR}/measure_setPoolable_false.log
CacheQueries=100000 # default=256
CacheSizeMiB=1   # default=5
Threshold=5      # default=5
LongQueryCount=2
IsPoolable=false

java ${EXE_FILE} ${LongQueryCount} ${CacheQueries} ${CacheSizeMiB} ${Threshold} ${IsPoolable} 2>&1|tee -a ${LOG_FILE}


###################################################################################
LOG_FILE=${LOG_DIR}/measure_setPoolable_true.log
CacheQueries=100000 # default=256
CacheSizeMiB=1   # default=5
Threshold=5      # default=5
LongQueryCount=2
IsPoolable=true

java ${EXE_FILE} ${LongQueryCount} ${CacheQueries} ${CacheSizeMiB} ${Threshold} ${IsPoolable} 2>&1|tee -a ${LOG_FILE}


###################################################################################
LOG_FILE=${LOG_DIR}/measure_cache_on.log
CacheQueries=100000 # default=256
CacheSizeMiB=1   # default=5
Threshold=5      # default=5
LongQueryCount=0
IsPoolable=true

java ${EXE_FILE}  ${CacheQueries} ${CacheSizeMiB} ${Threshold} ${IsPoolable} 2>&1|tee -a ${LOG_FILE}


###################################################################################

LOG_FILE=${LOG_DIR}/measure_cache_off.log
CacheQueries=0
CacheSizeMiB=0
Threshold=5      # default=5
LongQueryCount=(2)
IsPoolable=true

java ${EXE_FILE} ${LongQueryCount} ${CacheQueries} ${CacheSizeMiB} ${Threshold} ${IsPoolable} 2>&1|tee -a ${LOG_FILE}


done
