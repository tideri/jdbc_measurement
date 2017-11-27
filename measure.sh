#!/bin/bash
# You need to do 'git checkout dev' in jdbc directory
# If you want to display debug messages, please switch your JDBC branch to pstmtchache

set -eu

LOG_DIR=$(dirname ${0})/log
EXE_FILE=TestMeasure

for num in `seq 1 1000`;do

###################################################################################
LOG_FILE=${LOG_DIR}/measure_setPoolable_false.log
CacheQueries=100000 # default=256
CacheSizeMiB=1   # default=5
Threshold=5      # default=5
LongQueryCount=(2)
IsPoolable=false

for n in ${LongQueryCount[@]};do
    java ${EXE_FILE} ${n} ${CacheQueries} ${CacheSizeMiB} ${Threshold} ${IsPoolable} 2>&1|tee -a ${LOG_FILE}
done

###################################################################################
LOG_FILE=${LOG_DIR}/measure_setPoolable_true.log
CacheQueries=100000 # default=256
CacheSizeMiB=1   # default=5
Threshold=5      # default=5
LongQueryCount=(2)
IsPoolable=true

for n in ${LongQueryCount[@]};do
    java ${EXE_FILE} ${n} ${CacheQueries} ${CacheSizeMiB} ${Threshold} ${IsPoolable} 2>&1|tee -a ${LOG_FILE}
done

###################################################################################
LOG_FILE=${LOG_DIR}/measure_cache_on.log
CacheQueries=100000 # default=256
CacheSizeMiB=1   # default=5
Threshold=5      # default=5
LongQueryCount=(2)
IsPoolable=true

java ${EXE_FILE} 0 ${CacheQueries} ${CacheSizeMiB} ${Threshold} ${IsPoolable} 2>&1|tee -a ${LOG_FILE}


###################################################################################

LOG_FILE=${LOG_DIR}/measure_cache_off.log
CacheQueries=0
CacheSizeMiB=0
Threshold=5      # default=5
LongQueryCount=(2)
IsPoolable=true

for n in ${LongQueryCount[@]};do
    java ${EXE_FILE} ${n} ${CacheQueries} ${CacheSizeMiB} ${Threshold} ${IsPoolable} 2>&1|tee -a ${LOG_FILE}
done

done
