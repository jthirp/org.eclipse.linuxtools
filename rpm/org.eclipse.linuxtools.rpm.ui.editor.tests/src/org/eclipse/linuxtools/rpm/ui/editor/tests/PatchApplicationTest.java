/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.tests;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileErrorHandler;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileElement;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePatchMacro;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileSource;

public class PatchApplicationTest extends TestCase {
	
	private SpecfileParser parser;
	private Specfile specfile;
	private IFile testFile;
	private Document testDocument;
	private SpecfileErrorHandler errorHandler;
	private SpecfileTestProject testProject;

	protected void newFile(String contents) throws Exception {
		testFile.setContents(new ByteArrayInputStream(contents.getBytes()), false, false, null);
		testDocument = new Document(contents);
		errorHandler = new SpecfileErrorHandler(testFile, testDocument);
		parser.setErrorHandler(errorHandler);
		specfile = parser.parse(testDocument);
	}
	
	@Override
	protected void setUp() throws Exception {
		testProject = new SpecfileTestProject();
		testFile = testProject.createFile("test.spec");
		parser = new SpecfileParser();
	}

	@Override
	protected void tearDown() throws Exception {
		testProject.dispose();
	}
	public PatchApplicationTest(String name) {
		super(name);
	}

	public void testParsePatchApplication() {
		String specText = "Patch3: somefilesomewhere.patch\n%patch3";
		String testText = "%patch3";
		
		try {
			newFile(specText);
			SpecfileElement element = parser.parseLine(testText, specfile, 1);
			assertEquals(SpecfilePatchMacro.class, element.getClass());
			assertEquals(3, ((SpecfilePatchMacro) element).getPatchNumber());
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testPatchLineNumber() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" +
		"%patch3";
		
		try {
			newFile(specText);
			SpecfileSource thisPatch = specfile.getPatch(3);
			List<Integer> usedList = new ArrayList<Integer>(1);
			usedList.add(Integer.valueOf(1));
			assertEquals(thisPatch.getLinesUsed(), usedList);
		} catch (Exception e) {
			fail();
		}
	}
	
	public void testMultiplePatchLineNumbers() {
		String specText = "Patch3: somefilesomewhere.patch" + "\n" +
		"%patch3" + "\n" +
		"blah" + "\n" +
		"%patch3";
		
		try {
			newFile(specText);
			SpecfileSource thisPatch = specfile.getPatch(3);
			List<Integer> usedList = new ArrayList<Integer>(2);
			usedList.add(Integer.valueOf(1));
			usedList.add(Integer.valueOf(3));
			assertEquals(thisPatch.getLinesUsed(), usedList);
		} catch (Exception e) {
			fail();
		}
	}
}
