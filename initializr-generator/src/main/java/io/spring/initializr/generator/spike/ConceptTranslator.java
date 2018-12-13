/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.initializr.generator.spike;

import io.spring.initializr.generator.buildsystem.DependencyType;
import io.spring.initializr.generator.util.VersionReference;
import io.spring.initializr.metadata.BillOfMaterials;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.util.Version;
import io.spring.initializr.util.VersionProperty;

/**
 * An internal class used to translate from the existing concepts to the spike concepts.
 *
 * @author Stephane Nicoll
 */
public final class ConceptTranslator {

	private ConceptTranslator() {
	}

	/**
	 * Return a {@link Version} from the spike's version.
	 * @param version the version to translate
	 * @return an equivalent {@link Version} of the spike structure
	 */
	public static Version fromVersion(
			io.spring.initializr.generator.util.Version version) {
		return Version.parse(version.toString());
	}

	/**
	 * Return a spike dependency from a {@link Dependency}.
	 * @param dependency a regular dependency
	 * @return an equivalent spike structure
	 */
	public static io.spring.initializr.generator.buildsystem.Dependency toDependency(
			Dependency dependency) {
		return new io.spring.initializr.generator.buildsystem.Dependency(
				dependency.getGroupId(), dependency.getArtifactId(),
				VersionReference.ofValue(dependency.getVersion()),
				toDependencyType(dependency.getScope()));
	}

	private static DependencyType toDependencyType(String scope) {
		switch (scope) {
		case Dependency.SCOPE_ANNOTATION_PROCESSOR:
			return DependencyType.ANNOTATION_PROCESSOR;
		case Dependency.SCOPE_COMPILE:
			return DependencyType.COMPILE;
		case Dependency.SCOPE_RUNTIME:
			return DependencyType.RUNTIME;
		case Dependency.SCOPE_COMPILE_ONLY:
			return DependencyType.ANNOTATION_PROCESSOR;
		case Dependency.SCOPE_PROVIDED:
			return DependencyType.PROVIDED_RUNTIME;
		case Dependency.SCOPE_TEST:
			return DependencyType.TEST_COMPILE;
		}
		return null;
	}

	public static io.spring.initializr.generator.util.VersionProperty toVersionProperty(
			VersionProperty versionProperty) {
		return io.spring.initializr.generator.util.VersionProperty
				.of(versionProperty.toStandardFormat(), versionProperty.isInternal());
	}

	/**
	 * Return a spike dependency from a {@link BillOfMaterials}.
	 * @param bom a regular bom
	 * @return an equivalent spike structure
	 */
	public static io.spring.initializr.generator.buildsystem.BillOfMaterials toBom(
			BillOfMaterials bom) {
		VersionReference version = (bom.getVersionProperty() != null)
				? VersionReference.ofProperty(
						ConceptTranslator.toVersionProperty(bom.getVersionProperty()))
				: VersionReference.ofValue(bom.getVersion());
		return new io.spring.initializr.generator.buildsystem.BillOfMaterials(
				bom.getGroupId(), bom.getArtifactId(), version, bom.getOrder());
	}

}
