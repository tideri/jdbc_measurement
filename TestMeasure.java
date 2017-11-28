/*
 * Test of Statement and Prepared Statement
 * 
 */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Properties;

public class TestMeasure {
	private Connection con = null;
	private static final int fillinQueryCount = 23; // to make the cache 1 MB
	private static final int shortQueryUnit = 21;    // # of queries issued in runShortQuery

	public TestMeasure(Connection con){
		this.con = con;
	}

	public static void main(String args[]){
		int count = 0;
		int select_count;
		int longQueryCount;
		long time = 0;
		long tps = 0;

		/* args handling is awkward but don't care for now */
		if (args.length != 5) {
			longQueryCount = 1;
		} else {
			longQueryCount = Integer. parseInt(args[0]);
		}

		String url = "jdbc:postgresql://localhost:5555/jdbctest_bk?postgres";

		Properties props = new Properties();
		setProps(props,args);

		try {
			Class.forName("org.postgresql.Driver");
		} catch(java.lang.ClassNotFoundException e){
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
		}

		try {
			Connection con = DriverManager.getConnection(url, props);
			TestMeasure testMeasure = new TestMeasure(con);
			/* Fill up the cache */
		
	       		for (select_count = 0; select_count < fillinQueryCount; select_count++) {
       				testMeasure.runShortQuery(select_count);
			}
			/* Run large queries. runLongQuery() internally sets up true/false against the setPoolable()'s arg*/
			for (int i = 0; i < 10; i++){
				for (select_count = 0; select_count < longQueryCount; select_count++) {
					testMeasure.runLongQuery(select_count, Boolean.valueOf(args[4]));
				}
			}
			/*---------- Measure Start----------*/
			long start = System.currentTimeMillis();

			for (select_count = 0; select_count < fillinQueryCount; select_count++) {
				testMeasure.runShortQuery(select_count);
			}

			long end = System.currentTimeMillis();
			/*---------- Measure End----------*/
			time = end - start;

			con.close();
		} catch(SQLException ex){
			System.err.print("SQLException: ");
			System.err.println(ex.getMessage());
		}

		/* time is milli-seconds */
		int numTx = fillinQueryCount * shortQueryUnit;
		tps = (numTx * 1000) / time;

		/*
		 * # of transaction, time [ms], tps ,
		 * preparedStatementCacheQueries, preparedStatementCacheSizeMiB
		 * argument of setPoolable()
		 */
		System.out.println(numTx +", " +  time +", " + tps
					+ ", "+ props.getProperty("preparedStatementCacheQueries")
					+ ", " + props.getProperty("preparedStatementCacheSizeMiB")
					+ ", " + args[4]
		);
	}

	/* Does not check the sufficient arguments are provided */
	private static void setProps(Properties props, String args[]) {
		// default: 256; if 0, cache disabled
		props.setProperty("preparedStatementCacheQueries",args[1]);
		// default: 5; if 0, cache disabled
		props.setProperty("preparedStatementCacheSizeMiB",args[2]);
		// default: 5;
		props.setProperty("prepareThreshold",args[3]);
	}

