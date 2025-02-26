/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.dialect;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.spi.RuntimeModelCreationContext;
import org.hibernate.query.sqm.mutation.internal.temptable.GlobalTemporaryTableInsertStrategy;
import org.hibernate.query.sqm.mutation.internal.temptable.GlobalTemporaryTableMutationStrategy;
import org.hibernate.dialect.temptable.TemporaryTable;
import org.hibernate.dialect.temptable.TemporaryTableKind;
import org.hibernate.query.sqm.mutation.spi.SqmMultiTableInsertStrategy;
import org.hibernate.query.sqm.mutation.spi.SqmMultiTableMutationStrategy;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.spi.TypeConfiguration;

import static org.hibernate.query.sqm.produce.function.FunctionParameterType.ANY;

/**
 * An SQL dialect for the SAP HANA column store.
 * <p>
 * For more information on interacting with the SAP HANA database, refer to the
 * <a href="https://help.sap.com/viewer/4fe29514fd584807ac9f2a04f6754767/">SAP HANA SQL and System Views Reference</a>
 * and the <a href=
 * "https://help.sap.com/viewer/0eec0d68141541d1b07893a39944924e/latest/en-US/434e2962074540e18c802fd478de86d6.html">SAP
 * HANA Client Interface Programming Reference</a>.
 * <p>
 * Column tables are created by this dialect when using the auto-ddl feature.
 * 
 * @author Andrew Clemons
 * @author Jonathan Bregler
 */
public class HANAColumnStoreDialect extends AbstractHANADialect {

	public HANAColumnStoreDialect(DialectResolutionInfo info) {
		this( AbstractHANADialect.createVersion( info ) );
		registerKeywords( info );
	}
	
	public HANAColumnStoreDialect() {
		// SAP HANA 1.0 SP12 is the default
		this( DatabaseVersion.make( 1, 0, 120 ) );
	}

	public HANAColumnStoreDialect(DatabaseVersion version) {
		super( version );
	}

	@Override
	public boolean isUseUnicodeStringTypes() {
		return getVersion().isSameOrAfter( 4 ) || super.isUseUnicodeStringTypes();
	}

	@Override
	public int getMaxVarcharLength() {
		return 5000;
	}

	@Override
	protected void registerDefaultKeywords() {
		super.registerDefaultKeywords();
		registerKeyword( "array" );
		registerKeyword( "at" );
		registerKeyword( "authorization" );
		registerKeyword( "between" );
		registerKeyword( "by" );
		registerKeyword( "collate" );
		registerKeyword( "empty" );
		registerKeyword( "filter" );
		registerKeyword( "grouping" );
		registerKeyword( "no" );
		registerKeyword( "not" );
		registerKeyword( "of" );
		registerKeyword( "over" );
		registerKeyword( "recursive" );
		registerKeyword( "row" );
		registerKeyword( "table" );
		registerKeyword( "to" );
		registerKeyword( "window" );
		registerKeyword( "within" );
	}


	@Override
	public void initializeFunctionRegistry(FunctionContributions functionContributions) {
		super.initializeFunctionRegistry(functionContributions);
		final TypeConfiguration typeConfiguration = functionContributions.getTypeConfiguration();

		// full-text search functions
		functionContributions.getFunctionRegistry().registerNamed(
				"score",
				typeConfiguration.getBasicTypeRegistry().resolve( StandardBasicTypes.DOUBLE )
		);
		functionContributions.getFunctionRegistry().registerNamed( "snippets" );
		functionContributions.getFunctionRegistry().registerNamed( "highlighted" );
		functionContributions.getFunctionRegistry().registerBinaryTernaryPattern(
				"contains",
				typeConfiguration.getBasicTypeRegistry().resolve( StandardBasicTypes.BOOLEAN ),
				"contains(?1,?2)",
				"contains(?1,?2,?3)",
				ANY, ANY, ANY,
				typeConfiguration
		);
	}

	@Override
	public String getCreateTableString() {
		return "create column table";
	}

	@Override
	public SqmMultiTableMutationStrategy getFallbackSqmMutationStrategy(
			EntityMappingType entityDescriptor,
			RuntimeModelCreationContext runtimeModelCreationContext) {
		return new GlobalTemporaryTableMutationStrategy(
				TemporaryTable.createIdTable(
						entityDescriptor,
						basename -> TemporaryTable.ID_TABLE_PREFIX + basename,
						this,
						runtimeModelCreationContext
				),
				runtimeModelCreationContext.getSessionFactory()
		);
	}

	@Override
	public SqmMultiTableInsertStrategy getFallbackSqmInsertStrategy(
			EntityMappingType entityDescriptor,
			RuntimeModelCreationContext runtimeModelCreationContext) {
		return new GlobalTemporaryTableInsertStrategy(
				TemporaryTable.createEntityTable(
						entityDescriptor,
						name -> TemporaryTable.ENTITY_TABLE_PREFIX + name,
						this,
						runtimeModelCreationContext
				),
				runtimeModelCreationContext.getSessionFactory()
		);
	}

	@Override
	public TemporaryTableKind getSupportedTemporaryTableKind() {
		return TemporaryTableKind.GLOBAL;
	}

	@Override
	public String getTemporaryTableCreateOptions() {
		return "on commit delete rows";
	}

	@Override
	public String getTemporaryTableCreateCommand() {
		// We use a row table for temporary tables here because HANA doesn't support UPDATE on temporary column tables
		return "create global temporary row table";
	}

	@Override
	public String getTemporaryTableTruncateCommand() {
		return "truncate table";
	}

	@Override
	protected boolean supportsAsciiStringTypes() {
		return true;
	}

	@Override
	protected Boolean useUnicodeStringTypesDefault() {
		return true;
	}
}
