/*
 * The MIT License
 *
 * Copyright (c) 2017 CloudBees, Inc.
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.security;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequest;
import hudson.model.UnprotectedRootAction;
import hudson.model.User;
import hudson.security.csrf.DefaultCrumbIssuer;
import hudson.util.HttpResponses;
import hudson.util.Scrambler;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.jvnet.hudson.test.TestExtension;
import org.kohsuke.stapler.HttpResponse;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ApiCrumbExclusionTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    private WebClient wc;

    @Test
    @Issue("JENKINS-22474")
    public void callUsingApiTokenDoesNotRequireCSRFToken() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setCrumbIssuer(null);
        User foo = User.get("foo");

        wc = j.createWebClient();

        // call with API token
        ApiTokenProperty t = foo.getProperty(ApiTokenProperty.class);
        String token = t.getApiToken();

        // API Token
        makeRequestWithAuthAndVerify("foo:" + token, "foo");
        // Basic auth using password
        makeRequestWithAuthAndVerify("foo:foo", "foo");

        wc.login("foo");

        wc = j.createWebClient();
        j.jenkins.setCrumbIssuer(new DefaultCrumbIssuer(false));

        // even with crumbIssuer enabled, we are not required to send a CSRF token when using API token
        makeRequestWithAuthAndVerify("foo:" + token, "foo");
        // Basic auth using password requires crumb
        makeRequestAndFail("foo:foo", 403);

        wc.login("foo");
    }

    private String encode(String prefix, String userAndPass) {
        if (userAndPass == null) {
            return null;
        }
        return prefix + " " + Scrambler.scramble(userAndPass);
    }

    private void makeRequestWithAuthAndVerify(String userAndPass, String username) throws IOException, SAXException {
        makeRequestWithAuthCodeAndVerify(encode("Basic", userAndPass), username);
    }

    private void makeRequestWithAuthCodeAndVerify(String authCode, String expected) throws IOException, SAXException {
        WebRequest req = new WebRequest(new URL(j.getURL(), "test-post"));
        req.setHttpMethod(HttpMethod.POST);
        req.setEncodingType(null);
        if (authCode != null)
            req.setAdditionalHeader("Authorization", authCode);
        Page p = wc.getPage(req);
        assertEquals(expected, p.getWebResponse().getContentAsString().trim());
    }

    private void makeRequestAndFail(String userAndPass, int expectedCode) throws IOException, SAXException {
        makeRequestWithAuthCodeAndFail(encode("Basic", userAndPass), expectedCode);
    }

    private void makeRequestWithAuthCodeAndFail(String authCode, int expectedCode) throws IOException, SAXException {
        try {
            makeRequestWithAuthCodeAndVerify(authCode, "-");
            fail();
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(expectedCode, e.getStatusCode());
        }
    }

    @TestExtension
    public static class WhoAmI implements UnprotectedRootAction {
        @Override
        public String getIconFileName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getUrlName() {
            return "test-post";
        }

        public HttpResponse doIndex() {
            User u = User.current();
            return HttpResponses.plainText(u != null ? u.getId() : "anonymous");
        }
    }
}