	public void runShortQuery(int select_count) throws SQLException {
		String str;
		ResultSet rs;
		PreparedStatement pstmt;

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT l_returnflag, l_linestatus, sum(l_quantity) AS sum_qty, sum(l_extendedprice) AS sum_base_price, sum(l_extendedprice * (1 - l_discount)) AS sum_disc_price, sum(l_extendedprice * (1 - l_discount) * (1 + l_tax)) AS sum_charge, avg(l_quantity) AS avg_qty, avg(l_extendedprice) AS avg_price, avg(l_discount) AS avg_disc, count(*) AS count_order FROM lineitem WHERE l_shipdate <= date'1998-12-01' - interval '80 days' GROUP BY l_returnflag, l_linestatus ORDER BY l_returnflag, l_linestatus";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT s_acctbal, s_name, n_name, p_partkey, p_mfgr, s_address, s_phone, s_comment FROM part, supplier, partsupp, nation, region WHERE p_partkey = ps_partkey AND s_suppkey = ps_suppkey AND p_size = 49 AND p_type LIKE '%BRASS' AND s_nationkey = n_nationkey AND n_regionkey = r_regionkey AND r_name = 'AMERICA' AND ps_supplycost = ( SELECT min(ps_supplycost) FROM partsupp, supplier, nation, region WHERE p_partkey = ps_partkey AND s_suppkey = ps_suppkey AND s_nationkey = n_nationkey AND n_regionkey = r_regionkey AND r_name = 'AMERICA' ) ORDER BY s_acctbal DESC, n_name, s_name, p_partkey LIMIT 20";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT l_orderkey, sum(l_extendedprice * (1 - l_discount)) AS revenue, o_orderdate, o_shippriority FROM customer, orders, lineitem WHERE c_mktsegment = 'MACHINERY' AND c_custkey = o_custkey AND l_orderkey = o_orderkey AND o_orderdate < date '1995-03-09' AND l_shipdate > date '1995-03-09' GROUP BY l_orderkey, o_orderdate, o_shippriority ORDER BY revenue DESC, o_orderdate LIMIT 10";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();
				

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT o_orderpriority, count(*) AS order_count FROM orders WHERE o_orderdate >= date '1994-05-01' AND o_orderdate < date '1994-05-01' + interval '3 month' AND EXISTS ( SELECT * FROM lineitem WHERE l_orderkey = o_orderkey AND l_commitdate < l_receiptdate ) GROUP BY o_orderpriority ORDER BY o_orderpriority";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();
				

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT n_name, sum(l_extendedprice * (1 - l_discount)) AS revenue FROM customer, orders, lineitem, supplier, nation, region WHERE c_custkey = o_custkey AND l_orderkey = o_orderkey AND l_suppkey = s_suppkey AND c_nationkey = s_nationkey AND s_nationkey = n_nationkey AND n_regionkey = r_regionkey AND r_name = 'AFRICA' AND o_orderdate >= date '1996-01-01' AND o_orderdate < date '1996-01-01' + interval '1 year' GROUP BY n_name ORDER BY revenue desc";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();
				

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT sum(l_extendedprice * l_discount) AS revenue FROM lineitem WHERE l_shipdate >= date '1996-01-01' AND l_shipdate < date '1996-01-01' + interval '1 year' AND l_discount between 0.08 - 0.01 AND 0.08 + 0.01 AND l_quantity < 25";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();


		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT supp_nation, cust_nation, l_year, sum(volume) AS revenue FROM ( SELECT n1.n_name AS supp_nation, n2.n_name AS cust_nation, extract(year FROM l_shipdate) AS l_year, l_extendedprice * (1 - l_discount) AS volume FROM supplier, lineitem, orders, customer, nation n1, nation n2 WHERE s_suppkey = l_suppkey AND o_orderkey = l_orderkey AND c_custkey = o_custkey AND s_nationkey = n1.n_nationkey AND c_nationkey = n2.n_nationkey AND ( (n1.n_name = 'ROMANIA' AND n2.n_name = 'RUSSIA') OR (n1.n_name = 'RUSSIA' AND n2.n_name = 'ROMANIA') ) AND l_shipdate between date '1995-01-01' AND date '1996-12-31' ) AS shipping GROUP BY supp_nation, cust_nation, l_year ORDER BY supp_nation, cust_nation, l_year";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();
				

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT o_year, sum(CASE WHEN nation = 'RUSSIA' THEN volume ELSE 0 END) / sum(volume) AS mkt_share FROM ( SELECT extract(year FROM o_orderdate) AS o_year, l_extendedprice * (1 - l_discount) AS volume, n2.n_name AS nation FROM part, supplier, lineitem, orders, customer, nation n1, nation n2, region WHERE p_partkey = l_partkey AND s_suppkey = l_suppkey AND l_orderkey = o_orderkey AND o_custkey = c_custkey AND c_nationkey = n1.n_nationkey AND n1.n_regionkey = r_regionkey AND r_name = 'EUROPE' AND s_nationkey = n2.n_nationkey AND o_orderdate between date '1995-01-01' AND date '1996-12-31' AND p_type = 'SMALL POLISHED COPPER' ) AS all_nations GROUP BY o_year ORDER BY o_year";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();

				
		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT nation, o_year, sum(amount) AS sum_profit FROM ( SELECT n_name AS nation, extract(year FROM o_orderdate) AS o_year, l_extendedprice * (1 - l_discount) - ps_supplycost * l_quantity AS amount FROM part, supplier, lineitem, partsupp, orders, nation WHERE s_suppkey = l_suppkey AND ps_suppkey = l_suppkey AND ps_partkey = l_partkey AND p_partkey = l_partkey AND o_orderkey = l_orderkey AND s_nationkey = n_nationkey AND p_name LIKE '%floral%' ) AS profit GROUP BY nation, o_year ORDER BY nation, o_year DESC";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();
				

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT c_custkey, c_name, sum(l_extendedprice * (1 - l_discount)) AS revenue, c_acctbal, n_name, c_address, c_phone, c_comment FROM customer, orders, lineitem, nation WHERE c_custkey = o_custkey AND l_orderkey = o_orderkey AND o_orderdate >= date '1993-09-01' AND o_orderdate < date '1993-09-01' + interval '3 month' AND l_returnflag = 'R' AND c_nationkey = n_nationkey GROUP BY c_custkey, c_name, c_acctbal, c_phone, n_name, c_address, c_comment ORDER BY revenue desc LIMIT 20";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();
				

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT ps_partkey, sum(ps_supplycost * ps_availqty) AS value FROM partsupp, supplier, nation WHERE ps_suppkey = s_suppkey AND s_nationkey = n_nationkey AND n_name = 'IRAQ' GROUP BY ps_partkey having sum(ps_supplycost * ps_availqty) > ( SELECT sum(ps_supplycost * ps_availqty) * 0.0000100000 FROM partsupp, supplier, nation WHERE ps_suppkey = s_suppkey AND s_nationkey = n_nationkey AND n_name = 'IRAQ' ) ORDER BY value DESC LIMIT 20";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();
				

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT l_shipmode, sum(CASE WHEN o_orderpriority = '1-URGENT' OR o_orderpriority = '2-HIGH' THEN 1 ELSE 0 END) AS high_line_count, sum(case when o_orderpriority <> '1-URGENT' AND o_orderpriority <> '2-HIGH' then 1 else 0 end) AS low_line_count FROM orders, lineitem WHERE o_orderkey = l_orderkey AND l_shipmode IN ('RAIL', 'TRUCK') AND l_commitdate < l_receiptdate AND l_shipdate < l_commitdate AND l_receiptdate >= date '1993-01-01' AND l_receiptdate < date '1993-01-01' + interval '1 year' GROUP BY l_shipmode ORDER BY l_shipmode";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();
				

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT c_count, count(*) AS custdist FROM ( SELECT c_custkey, count(o_orderkey) FROM customer LEFT OUTER JOIN orders ON c_custkey = o_custkey AND o_comment NOT LIKE '%pending%packages%' GROUP BY c_custkey ) AS c_orders (c_custkey, c_count) GROUP BY c_count ORDER BY custdist DESC, c_count DESC";    
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();
				

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT 100.00 * sum(CASE WHEN p_type LIKE 'PROMO%' THEN l_extendedprice * (1 - l_discount) ELSE 0 END) / sum(l_extendedprice * (1 - l_discount)) AS promo_revenue FROM lineitem, part WHERE l_partkey = p_partkey AND l_shipdate >= date '1993-11-01' AND l_shipdate < date '1993-11-01' + interval '1 month'";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT p_brand, p_type, p_size, count(DISTINCT ps_suppkey) AS supplier_cnt FROM partsupp, part WHERE p_partkey = ps_partkey AND p_brand <> 'Brand#22' AND p_type NOT LIKE 'SMALL ANODIZED%' AND p_size IN (39, 47, 12, 50, 17, 40, 9, 7) AND ps_suppkey NOT IN ( SELECT s_suppkey FROM supplier WHERE s_comment LIKE '%Customer%Complaints%' ) GROUP BY p_brand, p_type, p_size ORDER BY supplier_cnt DESC, p_brand, p_type, p_size";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();
			

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT sum(l_extendedprice) / 7.0 AS avg_yearly FROM lineitem, part WHERE p_partkey = l_partkey AND p_brand = 'Brand#21' AND p_container = 'JUMBO CAN' AND l_quantity < ( SELECT 0.2 * avg(l_quantity) FROM lineitem WHERE l_partkey = p_partkey )";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT c_name, c_custkey, o_orderkey, o_orderdate, o_totalprice, sum(l_quantity) FROM customer, orders, lineitem WHERE o_orderkey IN ( SELECT l_orderkey FROM lineitem GROUP BY l_orderkey HAVING sum(l_quantity) > 312 ) AND c_custkey = o_custkey AND o_orderkey = l_orderkey GROUP BY c_name, c_custkey, o_orderkey, o_orderdate, o_totalprice ORDER BY o_totalprice DESC, o_orderdate LIMIT 20";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();

		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT sum(l_extendedprice* (1 - l_discount)) AS revenue FROM lineitem, part WHERE ( p_partkey = l_partkey AND p_brand = 'Brand#45' AND p_container IN ('SM CASE', 'SM BOX', 'SM PACK', 'SM PKG') AND l_quantity >= 2 AND l_quantity <= 2+10 AND p_size BETWEEN 1 AND 5 AND l_shipmode IN ('AIR', 'AIR REG') AND l_shipinstruct = 'DELIVER IN PERSON' ) OR ( p_partkey = l_partkey AND p_brand = 'Brand#24' AND p_container IN ('MED BAG', 'MED BOX', 'MED PKG', 'MED PACK') AND l_quantity >= 11 AND l_quantity <= 11+10 AND p_size BETWEEN 1 AND 10 AND l_shipmode IN ('AIR', 'AIR REG') AND l_shipinstruct = 'DELIVER IN PERSON' ) OR ( p_partkey = l_partkey AND p_brand = 'Brand#24' AND p_container IN ('LG CASE', 'LG BOX', 'LG PACK', 'LG PKG') AND l_quantity >= 22 AND l_quantity <= 22+10 AND p_size BETWEEN 1 AND 15 AND l_shipmode IN ('AIR', 'AIR REG') AND l_shipinstruct = 'DELIVER IN PERSON' )";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();


		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT s_name, s_address FROM supplier, nation WHERE s_suppkey IN ( SELECT DISTINCT (ps_suppkey) FROM partsupp, part WHERE ps_partkey=p_partkey AND p_name LIKE 'thistle%' AND ps_availqty > ( SELECT 0.5 * sum(l_quantity) FROM lineitem WHERE l_partkey = ps_partkey AND l_suppkey = ps_suppkey AND l_shipdate >= '1994-01-01' AND l_shipdate < date '1994-01-01' + interval '1 year' ) ) AND s_nationkey = n_nationkey AND n_name = 'PERU' ORDER BY s_name";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();


		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT s_name, count(*) AS numwait FROM supplier, lineitem l1, orders, nation WHERE s_suppkey = l1.l_suppkey AND o_orderkey = l1.l_orderkey AND o_orderstatus = 'F' AND l1.l_receiptdate > l1.l_commitdate AND EXISTS ( SELECT * FROM lineitem l2 WHERE l2.l_orderkey = l1.l_orderkey AND l2.l_suppkey <> l1.l_suppkey ) AND not EXISTS ( SELECT * FROM lineitem l3 WHERE l3.l_orderkey = l1.l_orderkey AND l3.l_suppkey <> l1.l_suppkey AND l3.l_receiptdate > l3.l_commitdate ) AND s_nationkey = n_nationkey AND n_name = 'ETHIOPIA' GROUP BY s_name ORDER BY numwait DESC, s_name LIMIT 20";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();


		str = "/* SQL number " + select_count + " */";
		str = str + "SELECT cntrycode, count(*) AS numcust, sum(c_acctbal) AS totacctbal FROM ( SELECT substr(c_phone, 1, 2) AS cntrycode, c_acctbal FROM customer WHERE substr(c_phone, 1, 2) IN ('27', '18', '29', '14', '11', '16', '10') AND c_acctbal > ( SELECT avg(c_acctbal) FROM customer WHERE c_acctbal > 0.00 AND substr(c_phone, 1, 2) IN ('27', '18', '29', '14', '11', '16', '10') ) AND NOT EXISTS ( SELECT * FROM orders WHERE o_custkey = c_custkey ) ) AS vip GROUP BY cntrycode ORDER BY cntrycode";
		pstmt=con.prepareStatement(str);
		rs = pstmt.executeQuery();
		pstmt.close();
    }

