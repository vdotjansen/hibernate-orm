/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.from;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.spi.NavigablePath;
import org.hibernate.sql.ast.spi.SqlAliasBase;

/**
 * A table group for collection tables of plural attributes.
 *
 * @author Christian Beikov
 */
public class CollectionTableGroup extends StandardTableGroup implements PluralTableGroup {

	private TableGroup indexTableGroup;
	private TableGroup elementTableGroup;

	public CollectionTableGroup(
			boolean canUseInnerJoins,
			NavigablePath navigablePath,
			PluralAttributeMapping tableGroupProducer,
			boolean fetched,
			String sourceAlias,
			TableReference primaryTableReference,
			boolean realTableGroup,
			SqlAliasBase sqlAliasBase,
			Predicate<String> tableReferenceJoinNameChecker,
			BiFunction<String, TableGroup, TableReferenceJoin> tableReferenceJoinCreator,
			SessionFactoryImplementor sessionFactory) {
		super(
				canUseInnerJoins,
				navigablePath,
				tableGroupProducer,
				fetched,
				sourceAlias,
				primaryTableReference,
				realTableGroup,
				sqlAliasBase,
				tableReferenceJoinNameChecker,
				tableReferenceJoinCreator,
				sessionFactory
		);
	}

	@Override
	public PluralAttributeMapping getModelPart() {
		return (PluralAttributeMapping) super.getModelPart();
	}

	@Override
	public TableGroup getElementTableGroup() {
		return elementTableGroup;
	}

	@Override
	public TableGroup getIndexTableGroup() {
		return indexTableGroup;
	}

	public void registerIndexTableGroup(TableGroupJoin indexTableGroupJoin) {
		assert this.indexTableGroup == null;
		this.indexTableGroup = indexTableGroupJoin.getJoinedGroup();
		addNestedTableGroupJoin( indexTableGroupJoin );
	}

	public void registerElementTableGroup(TableGroupJoin elementTableGroupJoin) {
		assert this.elementTableGroup == null;
		this.elementTableGroup = elementTableGroupJoin.getJoinedGroup();
		addNestedTableGroupJoin( elementTableGroupJoin );
	}

	@Override
	public TableReference getTableReference(
			NavigablePath navigablePath,
			String tableExpression,
			boolean resolve) {
		final TableReference tableReference = super.getTableReference(
				navigablePath,
				tableExpression,
				resolve
		);
		if ( tableReference != null ) {
			return tableReference;
		}
		if ( indexTableGroup != null && ( navigablePath == null || indexTableGroup.getNavigablePath().isParent( navigablePath ) ) ) {
			final TableReference indexTableReference = indexTableGroup.getTableReference(
					navigablePath,
					tableExpression,
					resolve
			);
			if ( indexTableReference != null ) {
				return indexTableReference;
			}
		}
		if ( elementTableGroup != null && ( navigablePath == null || elementTableGroup.getNavigablePath().isParent( navigablePath ) ) ) {
			final TableReference elementTableReference = elementTableGroup.getTableReference(
					navigablePath,
					tableExpression,
					resolve
			);
			if ( elementTableReference != null ) {
				return elementTableReference;
			}
		}
		return null;
	}

}
