/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm;

import org.hibernate.query.SemanticException;

/**
 * Indicates we were not able to resolve a given "path structure" as an entity name.
 *
 * @apiNote JPA generally requires this to be reported as the much less useful
 *          {@link IllegalArgumentException}.
 *
 * todo (6.0) : account for this in the "exception conversion" handling
 *
 * @author Steve Ebersole
 */
public class UnknownEntityException extends SemanticException {
	private final String entityName;

	public UnknownEntityException(String entityName) {
		this( "Could not resolve entity '" + entityName + "'", entityName );
	}

	public UnknownEntityException(String message, String entityName) {
		super( message );
		this.entityName = entityName;
	}

	public String getEntityName() {
		return entityName;
	}
}