	public void runLongQuery(int select_count, boolean isPool) throws SQLException{
		String str;
		ResultSet rs;
		PreparedStatement pstmt;
		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 1: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/1.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT l_returnflag, l_linestatus, sum(l_quantity) AS sum_qty, sum(l_extendedprice) AS sum_base_price, sum(l_extendedprice * (1 - l_discount)) AS sum_disc_price, sum(l_extendedprice * (1 - l_discount) * (1 + l_tax)) AS sum_charge, avg(l_quantity) AS avg_qty, avg(l_extendedprice) AS avg_price, avg(l_discount) AS avg_disc, count(*) AS count_order FROM lineitem WHERE l_shipdate <= date'1998-12-01' - interval '80 days' GROUP BY l_returnflag, l_linestatus ORDER BY l_returnflag, l_linestatus");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 2: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/2.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT s_acctbal, s_name, n_name, p_partkey, p_mfgr, s_address, s_phone, s_comment FROM part, supplier, partsupp, nation, region WHERE p_partkey = ps_partkey AND s_suppkey = ps_suppkey AND p_size = 49 AND p_type LIKE '%BRASS' AND s_nationkey = n_nationkey AND n_regionkey = r_regionkey AND r_name = 'AMERICA' AND ps_supplycost = ( SELECT min(ps_supplycost) FROM partsupp, supplier, nation, region WHERE p_partkey = ps_partkey AND s_suppkey = ps_suppkey AND s_nationkey = n_nationkey AND n_regionkey = r_regionkey AND r_name = 'AMERICA' ) ORDER BY s_acctbal DESC, n_name, s_name, p_partkey LIMIT 20");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();
		
		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 3: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/3.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT l_orderkey, sum(l_extendedprice * (1 - l_discount)) AS revenue, o_orderdate, o_shippriority FROM customer, orders, lineitem WHERE c_mktsegment = 'MACHINERY' AND c_custkey = o_custkey AND l_orderkey = o_orderkey AND o_orderdate < date '1995-03-09' AND l_shipdate > date '1995-03-09' GROUP BY l_orderkey, o_orderdate, o_shippriority ORDER BY revenue DESC, o_orderdate LIMIT 10");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();
		

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 4: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/4.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT o_orderpriority, count(*) AS order_count FROM orders WHERE o_orderdate >= date '1994-05-01' AND o_orderdate < date '1994-05-01' + interval '3 month' AND EXISTS ( SELECT * FROM lineitem WHERE l_orderkey = o_orderkey AND l_commitdate < l_receiptdate ) GROUP BY o_orderpriority ORDER BY o_orderpriority");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();
		

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 5: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/5.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT n_name, sum(l_extendedprice * (1 - l_discount)) AS revenue FROM customer, orders, lineitem, supplier, nation, region WHERE c_custkey = o_custkey AND l_orderkey = o_orderkey AND l_suppkey = s_suppkey AND c_nationkey = s_nationkey AND s_nationkey = n_nationkey AND n_regionkey = r_regionkey AND r_name = 'AFRICA' AND o_orderdate >= date '1996-01-01' AND o_orderdate < date '1996-01-01' + interval '1 year' GROUP BY n_name ORDER BY revenue desc");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();
		

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 6: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/6.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT sum(l_extendedprice * l_discount) AS revenue FROM lineitem WHERE l_shipdate >= date '1996-01-01' AND l_shipdate < date '1996-01-01' + interval '1 year' AND l_discount between 0.08 - 0.01 AND 0.08 + 0.01 AND l_quantity < 25");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();
		

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 7: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/7.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT supp_nation, cust_nation, l_year, sum(volume) AS revenue FROM ( SELECT n1.n_name AS supp_nation, n2.n_name AS cust_nation, extract(year FROM l_shipdate) AS l_year, l_extendedprice * (1 - l_discount) AS volume FROM supplier, lineitem, orders, customer, nation n1, nation n2 WHERE s_suppkey = l_suppkey AND o_orderkey = l_orderkey AND c_custkey = o_custkey AND s_nationkey = n1.n_nationkey AND c_nationkey = n2.n_nationkey AND ( (n1.n_name = 'ROMANIA' AND n2.n_name = 'RUSSIA') OR (n1.n_name = 'RUSSIA' AND n2.n_name = 'ROMANIA') ) AND l_shipdate between date '1995-01-01' AND date '1996-12-31' ) AS shipping GROUP BY supp_nation, cust_nation, l_year ORDER BY supp_nation, cust_nation, l_year");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();
		

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 8: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/8.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT o_year, sum(CASE WHEN nation = 'RUSSIA' THEN volume ELSE 0 END) / sum(volume) AS mkt_share FROM ( SELECT extract(year FROM o_orderdate) AS o_year, l_extendedprice * (1 - l_discount) AS volume, n2.n_name AS nation FROM part, supplier, lineitem, orders, customer, nation n1, nation n2, region WHERE p_partkey = l_partkey AND s_suppkey = l_suppkey AND l_orderkey = o_orderkey AND o_custkey = c_custkey AND c_nationkey = n1.n_nationkey AND n1.n_regionkey = r_regionkey AND r_name = 'EUROPE' AND s_nationkey = n2.n_nationkey AND o_orderdate between date '1995-01-01' AND date '1996-12-31' AND p_type = 'SMALL POLISHED COPPER' ) AS all_nations GROUP BY o_year ORDER BY o_year");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();
		

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 9: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/9.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT nation, o_year, sum(amount) AS sum_profit FROM ( SELECT n_name AS nation, extract(year FROM o_orderdate) AS o_year, l_extendedprice * (1 - l_discount) - ps_supplycost * l_quantity AS amount FROM part, supplier, lineitem, partsupp, orders, nation WHERE s_suppkey = l_suppkey AND ps_suppkey = l_suppkey AND ps_partkey = l_partkey AND p_partkey = l_partkey AND o_orderkey = l_orderkey AND s_nationkey = n_nationkey AND p_name LIKE '%floral%' ) AS profit GROUP BY nation, o_year ORDER BY nation, o_year DESC");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();
		

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 10: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/10.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT c_custkey, c_name, sum(l_extendedprice * (1 - l_discount)) AS revenue, c_acctbal, n_name, c_address, c_phone, c_comment FROM customer, orders, lineitem, nation WHERE c_custkey = o_custkey AND l_orderkey = o_orderkey AND o_orderdate >= date '1993-09-01' AND o_orderdate < date '1993-09-01' + interval '3 month' AND l_returnflag = 'R' AND c_nationkey = n_nationkey GROUP BY c_custkey, c_name, c_acctbal, c_phone, n_name, c_address, c_comment ORDER BY revenue desc LIMIT 20");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();
		

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 11: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/11.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT ps_partkey, sum(ps_supplycost * ps_availqty) AS value FROM partsupp, supplier, nation WHERE ps_suppkey = s_suppkey AND s_nationkey = n_nationkey AND n_name = 'IRAQ' GROUP BY ps_partkey having sum(ps_supplycost * ps_availqty) > ( SELECT sum(ps_supplycost * ps_availqty) * 0.0000100000 FROM partsupp, supplier, nation WHERE ps_suppkey = s_suppkey AND s_nationkey = n_nationkey AND n_name = 'IRAQ' ) ORDER BY value DESC LIMIT 20");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();
		

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 12: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/12.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT l_shipmode, sum(CASE WHEN o_orderpriority = '1-URGENT' OR o_orderpriority = '2-HIGH' THEN 1 ELSE 0 END) AS high_line_count, sum(case when o_orderpriority <> '1-URGENT' AND o_orderpriority <> '2-HIGH' then 1 else 0 end) AS low_line_count FROM orders, lineitem WHERE o_orderkey = l_orderkey AND l_shipmode IN ('RAIL', 'TRUCK') AND l_commitdate < l_receiptdate AND l_shipdate < l_commitdate AND l_receiptdate >= date '1993-01-01' AND l_receiptdate < date '1993-01-01' + interval '1 year' GROUP BY l_shipmode ORDER BY l_shipmode");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();
		

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 13: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/13.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT c_count, count(*) AS custdist FROM ( SELECT c_custkey, count(o_orderkey) FROM customer LEFT OUTER JOIN orders ON c_custkey = o_custkey AND o_comment NOT LIKE '%pending%packages%' GROUP BY c_custkey ) AS c_orders (c_custkey, c_count) GROUP BY c_count ORDER BY custdist DESC, c_count DESC");    
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();
		

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 14: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/14.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT 100.00 * sum(CASE WHEN p_type LIKE 'PROMO%' THEN l_extendedprice * (1 - l_discount) ELSE 0 END) / sum(l_extendedprice * (1 - l_discount)) AS promo_revenue FROM lineitem, part WHERE l_partkey = p_partkey AND l_shipdate >= date '1993-11-01' AND l_shipdate < date '1993-11-01' + interval '1 month'");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();
		

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 15: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/15.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT p_brand, p_type, p_size, count(DISTINCT ps_suppkey) AS supplier_cnt FROM partsupp, part WHERE p_partkey = ps_partkey AND p_brand <> 'Brand#22' AND p_type NOT LIKE 'SMALL ANODIZED%' AND p_size IN (39, 47, 12, 50, 17, 40, 9, 7) AND ps_suppkey NOT IN ( SELECT s_suppkey FROM supplier WHERE s_comment LIKE '%Customer%Complaints%' ) GROUP BY p_brand, p_type, p_size ORDER BY supplier_cnt DESC, p_brand, p_type, p_size");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();
		

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 16: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/16.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT sum(l_extendedprice) / 7.0 AS avg_yearly FROM lineitem, part WHERE p_partkey = l_partkey AND p_brand = 'Brand#21' AND p_container = 'JUMBO CAN' AND l_quantity < ( SELECT 0.2 * avg(l_quantity) FROM lineitem WHERE l_partkey = p_partkey )");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 17: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/17.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT c_name, c_custkey, o_orderkey, o_orderdate, o_totalprice, sum(l_quantity) FROM customer, orders, lineitem WHERE o_orderkey IN ( SELECT l_orderkey FROM lineitem GROUP BY l_orderkey HAVING sum(l_quantity) > 312 ) AND c_custkey = o_custkey AND o_orderkey = l_orderkey GROUP BY c_name, c_custkey, o_orderkey, o_orderdate, o_totalprice ORDER BY o_totalprice DESC, o_orderdate LIMIT 20");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 18: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/18.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT sum(l_extendedprice* (1 - l_discount)) AS revenue FROM lineitem, part WHERE ( p_partkey = l_partkey AND p_brand = 'Brand#45' AND p_container IN ('SM CASE', 'SM BOX', 'SM PACK', 'SM PKG') AND l_quantity >= 2 AND l_quantity <= 2+10 AND p_size BETWEEN 1 AND 5 AND l_shipmode IN ('AIR', 'AIR REG') AND l_shipinstruct = 'DELIVER IN PERSON' ) OR ( p_partkey = l_partkey AND p_brand = 'Brand#24' AND p_container IN ('MED BAG', 'MED BOX', 'MED PKG', 'MED PACK') AND l_quantity >= 11 AND l_quantity <= 11+10 AND p_size BETWEEN 1 AND 10 AND l_shipmode IN ('AIR', 'AIR REG') AND l_shipinstruct = 'DELIVER IN PERSON' ) OR ( p_partkey = l_partkey AND p_brand = 'Brand#24' AND p_container IN ('LG CASE', 'LG BOX', 'LG PACK', 'LG PKG') AND l_quantity >= 22 AND l_quantity <= 22+10 AND p_size BETWEEN 1 AND 15 AND l_shipmode IN ('AIR', 'AIR REG') AND l_shipinstruct = 'DELIVER IN PERSON' )");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 19: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/19.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT s_name, s_address FROM supplier, nation WHERE s_suppkey IN ( SELECT DISTINCT (ps_suppkey) FROM partsupp, part WHERE ps_partkey=p_partkey AND p_name LIKE 'thistle%' AND ps_availqty > ( SELECT 0.5 * sum(l_quantity) FROM lineitem WHERE l_partkey = ps_partkey AND l_suppkey = ps_suppkey AND l_shipdate >= '1994-01-01' AND l_shipdate < date '1994-01-01' + interval '1 year' ) ) AND s_nationkey = n_nationkey AND n_name = 'PERU' ORDER BY s_name");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 20: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/20.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT s_name, count(*) AS numwait FROM supplier, lineitem l1, orders, nation WHERE s_suppkey = l1.l_suppkey AND o_orderkey = l1.l_orderkey AND o_orderstatus = 'F' AND l1.l_receiptdate > l1.l_commitdate AND EXISTS ( SELECT * FROM lineitem l2 WHERE l2.l_orderkey = l1.l_orderkey AND l2.l_suppkey <> l1.l_suppkey ) AND not EXISTS ( SELECT * FROM lineitem l3 WHERE l3.l_orderkey = l1.l_orderkey AND l3.l_suppkey <> l1.l_suppkey AND l3.l_receiptdate > l3.l_commitdate ) AND s_nationkey = n_nationkey AND n_name = 'ETHIOPIA' GROUP BY s_name ORDER BY numwait DESC, s_name LIMIT 20");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();

		pstmt=con.prepareStatement(
			"/* use DBT-3 SQL 21: select_count" + select_count + " https://sourceforge.net/p/osdldbt/dbt3/ci/master/tree/queries/pgsql/21.sql */"
				+ "/* for simulatingly reproduce the situation which the SQL statement is very long, some comments are added */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */"
				+ "/* @@@@@@@@@@@@@@@@@@@ */"
			+ "SELECT cntrycode, count(*) AS numcust, sum(c_acctbal) AS totacctbal FROM ( SELECT substr(c_phone, 1, 2) AS cntrycode, c_acctbal FROM customer WHERE substr(c_phone, 1, 2) IN ('27', '18', '29', '14', '11', '16', '10') AND c_acctbal > ( SELECT avg(c_acctbal) FROM customer WHERE c_acctbal > 0.00 AND substr(c_phone, 1, 2) IN ('27', '18', '29', '14', '11', '16', '10') ) AND NOT EXISTS ( SELECT * FROM orders WHERE o_custkey = c_custkey ) ) AS vip GROUP BY cntrycode ORDER BY cntrycode");
		rs = pstmt.executeQuery();
		pstmt.setPoolable(isPool);
		pstmt.close();
	}
}
