# JDBC Peformance Measurement Test for setPoolable()

This repostiry is made to give peformance evidence against [pgJDBC issue 687](https://github.com/pgjdbc/pgjdbc/issues/687).

## Test Scenario


## Prepartion
- Apply patch which was attached to [pgJDBC issue 687](https://github.com/pgjdbc/pgjdbc/issues/687). and build JDBC.
- Build `TestMeasure.java`
- Use DBT-3(TPC-H) database
  - I just used DDL of DBT-3. In other words, no data was acutually stored in the database to purely compare the effect of statement cache (setPoolable(true/false) of "long" queries.)

## Usage

```
./measure.sh
```

`measure.sh` calls `TestMeasure` and then the output will be shown.
