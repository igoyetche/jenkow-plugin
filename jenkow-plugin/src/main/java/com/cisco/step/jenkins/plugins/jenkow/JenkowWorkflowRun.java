package com.cisco.step.jenkins.plugins.jenkow;

import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.TaskListener;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.jenkinsci.plugins.asyncjob.AsyncRun;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class JenkowWorkflowRun extends AsyncRun<JenkowWorkflowJob,JenkowWorkflowRun> {
    private String procId;

    public JenkowWorkflowRun(JenkowWorkflowJob job) throws IOException {
        super(job);
        System.out.println("JenkowWorkflowRun.1: "+getParent().getDisplayName()+"#"+getNumber()+" procId="+procId);
    }

    public JenkowWorkflowRun(JenkowWorkflowJob job, File buildDir) throws IOException {
        super(job,buildDir);
        System.out.println("JenkowWorkflowRun.2: "+getParent().getDisplayName()+"#"+getNumber()+" procId="+procId);
    }
    
    public String getProcId() {
        return procId;
    }

    public void setProcId(String procId) {
        this.procId = procId;
    }

    public void run() {
        run(new Runner() {
            @Override
            public Result run(BuildListener listener) throws Exception {
                procId = WfUtil.launchWf(listener.getLogger(),getParent().getWorkflowName(),getParent().getName(),Integer.valueOf(getNumber()));
                System.out.println("procId="+procId);
                if (procId != null) return Result.SUCCESS;
                return Result.FAILURE;
            }

            @Override
            public void post(BuildListener listener) throws Exception {
                System.out.println("JenkowWorkflowRun.run.Runner.post");
            }

            @Override
            public void cleanUp(BuildListener listener) throws Exception {
                System.out.println("JenkowWorkflowRun.run.Runner.cleanup");
            }
        });
    }

    public HttpResponse doComplete() throws IOException {
        System.out.println("JenkowWorkflowRun.doComplete");
        markCompleted(Result.SUCCESS);
        return HttpResponses.redirectToDot();
    }

    public HttpResponse doWriteLog() throws IOException {
        System.out.println("JenkowWorkflowRun.doWriteLog");
        TaskListener l = createListener();
        l.getLogger().println("Current time is "+new Date());
        return HttpResponses.redirectTo("console");
    }

    @Override
    public void doStop(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        // This is where you talk to the external system to actually abort stuff
        System.out.println("doing abort");
        System.out.println("procId="+procId);

        if (false) {
            // if you couldn't abort...
            // this would still keep the build marked as running
            // alternatively maybe you want to call markCompleted(FAILURE) to mark it as failed?
            throw new IOException("Failed to abort");
        }
        
        markCompleted(Result.ABORTED);
        rsp.forwardToPreviousPage(req);
    }
}
