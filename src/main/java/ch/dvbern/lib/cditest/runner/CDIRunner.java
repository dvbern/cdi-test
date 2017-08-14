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

package ch.dvbern.lib.cditest.runner;

import javax.annotation.PostConstruct;

import ch.dvbern.lib.cditest.event.TestClassCreated;
import ch.dvbern.lib.cditest.event.TestFinished;
import ch.dvbern.lib.cditest.event.TestResult;
import ch.dvbern.lib.cditest.event.TestRunFinished;
import ch.dvbern.lib.cditest.event.TestStarted;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.exceptions.UnsatisfiedResolutionException;
import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit4-Runner which CDI-Enables Tests.
 */
public class CDIRunner extends BlockJUnit4ClassRunner {

	private final Class<?> klass;

	private static final Weld weld;
	private static final WeldContainer CONTAINER;
	private static boolean weldRunning = false;
	private static ShutdownOnFinishedListener weldShutdownListener = null;

	private static final Logger LOG = LoggerFactory.getLogger(CDIRunner.class);

	static {
		LOG.info("Starting up Weld...");
		weld = new Weld();
		CONTAINER = weld.initialize();
		weldRunning = true;
		LOG.info("Weld startup finished");
	}

	/**
	 * Der Listener kuemmer sich darum, das am Ende des gesamten Test-Runs der Weld-Container
	 * definiert gestoppt wird.
	 * Damit werden dann {@link PostConstruct} und aehnliche Konstrukte aufgerufen.
	 */
	private static final class ShutdownOnFinishedListener extends RunListener {
		@Override
		public void testRunFinished(Result result) throws Exception {
			if (!weldRunning) {
				LOG.error("Weld not running???\nEither CDIRunner is broken or there was an error initializing Weld");
			} else {
				LOG.info("Shutting down Weld...");
				CONTAINER.getBeanManager().fireEvent(new TestRunFinished(result));

				weld.shutdown();
				weldRunning = false;
				LOG.info("Weld shutdown finished");
			}
		}
	}

	/**
	 * Versucht, den {@link ShutdownOnFinishedListener} genau einmal im aktuellen @link {@link RunNotifier} zu registrieren.
	 */
	private static void registerShutdownListenerOnce(RunNotifier notifier) {
		if (weldShutdownListener == null) {
			weldShutdownListener = new ShutdownOnFinishedListener();
			notifier.addListener(weldShutdownListener);
		}
	}

	/**
	 * Creates a BlockJUnit4ClassRunner to run {@code klass}
	 *
	 * @param klass Test Class
	 * @throws org.junit.runners.model.InitializationError if the test class is malformed.
	 */
	public CDIRunner(final Class<?> klass) throws InitializationError {
		super(klass);
		this.klass = klass;
	}

	@Override
	protected Object createTest() throws Exception {

		Object createdTest = null;
		try {
			createdTest = CONTAINER.instance().select(klass).get();
		} catch (UnsatisfiedResolutionException e) {
			LOG.error("Unable to create test class {}. Did you create an empty 'META-INF/beans.xml' file in the " + "test's resources folder?", klass);
			throw e;
		}
		CONTAINER.getBeanManager().fireEvent(new TestClassCreated(createdTest));
		return createdTest;
	}

	@Override
	// Das Abfangen von {@link Throwable} ist hier nötig: siehe {@link org.junit.runners.model.Statement}
	@SuppressWarnings("PMD.AvoidCatchingThrowable")
	protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
		registerShutdownListenerOnce(notifier);

		final EachTestNotifier eachNotifier = makeNotifier(method, notifier);
		if (method.getAnnotation(Ignore.class) != null) {
			eachNotifier.fireTestIgnored();
			return;
		}
		CONTAINER.getBeanManager().fireEvent(new TestStarted(method.getMethod()));
		eachNotifier.fireTestStarted();
		TestResult result = TestResult.PASSED;
		try {
			methodBlock(method).evaluate();
		} catch (AssumptionViolatedException e) {
			result = TestResult.ASSUMPTION_VIOLATED;
			eachNotifier.addFailedAssumption(e);
		} catch (Throwable e) {
			result = TestResult.FAILED;
			eachNotifier.addFailure(e);
		} finally {
			CONTAINER.getBeanManager().fireEvent(new TestFinished(method.getMethod(), result));
			eachNotifier.fireTestFinished();
		}
	}

	private EachTestNotifier makeNotifier(final FrameworkMethod method, final RunNotifier notifier) {
		final Description description = describeChild(method);
		return new EachTestNotifier(notifier, description);
	}
}
