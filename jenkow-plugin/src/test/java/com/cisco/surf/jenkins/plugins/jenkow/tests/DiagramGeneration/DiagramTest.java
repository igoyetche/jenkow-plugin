package com.cisco.surf.jenkins.plugins.jenkow.tests.DiagramGeneration;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import hudson.util.DescribableList;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import junitx.framework.FileAssert;

import org.apache.commons.io.IOUtils;

import com.cisco.step.jenkins.plugins.jenkow.JenkowBuilder;
import com.cisco.step.jenkins.plugins.jenkow.JenkowPlugin;
import com.cisco.step.jenkins.plugins.jenkow.JenkowTestCase;
import com.cisco.step.jenkins.plugins.jenkow.JenkowWorkflowRepository;
import com.google.common.io.NullOutputStream;

public class DiagramTest extends JenkowTestCase {
    
    public void testDiagramGeneration() throws Exception {
        JenkowWorkflowRepository repo = JenkowPlugin.getInstance().getRepo();
        repo.ensureWorkflowDefinition("wf1");
        
        FreeStyleProject launcher = createFreeStyleProject("j1");
        DescribableList<Builder,Descriptor<Builder>> bl = launcher.getBuildersList();
        bl.add(new JenkowBuilder("wf1"));
        bl.add(new Shell("echo wf.done"));
        configRoundtrip(launcher);  // work around the problem in the core of not registering our builder's project actions

        testImage("job/j1/jenkow/graph","default");
        testImage("job/j1/jenkow/graph/0","0");
        testImage("job/j1/jenkow/graph/wf1","wf1");

        testError("job/j1/jenkow/graph/1");
        testError("job/j1/jenkow/graph/no-such-workflow");
        testError("job/j1/jenkow/graph/WF1");
    }

    private void testImage(String path, String suffix) throws IOException {
        URL url = new URL(new WebClient().getContextPath()+path);
        System.out.println("url="+url);
        
        BufferedImage img = ImageIO.read(url);
        File pf = new File("target/test-artifacts/"+getTestName()+"-"+suffix+".png");
        pf.getParentFile().mkdirs();
        ImageIO.write(img,"png",pf);
        FileAssert.assertBinaryEquals("generated diagram file discrepancy: ",new File(getResource("ref.png")),pf);
    }

    private void testError(String path) throws Exception{
        try {
            URL url = new URL(new WebClient().getContextPath()+path);
            IOUtils.copy(url.openStream(), new NullOutputStream());
            fail("Should have been 404");
        } catch (FileNotFoundException e) {
            // expected
        }
    }
}
