/***********************************************************************************************
 * Copyright (c) Microsoft Corporation All rights reserved.
 * 
 * MIT License:
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ***********************************************************************************************/

package com.microsoft.gittf.core.util;

import com.microsoft.gittf.core.Messages;
import com.microsoft.tfs.core.clients.versioncontrol.events.NonFatalErrorEvent;
import com.microsoft.tfs.core.clients.versioncontrol.events.NonFatalErrorListener;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Failure;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.SeverityType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;

public class WorkspaceOperationErrorListener
    implements NonFatalErrorListener

{
    private Workspace workspace;
    private String lastError;
    private Throwable lastException;

    public static final WorkspaceOperationErrorListener EMPTY = new WorkspaceOperationErrorListener();

    public WorkspaceOperationErrorListener(Workspace workspace)
    {
        Check.notNull(workspace, "workspace"); //$NON-NLS-1$

        this.workspace = workspace;

        initialize();
    }

    private WorkspaceOperationErrorListener()
    {

    }

    public void onNonFatalError(NonFatalErrorEvent event)
    {
        Failure failure = event.getFailure();
        if (failure != null && failure.getSeverity() == SeverityType.ERROR)
        {
            lastError = event.getMessage();
        }
        else if (failure == null && event.getThrowable() != null)
        {
            lastException = event.getThrowable();
        }
    }

    public void validate()
        throws Exception
    {
        if (lastError != null && lastError.length() > 0)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Messages.getString("WorkspaceOperationErrorListener.ErrorMessage")); //$NON-NLS-1$
            sb.append(System.getProperty("line.separator")); //$NON-NLS-1$
            sb.append(lastError);

            throw new Exception(sb.toString());
        }
        else if (lastException != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(Messages.getString("WorkspaceOperationErrorListener.ErrorMessage")); //$NON-NLS-1$
            sb.append(System.getProperty("line.separator")); //$NON-NLS-1$
            sb.append(lastException.getLocalizedMessage());

            throw new Exception(sb.toString());
        }
    }

    public void dispose()
    {
        if (workspace != null)
        {
            workspace.getClient().getEventEngine().removeNonFatalErrorListener(this);
        }
    }

    private void initialize()
    {
        if (workspace != null)
        {
            workspace.getClient().getEventEngine().addNonFatalErrorListener(this);
        }
    }
}
