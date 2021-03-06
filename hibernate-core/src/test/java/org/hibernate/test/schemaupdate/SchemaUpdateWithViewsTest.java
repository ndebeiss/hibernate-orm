/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.schemaupdate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.PostgreSQL81Dialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.hibernate.testing.RequiresDialect;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;

/**
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HHH-1872")
@RequiresDialect(PostgreSQL81Dialect.class)
public class SchemaUpdateWithViewsTest extends BaseNonConfigCoreFunctionalTestCase {
	protected ServiceRegistry serviceRegistry;
	protected MetadataImplementor metadata;

	@Test
	public void testUpdateSchema() {
		SchemaUpdate schemaUpdate = new SchemaUpdate( serviceRegistry, metadata );
		schemaUpdate.execute( true, true );
	}

	@Before
	public void setUp() {
		createViewWithSameNameOfEntityTable();
		serviceRegistry = new StandardServiceRegistryBuilder()
				.applySetting( Environment.GLOBALLY_QUOTED_IDENTIFIERS, "false" )
				.applySetting( Environment.DEFAULT_SCHEMA, "public" )
				.build();
		metadata = (MetadataImplementor) new MetadataSources( serviceRegistry )
				.addAnnotatedClass( MyEntity.class )
				.buildMetadata();
	}

	private void createViewWithSameNameOfEntityTable() {
		Session session = openSession();
		Transaction transaction = session.beginTransaction();
		Query query = session.createSQLQuery( "CREATE VIEW MyEntity AS SELECT 'Hello World' " );
		query.executeUpdate();
		transaction.commit();
		session.close();
	}

	@After
	public void tearDown() {
		dropView();
		System.out.println( "********* Starting SchemaExport (drop) for TEAR-DOWN *************************" );
		SchemaExport schemaExport = new SchemaExport( serviceRegistry, metadata );
		schemaExport.drop( true, true );
		System.out.println( "********* Completed SchemaExport (drop) for TEAR-DOWN *************************" );

		StandardServiceRegistryBuilder.destroy( serviceRegistry );
		serviceRegistry = null;
	}

	private void dropView() {
		Session session = openSession();
		Transaction transaction = session.beginTransaction();
		Query query = session.createSQLQuery( "DROP VIEW MyEntity " );
		query.executeUpdate();
		transaction.commit();
		session.close();
	}


	@Entity
	@Table(name = "MyEntity", indexes = {@Index(columnList = "id", name = "user_id_hidx")})
	public static class MyEntity {
		private int id;

		@Id
		public int getId() {
			return this.id;
		}

		public void setId(final int id) {
			this.id = id;
		}
	}
}
