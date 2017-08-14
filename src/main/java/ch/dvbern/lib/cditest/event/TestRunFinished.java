/*
 * Copyright 2014 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschuetzt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulaessig. Dies gilt
 * insbesondere fuer Vervielfaeltigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht uebergeben ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 *
 */
package ch.dvbern.lib.cditest.event;

import org.junit.runner.Result;

/**
 * Event which notifies that a complete Test Run has finished.
 * Might be used to e.g. cleanup temporary directories created during testing.
 */
public class TestRunFinished {

	private final Result result;

	public TestRunFinished(Result result) {
		this.result = result;
	}

	public Result getResult() {
		return result;
	}
}
