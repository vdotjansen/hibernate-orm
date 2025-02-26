package org.hibernate.orm.test.type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.RequiresDialect;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SessionFactory
@DomainModel(annotatedClasses = {OracleNestedTableTest.Container.class})
@RequiresDialect(OracleDialect.class)
public class OracleNestedTableTest {

	@Test public void test(SessionFactoryScope scope) {
		Container container = new Container();
		container.activityTypes = new ActivityType[] { ActivityType.Work, ActivityType.Play };
		container.strings = new String[] { "hello", "world" };
		scope.inTransaction( s -> s.persist( container ) );
		Container c = scope.fromTransaction( s-> s.createQuery("from ContainerWithArrays where strings = ?1", Container.class ).setParameter(1, new String[] { "hello", "world" }).getSingleResult() );
		assertArrayEquals( c.activityTypes, new ActivityType[] { ActivityType.Work, ActivityType.Play } );
		assertArrayEquals( c.strings, new String[] { "hello", "world" } );
		c = scope.fromTransaction( s-> s.createQuery("from ContainerWithArrays where activityTypes = ?1", Container.class ).setParameter(1, new ActivityType[] { ActivityType.Work, ActivityType.Play }).getSingleResult() );
	}

	@Test public void testSchema(SessionFactoryScope scope) {
		scope.inSession( s -> {
			try ( Connection c = s.getJdbcConnectionAccess().obtainConnection() ) {
				ResultSet tableInfo = c.getMetaData().getColumns(null, null, "CONTAINERWITHARRAYS", "STRINGS" );
				while ( tableInfo.next() ) {
					String type = tableInfo.getString(6);
					assertEquals( "STRINGARRAY", type );
					return;
				}
				fail("named enum column not exported");
			}
			catch (SQLException e) {
				throw new RuntimeException(e);
			}
		});
		scope.inSession( s -> {
			try ( Connection c = s.getJdbcConnectionAccess().obtainConnection() ) {
				ResultSet tableInfo = c.getMetaData().getColumns(null, null, "CONTAINERWITHARRAYS", "ACTIVITYTYPES" );
				while ( tableInfo.next() ) {
					String type = tableInfo.getString(6);
					assertEquals( "ACTIVITYTYPEARRAY", type );
					return;
				}
				fail("named enum column not exported");
			}
			catch (SQLException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public enum ActivityType { Work, Play, Sleep }

	@Entity(name = "ContainerWithArrays")
	public static class Container {

		@Id @GeneratedValue Long id;

		@Array(length = 33)
		@Column(length = 25)
		@JdbcTypeCode(SqlTypes.ARRAY)
		String[] strings;

		@Array(length = 2)
		ActivityType[] activityTypes;

	}

}
