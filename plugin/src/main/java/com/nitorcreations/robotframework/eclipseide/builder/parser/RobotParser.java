/**
 * Copyright 2012 Nitor Creations Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nitorcreations.robotframework.eclipseide.builder.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;

import com.nitorcreations.robotframework.eclipseide.builder.parser.state.Ignore;
import com.nitorcreations.robotframework.eclipseide.builder.parser.state.State;
import com.nitorcreations.robotframework.eclipseide.builder.util.FileMarkerManager;
import com.nitorcreations.robotframework.eclipseide.builder.util.MarkerManager;
import com.nitorcreations.robotframework.eclipseide.builder.util.NullMarkerManager;
import com.nitorcreations.robotframework.eclipseide.structure.KeywordSequence;
import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.RobotFileContents;
import com.nitorcreations.robotframework.eclipseide.structure.api.IDynamicParsedString;
import com.nitorcreations.robotframework.eclipseide.structure.api.IRobotFileContents;

/* TODO support the line continuation sequence "..." TODO support lists @{foo}, access @{foo}[0]
 * TODO support environment variables %{foo} TODO support builtin variables, section 2.5.4 TODO
 * since Robot Framework 2.6, support "number" variables ${123} ${0xFFF} ${0o777} ${0b111} TODO
 * since Robot Framework 2.5.5, all setting names can optionally include a colon at the end, for
 * example "Documentation:" */
public class RobotParser {

    private final String filename;
    private final IProgressMonitor monitor;
    private final MarkerManager markerManager;

    private State state = Ignore.STATE;
    final RobotFileContents fc = new RobotFileContents();
    KeywordSequence testcaseOrKeywordBeingParsed;
    List<? extends IDynamicParsedString> listToContinue;
    private final List<RobotLine> lexLines;

    public void setState(State newState, KeywordSequence testcaseOrKeywordBeingParsed) {
        state = newState;
        this.testcaseOrKeywordBeingParsed = testcaseOrKeywordBeingParsed;
    }

    void setContinuationList(List<? extends IDynamicParsedString> listToContinue) {
        assert listToContinue != null;
        this.listToContinue = listToContinue;
    }

    void clearContinuationList() {
        listToContinue = null;
    }

    /**
     * For files being "compiled" from disk.
     * 
     * @param file
     * @param monitor
     * @throws UnsupportedEncodingException
     * @throws CoreException
     */
    public RobotParser(final IFile file, List<RobotLine> lexLines, IProgressMonitor monitor) throws UnsupportedEncodingException, CoreException {
        this.filename = file.toString();
        this.lexLines = lexLines;
        this.monitor = monitor == null ? new NullProgressMonitor() : monitor;
        this.markerManager = new FileMarkerManager(file);
    }

    /**
     * For unit tests.
     * 
     * @param file
     *            the file path
     * @param charset
     *            the charset to read the file in
     * @param markerManager
     *            for managing markers
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     */
    public RobotParser(File file, List<RobotLine> lexLines, MarkerManager markerManager) throws UnsupportedEncodingException, FileNotFoundException {
        this.filename = file.getName();
        this.lexLines = lexLines;
        this.monitor = new NullProgressMonitor();
        this.markerManager = markerManager;
    }

    /**
     * For documents being edited.
     * 
     * @param document
     */
    public RobotParser(IDocument document, List<RobotLine> lexLines) {
        this.filename = "<document being edited>";
        this.lexLines = lexLines;
        this.monitor = new NullProgressMonitor();
        this.markerManager = new NullMarkerManager();
    }

    public IRobotFileContents parse() throws CoreException {
        try {
            System.out.println("Parsing " + filename);
            markerManager.eraseMarkers();
            for (RobotLine line : lexLines) {
                if (monitor.isCanceled()) {
                    return null;
                }
                try {
                    parseLine(line.arguments, line.lineNo, line.lineCharPos);
                } catch (CoreException e) {
                    throw new RuntimeException("Error when parsing line " + line.lineNo + ": '" + line.arguments + "'", e);
                } catch (RuntimeException e) {
                    throw new RuntimeException("Internal error when parsing line " + line.lineNo + ": '" + line.arguments + "'", e);
                }
            }

            // TODO store results
        } catch (Exception e) {
            throw new RuntimeException("Error parsing robot file " + filename, e);
        }
        return fc;
    }

    private void parseLine(List<ParsedString> arguments, int lineNo, int charPos) throws CoreException {
        if (arguments.isEmpty()) {
            return;
        }
        System.out.println(arguments);
        State oldState = state;
        state.parse(new ParsedLineInfo(this, arguments, lineNo, charPos));
        if (oldState != state) {
            System.out.println("State " + oldState + " -> " + state);
        }
    }

    public MarkerManager getMarkerManager() {
        return markerManager;
    }

}
