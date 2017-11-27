# JDBC Peformance Measurement Test for setPoolable()

This repostiry is made to give peformance evidence against [pgJDBC issue 687](https://github.com/pgjdbc/pgjdbc/issues/687).

## Test Scenario
Following steps are performed:
1. Set up properties :preparedStatementCacheSizeMiB=1 and argument of setPoolable()
1. Run short queries to fill in the cache
1. Run long queries
   - Case 1:if setPoolable(false), then short queries are still cached
   - Case 2:if setPoolable(true), then long queries are cached and pushed short queris away
1. Again run short queries and measure the time and throuput

## Prepartion

- Apply patch which was attached to [pgJDBC issue 687](https://github.com/pgjdbc/pgjdbc/issues/687). and build JDBC.
- Build `TestMeasure.java`
- Use (DBT-3(TPC-H) database)[https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/]
  - I just used DDL of DBT-3. In other words, no data was acutually stored in the database to purely compare the effect of statement cache (setPoolable(true/false) of "long" queries.)

## Usage

```
./measure.sh
```

`measure.sh` calls `TestMeasure` and then the output will be shown.

### `measure.sh`
Mainly following 2 test case will be done and you can compare the outputs

|test case|description|
|--------|--------|
|case 1|Case 1 should be faster than case 2. This case performs setPoolable(false) to the "long" queries.<br>All the "long" queries won't be put into the cache and  won't pushed "short" queries away from the cache.<br>"Short" queries can use cached queries (and will hopefully be faster). |
|case 2|Case 2 should be slower than case 1. This case performs setPoolable(true) to the "long" queries.<br>All the "long" queries will be put into the cache and will push "short" queries away from the cache.<br>"Short" queries cannot use cached queries (and will hopefully be slower).|

## Result example
Here is an example of the outputs of case 1 and case 2. These are the averages.
- case 1 (Long queries were not cached.)

|time|TPS|preparedStatementCacheQueries|preparedStatementCacheSizeMiB|IsPoolable|
|---|---|---|---|---|
|80.262|6046.811|100000|1|true|

- case 2 (Long queries were cached.)

|time|TPS|preparedStatementCacheQueries|preparedStatementCacheSizeMiB|IsPoolable|
|---|---|---|---|---|
|360.682|1339.782|100000|1|true|		
