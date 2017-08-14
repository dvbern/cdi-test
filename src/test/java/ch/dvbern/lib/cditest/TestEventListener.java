/*
 *  Copyright 2017 DV Bern AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * limitations under the License.
 */

package ch.dvbern.lib.cditest;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import ch.dvbern.lib.cditest.event.TestClassCreated;
import ch.dvbern.lib.cditest.event.TestFinished;
import ch.dvbern.lib.cditest.event.TestStarted;
import org.junit.Ignore;

/**
 * Holds all test events
 */
@Singleton
@Ignore
public class TestEventListener {

	private TestClassCreated testClassCreated = null;
	private TestStarted testStarted = null;
	private TestFinished testFinished = null;

	public void onTestClassCreated(@Observes final TestClassCreated testClassCreated) {
		this.testClassCreated = testClassCreated;
	}

	public void onTestStarted(@Observes final TestStarted testStarted) {
		this.testStarted = testStarted;
	}

	public void onTestFinished(@Observes final TestFinished testFinished) {
		this.testFinished = testFinished;
	}

	public TestClassCreated getTestClassCreated() {
		return testClassCreated;
	}

	public TestFinished getTestFinished() {
		return testFinished;
	}

	public TestStarted getTestStarted() {
		return testStarted;
	}

}
