package hudson.tools;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import hudson.tasks.Maven;
import hudson.tasks.Ant;

/**
 * @author huybrechts
 */
public class ToolLocationTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    /**
     * Test xml compatibility since 'extends ToolInstallation'
     */
    @Test
    @LocalData
    public void toolCompatibility() {
        Maven.MavenInstallation[] maven = j.jenkins.getDescriptorByType(Maven.DescriptorImpl.class).getInstallations();
        assertEquals(maven.length, 1);
        assertEquals(maven[0].getHome(), "bar");
        assertEquals(maven[0].getName(), "Maven 1");

        Ant.AntInstallation[] ant = j.jenkins.getDescriptorByType(Ant.DescriptorImpl.class).getInstallations();
        assertEquals(ant.length, 1);
        assertEquals(ant[0].getHome(), "foo");
        assertEquals(ant[0].getName(), "Ant 1");
    }
}
