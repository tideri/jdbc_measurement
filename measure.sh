#!/bin/bash
# You need to apply setPoolable() patch beforehand. 

set -eu

LOG_DIR=$(dirname ${0})/log
EXE_FILE=TestMeasure

for num in `seq 1 1000`
do
    ### Case 1 #######################################################################
    # Case 1 should be faster than case 2.
    # This case performs setPoolable(false) to the "long" queries.
    # All the "long" queries won't be put into the cache and 
    # won't pushed "short" queries away from the cache.
    # "Short" queries can use cached queries (and will hopefully be faster).
    ##################################################################################
    
    LOG_FILE=${LOG_DIR}/measure_setPoolable_false.log
    CacheQueries=100000 # default=256
    CacheSizeMiB=1   # default=5
    Threshold=5      # default=5
    LongQueryCount=2
    IsPoolable=false

    java ${EXE_FILE} ${LongQueryCount} ${CacheQueries} ${CacheSizeMiB} ${Threshold} ${IsPoolable} 2>&1|tee -a ${LOG_FILE}

    ### Case 2 #######################################################################
    # Case 2 should be slower than case 1.
    # This case performs setPoolable(true) to the "long" queries.
    # All the "long" queries will be put into the cache and
    # will push "short" queries away from the cache.
    # "Short" queries cannot use cached queries (and will hopefully be slower).
    ##################################################################################
    
    LOG_FILE=${LOG_DIR}/measure_setPoolable_true.log
    CacheQueries=100000 # default=256
    CacheSizeMiB=1   # default=5
    Threshold=5      # default=5
    LongQueryCount=2
    IsPoolable=true

    java ${EXE_FILE} ${LongQueryCount} ${CacheQueries} ${CacheSizeMiB} ${Threshold} ${IsPoolable} 2>&1|tee -a ${LOG_FILE}

    ### Following case 3 & case 4 are used for debug #################################

    ### Case 3 #######################################################################
    # Case 3 is equivalent to case 1. 
    # This case sets LongQueryCount to 0, which means no "long" queries will be issued.
    # And "short" queries can use cached queries (and will hopefully be faster).
    ##################################################################################
    
    LOG_FILE=${LOG_DIR}/measure_cache_on.log
    CacheQueries=100000 # default=256
    CacheSizeMiB=1   # default=5
    Threshold=5      # default=5
    LongQueryCount=0
    IsPoolable=true

    java ${EXE_FILE} ${LongQueryCount} ${CacheQueries} ${CacheSizeMiB} ${Threshold} ${IsPoolable} 2>&1|tee -a ${LOG_FILE}

    ### Case 4 #######################################################################
    # Case 4 is equivalent to case 2. 
    # This case sets CacheQueries (preparedStatementCacheQueries) to 0 and
    # CacheSizeMiB (preparedStatementCacheSizeMiB) to 0.
    # This means neiter "short" nor "long" queries will be cached.
    # "short" queris cannot use cached queries (and will hopefully be slower).
    ##################################################################################

    LOG_FILE=${LOG_DIR}/measure_cache_off.log
    CacheQueries=0   # default=256
    CacheSizeMiB=0   # defalut=5
    Threshold=5      # default=5
    LongQueryCount=2
    IsPoolable=true

    java ${EXE_FILE} ${LongQueryCount} ${CacheQueries} ${CacheSizeMiB} ${Threshold} ${IsPoolable} 2>&1|tee -a ${LOG_FILE}

done
